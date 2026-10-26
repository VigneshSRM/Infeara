#!/usr/bin/env bash
# Security gate for the content manager's sign-in.
#
# Runs against a live instance and checks the controls that are easy to lose later: the page and
# its headers, that an anonymous caller cannot reach the workspace, that a post without a CSRF token
# is refused, that a wrong password and an unknown username are answered the same way, that a
# successful sign-in reaches the workspace, that signing out needs a POST, and that repeated
# failures lock the account.
#
# Usage:
#   CMS_ADMIN_USERNAME=... CMS_ADMIN_PASSWORD=... scripts/check-cms-login.sh [base-url]
#
# Exit code 0 means every check passed; 1 means at least one release-blocking problem was found.

set -uo pipefail

BASE_URL="${1:-http://localhost:8080}"
BASE_URL="${BASE_URL%/}"
USERNAME="${CMS_ADMIN_USERNAME:-}"
PASSWORD="${CMS_ADMIN_PASSWORD:-}"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

failures=0
checks=0

pass() { checks=$((checks + 1)); printf '  ok   %s\n' "$1"; }
fail() { checks=$((checks + 1)); failures=$((failures + 1)); printf '  FAIL %s\n' "$1"; }

token_from() { grep -o 'name="_csrf" value="[^"]*"' "$1" | head -1 | sed -e 's/.*value="//' -e 's/"$//'; }
location_of() { grep -i '^location:' "$1" | head -1 | tr -d '\r' | sed 's/^[Ll]ocation:[[:space:]]*//'; }

# Signs in with the supplied credentials using the session in $1 and the token from $2.
# Response headers are written to $3; the response body is discarded.
post_sign_in() {
  local jar="$1" page="$2" headers="$3" name="$4" secret="$5" token
  token="$(token_from "$page")"
  curl -sS -o /dev/null -D "$headers" -b "$jar" -c "$jar" \
    --data-urlencode "username=${name}" \
    --data-urlencode "password=${secret}" \
    --data-urlencode "_csrf=${token}" \
    "${BASE_URL}/cms/login"
}

if [ -z "$USERNAME" ] || [ -z "$PASSWORD" ]; then
  echo "check-cms-login: CMS_ADMIN_USERNAME and CMS_ADMIN_PASSWORD must be set" >&2
  exit 1
fi

echo "CMS sign-in gate against ${BASE_URL}"
echo

# 1. The page itself: reachable, unindexable, and carrying a token.
status="$(curl -sS -o "$WORK_DIR/login.html" -c "$WORK_DIR/anon.jar" -w '%{http_code}' "${BASE_URL}/cms/login")" || status="000"
if [ "$status" = "200" ]; then
  pass "sign-in page returns 200"
else
  fail "sign-in page did not return 200 (status ${status})"
fi

if grep -qi '<meta name="robots" content="noindex' "$WORK_DIR/login.html"; then
  pass "sign-in page is marked noindex"
else
  fail "sign-in page is missing the noindex robots meta tag"
fi

if [ -n "$(token_from "$WORK_DIR/login.html")" ]; then
  pass "sign-in form carries a CSRF token"
else
  fail "sign-in form has no CSRF token"
fi

if grep -q 'action="/cms/login" method="post"' "$WORK_DIR/login.html"; then
  pass "sign-in form posts to the fixed processing URL"
else
  fail "sign-in form does not post to /cms/login"
fi

if grep -q 'autocomplete="current-password"' "$WORK_DIR/login.html" \
   && ! grep -qE 'id="password"[^>]*value=' "$WORK_DIR/login.html"; then
  pass "password field asks for the current password and never echoes one"
else
  fail "password field is missing autocomplete or echoes a value"
fi

external="$(grep -oE '(src|href)="https?://[^"]+"' "$WORK_DIR/login.html" || true)"
if [ -z "$external" ]; then
  pass "sign-in page loads nothing from another origin"
else
  fail "sign-in page references a third-party origin (${external})"
fi

# 2. Response headers that make the page hard to steal, frame, sniff or cache.
curl -sS -I -o "$WORK_DIR/headers.txt" "${BASE_URL}/cms/login" || true
if grep -qi "^content-security-policy:.*default-src 'none'" "$WORK_DIR/headers.txt" \
   && grep -qi "frame-ancestors 'none'" "$WORK_DIR/headers.txt"; then
  pass "Content-Security-Policy denies other origins and framing"
else
  fail "Content-Security-Policy is missing or permissive"
fi
if grep -qi '^x-frame-options: DENY' "$WORK_DIR/headers.txt"; then
  pass "X-Frame-Options is DENY"
else
  fail "X-Frame-Options is missing or not DENY"
fi
if grep -qi '^x-content-type-options: nosniff' "$WORK_DIR/headers.txt"; then
  pass "X-Content-Type-Options is nosniff"
