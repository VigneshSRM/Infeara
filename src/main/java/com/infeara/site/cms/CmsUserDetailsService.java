package com.infeara.site.cms;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * The single content-manager account, described entirely by configuration.
 *
 * <p>Three details matter more than the rest:</p>
 * <ul>
 *   <li>The account exists only when the environment supplies a credential, so a clone, a preview
 *       or a CI run has no administration account to guess.</li>
 *   <li>The submitted name is compared with a constant-time comparison, and an unknown name takes
 *       the same path as a wrong password, so the response cannot be used to learn which usernames
 *       exist.</li>
 *   <li>Lockout is checked before the account is looked up, so locking applies even to names that
 *       do not exist and the lockout itself reveals nothing.</li>
 * </ul>
 *
 * <p>A <em>fresh</em> {@link UserDetails} is returned for every authentication attempt. Spring
 * Security erases the credentials of the authenticated principal once a sign-in succeeds, and that
 * erasure reaches the {@code User} object itself; handing out one shared instance would therefore
 * empty the stored password after the first successful sign-in and reject every later one.</p>
 */
@Service
public class CmsUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CmsUserDetailsService.class);
    private static final int MINIMUM_PLAIN_SECRET_LENGTH = 12;
    private static final String ROLE = "CMS_ADMIN";

    private final LoginAttemptService attempts;
    private final String accountName;
    private final String passwordHash;

    public CmsUserDetailsService(CmsProperties properties, PasswordEncoder passwordEncoder,
                                 LoginAttemptService attempts) {
        this.attempts = attempts;
        this.accountName = properties.resolvedUsername();
        this.passwordHash = resolveHash(properties, passwordEncoder);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (attempts.isLocked(ClientAddress.current(), username)) {
            throw new LockedException("Too many failed sign-in attempts");
        }
        if (username == null || username.isBlank() || passwordHash == null
                || !sameName(accountName, username.strip())) {
            log.info("CMS sign-in rejected: no account matches [{}] (accountConfigured={})",
                    username, passwordHash != null);
            throw new UsernameNotFoundException("No such account");
        }
        return User.withUsername(accountName)
                .password(passwordHash)
                .roles(ROLE)
                .build();
    }

    /** True when the deployment has configured an account, used to explain a closed door honestly. */
    public boolean isConfigured() {
        return passwordHash != null;
    }

    /**
     * Returns the hash to verify against, or {@code null} when this instance has no account. A
     * supplied BCrypt hash is used as it stands; a plain secret is hashed in memory, and only if it
     * is long enough to be worth accepting.
     */
    private static String resolveHash(CmsProperties properties, PasswordEncoder passwordEncoder) {
        String secret = properties.resolvedSecret();
        if (secret == null) {
            log.warn("CMS sign-in is disabled: no CMS_ADMIN_PASSWORD_HASH or CMS_ADMIN_PASSWORD is set, "
                    + "so /cms stays closed on this instance");
            return null;
        }
        if (properties.usesPreHashedSecret()) {
            log.info("CMS account [{}] is active with a BCrypt hash from the environment",
                    properties.resolvedUsername());
            return secret;
        }
        if (secret.length() < MINIMUM_PLAIN_SECRET_LENGTH) {
            log.error("CMS sign-in is disabled: CMS_ADMIN_PASSWORD is shorter than {} characters. "
                    + "Supply a longer secret, or a BCrypt hash in CMS_ADMIN_PASSWORD_HASH",
                    MINIMUM_PLAIN_SECRET_LENGTH);
            return null;
        }
        log.warn("CMS account [{}] is active; the plain secret from the environment is hashed in memory. "
                + "Prefer CMS_ADMIN_PASSWORD_HASH so the secret is never readable by this process",
                properties.resolvedUsername());
        return passwordEncoder.encode(secret);
    }

    /** Length-independent comparison, so a wrong name cannot be spotted by how fast it is rejected. */
    private static boolean sameName(String expected, String submitted) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }
}
