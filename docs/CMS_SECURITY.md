# Content manager sign-in

The site now has a protected area at `/cms`: one sign-in page, one workspace behind it, and a
sign-out. This document records what protects it, how a deployment configures it, what is
deliberately left out, and how the controls are proven on every push.

## What was built

| Path | Method | Who reaches it |
| --- | --- | --- |
| `/cms/login` | GET | anyone; the form |
| `/cms/login` | POST | anyone; the only place credentials are accepted |
| `/cms` | GET | a signed-in CMS administrator |
| `/cms/logout` | POST | a signed-in CMS administrator |

Anything else under `/cms` requires the `CMS_ADMIN` role. The public marketing site is untouched:
it is served by a second, separate filter chain that only makes its pages reachable.

## Controls

**Credentials are never in the repository.** The account exists only when the deployment supplies
`CMS_ADMIN_PASSWORD_HASH` (BCrypt, preferred) or `CMS_ADMIN_PASSWORD`. With neither set, no account
exists, so a clone, a preview and a CI instance all have nothing to sign in to. That is the
difference between a locked door and a door with a default password.

**Passwords are slow to verify.** BCrypt at cost 12 with a per-password salt, so an attacker who
takes a hash still cannot test guesses quickly and identical passwords do not share a hash. A plain
secret is hashed in memory at start-up and is never written anywhere.

**Guessing is expensive.** Five failed attempts lock the account for fifteen minutes; every further
failure past the threshold doubles the next lockout, up to a day. An independent counter per source
address locks the address after twenty failures, so rotating usernames does not help. Lockout is
checked *before* the account is looked up, which means it behaves identically for names that exist
and names that do not.

**Answers do not confirm anything.** A wrong password, an unknown username and an over-long field
all land on the same page with the same sentence. The submitted name is compared with a
constant-time comparison, and unknown names take the same path as wrong passwords, so neither
timing nor wording reveals which usernames are real.

**The account object is rebuilt for every attempt.** Spring Security erases the credentials of the
authenticated principal once a sign-in succeeds, and that erasure reaches the stored `UserDetails`.
Returning one cached instance would empty the stored hash after the first successful sign-in and
reject every later one with a generic "bad credentials" — a fault that only a real second sign-in
reveals, which is why the gate signs in twice.

**Sessions are hardened.** The identifier changes at sign-in (session fixation protection), the
cookie is `HttpOnly` and `SameSite=Lax`, it is named `CMS_SESSION` rather than advertising the
server, it is marked `Secure` automatically when the request arrives over TLS, and the idle timeout
is fifteen minutes. Responses in this area are `no-store`.

**Cross-site requests cannot act.** Every state change needs a CSRF token: the sign-in post, the
sign-out post, and any future form. Signing out is a POST, not a link, so a stray image tag cannot
end a session. Both the sign-in and the workspace post to fixed paths, so nothing in the URL can
steer where credentials go, and a saved request is never replayed after sign-in.

**Nothing else can run in the page.** The sign-in page loads one stylesheet, one script and one
image, all from this application — no third-party font, script or tracker. That is what makes the
`Content-Security-Policy` possible: `default-src 'none'`, `script-src 'self'`, `frame-ancestors
'none'`, `base-uri 'none'`. On top of it the area sends `X-Frame-Options: DENY`,
`X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, `Permissions-Policy` denying
device APIs, and `Cross-Origin-Opener-Policy` / `Cross-Origin-Resource-Policy: same-origin`.

**It cannot be found by accident.** The area answers with `X-Robots-Tag: noindex, nofollow` and a
`noindex` meta tag, `robots.txt` disallows `/cms/`, and it is absent from the sitemap — while the
public pages keep their `index,follow` directive.

**Attempts are audited.** Rejected attempts are logged with the source address and the submitted
name, lockouts are logged as warnings with the threshold and duration, and successful sign-ins are
counted. Passwords never reach a log line: the application never sees one.

## Configuring a deployment

| Variable | Meaning |
| --- | --- |
| `CMS_ADMIN_USERNAME` | The account name. Defaults to `cms-admin`. |
| `CMS_ADMIN_PASSWORD_HASH` | A BCrypt hash. Preferred: the process never holds the secret. |
| `CMS_ADMIN_PASSWORD` | A plain secret of at least 12 characters, hashed in memory at start-up. |
| `CMS_MAX_ATTEMPTS` | Failed attempts before a lockout. Defaults to 5. |
| `CMS_LOCKOUT_MINUTES` | First lockout duration. Defaults to 15; later ones double. |

To produce a hash, run this on any machine with the application's classpath, or use any BCrypt
implementation with a cost of 12:

```bash
python3 -c "import bcrypt,sys; print(bcrypt.hashpw(sys.argv[1].encode(), bcrypt.gensalt(12)).decode())" 'your passphrase'
```

Behind a load balancer, set `server.forward-headers-strategy=framework` so the lockout sees the
address the proxy observed rather than the proxy's own. Do not enable it without a trusted proxy:
a caller could then choose the address their attempts are counted against.

If several instances run behind the balancer, the lockout counters are per instance. The correct
fix at that scale is a shared limiter at the edge (or a WAF); the in-memory counter is deliberately
not a second source of truth about who may sign in, and it disappears on restart.

## What is deliberately not here

- **Multi-factor authentication.** It needs enrolled secrets and a recovery path; a half-built
  second factor is worse than none. The single account is protected by a long secret, lockout and
  an audited, unindexed, unframeable page.
- **Self-service password reset.** There is no mail infrastructure in this application, and a reset
  flow is the most commonly broken part of an authentication system. The site owner issues
  credentials.
- **A user database and roles beyond one administrator.** The account is configuration, not data,
  which is why there is no table to leak, no registration endpoint and no privilege boundary to get
  wrong. Editing screens are a separate milestone.
- **Remember-me.** A persistent cookie on an administration area is a liability without a device
  management story.

## How it is proven

`scripts/check-cms-login.sh` runs against the live application on every push, after the SEO gate.
It creates no fixtures — it drives the real endpoints with curl:

```
ok   sign-in page returns 200
ok   sign-in page is marked noindex
ok   sign-in form carries a CSRF token
ok   sign-in form posts to the fixed processing URL
ok   password field asks for the current password and never echoes one
ok   sign-in page loads nothing from another origin
ok   Content-Security-Policy denies other origins and framing
ok   X-Frame-Options is DENY
ok   X-Content-Type-Options is nosniff
ok   Referrer-Policy is no-referrer
ok   administration area sends X-Robots-Tag noindex
ok   administration responses are not cacheable
ok   the workspace redirects an anonymous caller to the sign-in page
ok   a sign-in post without a CSRF token is refused with 403
ok   a wrong password is sent back to the form with a generic error
ok   a failed sign-in does not open the workspace
ok   an unknown username is rejected with the same generic error
ok   the correct credentials are sent to the workspace
ok   the session cookie is named CMS_SESSION and is HttpOnly
ok   the session cookie is SameSite=Lax
ok   the workspace loads for the signed-in account and offers a sign-out form
ok   a sign-out post without a CSRF token is refused
ok   a GET request cannot sign a session out
ok   a signed-out session is returned to the sign-in page
ok   the workspace is closed again after signing out
ok   repeated failures lock the account instead of allowing endless guessing
ok   the lockout does not block a different account
```

The CI run generates a throwaway credential for that run only (`openssl rand`), so no password is
ever committed, and the account cannot outlive the job.