else
  fail "X-Content-Type-Options is missing"
fi
if grep -qi '^referrer-policy: no-referrer' "$WORK_DIR/headers.txt"; then
  pass "Referrer-Policy is no-referrer"
else
  fail "Referrer-Policy is missing"
fi
if grep -qi '^x-robots-tag: noindex' "$WORK_DIR/headers.txt"; then
  pass "administration area sends X-Robots-Tag noindex"
else
  fail "administration area is missing X-Robots-Tag noindex"
fi
if grep -qi '^cache-control:.*no-store' "$WORK_DIR/headers.txt"; then
  pass "administration responses are not cacheable"
else
  fail "administration responses may be cached"
fi

# 3. The workspace is closed to anonymous callers.
status="$(curl -sS -o /dev/null -D "$WORK_DIR/anon-workspace.txt" -w '%{http_code}' "${BASE_URL}/cms")" || status="000"
if [ "$status" = "302" ] && location_of "$WORK_DIR/anon-workspace.txt" | grep -q '/cms/login'; then
  pass "the workspace redirects an anonymous caller to the sign-in page"
else
  fail "the workspace did not redirect an anonymous caller (status ${status})"
fi

# 4. A post without a token is refused, so a cross-site form cannot attempt a sign-in.
status="$(curl -sS -o /dev/null -w '%{http_code}' -X POST \
  --data-urlencode "username=${USERNAME}" --data-urlencode "password=${PASSWORD}" \
  "${BASE_URL}/cms/login")" || status="000"
if [ "$status" = "403" ]; then
  pass "a sign-in post without a CSRF token is refused with 403"
else
  fail "a sign-in post without a CSRF token returned ${status}, expected 403"
fi

# 5. A wrong password is rejected without opening a session.
curl -sS -o "$WORK_DIR/wrong.html" -c "$WORK_DIR/wrong.jar" "${BASE_URL}/cms/login" > /dev/null
post_sign_in "$WORK_DIR/wrong.jar" "$WORK_DIR/wrong.html" "$WORK_DIR/wrong.txt" "$USERNAME" "definitely-not-the-password-$RANDOM"
if location_of "$WORK_DIR/wrong.txt" | grep -q '/cms/login?error'; then
  pass "a wrong password is sent back to the form with a generic error"
else
  fail "a wrong password was not rejected with the generic error (location: $(location_of "$WORK_DIR/wrong.txt"))"
fi
status="$(curl -sS -o /dev/null -w '%{http_code}' -b "$WORK_DIR/wrong.jar" "${BASE_URL}/cms")" || status="000"
if [ "$status" = "302" ]; then
  pass "a failed sign-in does not open the workspace"
else
  fail "a failed sign-in left the workspace reachable (status ${status})"
fi

# A single failure must not lock a good sign-in out. Checked here, before the deliberate burst of
# failures below, so the probe never depends on how many failures this address has spent.
curl -sS -o "$WORK_DIR/retry.html" -c "$WORK_DIR/retry.jar" "${BASE_URL}/cms/login" > /dev/null
post_sign_in "$WORK_DIR/retry.jar" "$WORK_DIR/retry.html" "$WORK_DIR/retry.txt" "$USERNAME" "$PASSWORD"
if location_of "$WORK_DIR/retry.txt" | grep -qE '/cms$'; then
  pass "one failed attempt does not block the account"
else
  fail "one failed attempt blocked the account (location: $(location_of "$WORK_DIR/retry.txt"))"
fi

# 6. An unknown username is answered exactly like a wrong password: no account enumeration.
curl -sS -o "$WORK_DIR/unknown.html" -c "$WORK_DIR/unknown.jar" "${BASE_URL}/cms/login" > /dev/null
post_sign_in "$WORK_DIR/unknown.jar" "$WORK_DIR/unknown.html" "$WORK_DIR/unknown.txt" "nobody-$RANDOM" "irrelevant-$RANDOM"
if location_of "$WORK_DIR/unknown.txt" | grep -q '/cms/login?error'; then
  pass "an unknown username is rejected with the same generic error"
else
  fail "an unknown username produced a different answer (location: $(location_of "$WORK_DIR/unknown.txt"))"
fi

# 7. The correct credentials reach the workspace over a fresh, hardened session cookie.
curl -sS -o "$WORK_DIR/admin.html" -c "$WORK_DIR/admin.jar" "${BASE_URL}/cms/login" > /dev/null
post_sign_in "$WORK_DIR/admin.jar" "$WORK_DIR/admin.html" "$WORK_DIR/admin.txt" "$USERNAME" "$PASSWORD"
if location_of "$WORK_DIR/admin.txt" | grep -qE '/cms$'; then
  pass "the correct credentials are sent to the workspace"
else
  # One retry with a brand new session: a failure that reproduces names a real problem, and a
  # failure that does not reproduce is still reported rather than hidden.
  curl -sS -o "$WORK_DIR/admin2.html" -c "$WORK_DIR/admin2.jar" "${BASE_URL}/cms/login" > /dev/null
  post_sign_in "$WORK_DIR/admin2.jar" "$WORK_DIR/admin2.html" "$WORK_DIR/admin2.txt" "$USERNAME" "$PASSWORD"
  second="$(location_of "$WORK_DIR/admin2.txt")"
  if printf '%s' "$second" | grep -qE '/cms$'; then
    fail "the correct credentials were rejected once and accepted on a retry (first: $(location_of "$WORK_DIR/admin.txt"), retry: ${second})"
    cp "$WORK_DIR/admin2.jar" "$WORK_DIR/admin.jar"
    cp "$WORK_DIR/admin2.html" "$WORK_DIR/admin.html"
  else
    fail "the correct credentials did not reach the workspace (first: $(location_of "$WORK_DIR/admin.txt"), retry: ${second})"
  fi
fi
if grep -qi '^set-cookie: CMS_SESSION=' "$WORK_DIR/admin.txt" && grep -qi 'httponly' "$WORK_DIR/admin.txt"; then
  pass "the session cookie is named CMS_SESSION and is HttpOnly"
else
  fail "the session cookie is missing, renamed, or not HttpOnly"
fi
if grep -qi 'samesite=lax' "$WORK_DIR/admin.txt"; then
  pass "the session cookie is SameSite=Lax"
else
  fail "the session cookie does not set SameSite=Lax"
fi

status="$(curl -sS -o "$WORK_DIR/workspace.html" -b "$WORK_DIR/admin.jar" -w '%{http_code}' "${BASE_URL}/cms")" || status="000"
if [ "$status" = "200" ] && grep -q "$USERNAME" "$WORK_DIR/workspace.html" && grep -q 'action="/cms/logout"' "$WORK_DIR/workspace.html"; then
  pass "the workspace loads for the signed-in account and offers a sign-out form"
else
  fail "the workspace did not load for the signed-in account (status ${status})"
fi

# 8. Signing out is a state change: it needs a token, and it ends the session.
status="$(curl -sS -o /dev/null -w '%{http_code}' -X POST -b "$WORK_DIR/admin.jar" "${BASE_URL}/cms/logout")" || status="000"
if [ "$status" = "403" ]; then
  pass "a sign-out post without a CSRF token is refused"
else
  fail "a sign-out post without a CSRF token returned ${status}, expected 403"
fi
status="$(curl -sS -o /dev/null -w '%{http_code}' "${BASE_URL}/cms/logout")" || status="000"
if [ "$status" = "302" ] || [ "$status" = "404" ] || [ "$status" = "405" ]; then
  pass "a GET request cannot sign a session out"
else
  fail "a GET request to the sign-out URL returned ${status}"
fi
logout_token="$(token_from "$WORK_DIR/workspace.html")"
curl -sS -o /dev/null -D "$WORK_DIR/logout.txt" -b "$WORK_DIR/admin.jar" -c "$WORK_DIR/admin.jar" \
  --data-urlencode "_csrf=${logout_token}" "${BASE_URL}/cms/logout"
if location_of "$WORK_DIR/logout.txt" | grep -q '/cms/login?signedOut'; then
  pass "a signed-out session is returned to the sign-in page"
else
  fail "signing out did not reach the sign-in page (location: $(location_of "$WORK_DIR/logout.txt"))"
fi
status="$(curl -sS -o /dev/null -w '%{http_code}' -b "$WORK_DIR/admin.jar" "${BASE_URL}/cms")" || status="000"
if [ "$status" = "302" ]; then
  pass "the workspace is closed again after signing out"
else
  fail "the workspace remained reachable after signing out (status ${status})"
fi

# 9. Repeated failures lock the account. The name is a stranger so the real account stays usable.
curl -sS -o "$WORK_DIR/attack.html" -c "$WORK_DIR/attack.jar" "${BASE_URL}/cms/login" > /dev/null
locked=""
for attempt in 1 2 3 4 5 6; do
  post_sign_in "$WORK_DIR/attack.jar" "$WORK_DIR/attack.html" "$WORK_DIR/attack.txt" "intruder" "guess-${attempt}"
  locked="$(location_of "$WORK_DIR/attack.txt")"
done
if printf '%s' "$locked" | grep -q '/cms/login?locked'; then
  pass "repeated failures lock the account instead of allowing endless guessing"
else
  fail "repeated failures never locked the account (last location: ${locked})"
fi

echo
if [ "$failures" -gt 0 ]; then
  echo "CMS sign-in gate FAILED (${failures} of ${checks} checks failed)"
  exit 1
fi
echo "CMS sign-in gate passed (${checks} checks)"
