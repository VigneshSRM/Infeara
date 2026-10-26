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
 */
@Service
public class CmsUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CmsUserDetailsService.class);
    private static final int MINIMUM_PLAIN_SECRET_LENGTH = 12;

    private final LoginAttemptService attempts;
    private final UserDetails account;

    public CmsUserDetailsService(CmsProperties properties, PasswordEncoder passwordEncoder,
                                 LoginAttemptService attempts) {
        this.attempts = attempts;
        this.account = buildAccount(properties, passwordEncoder);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (attempts.isLocked(ClientAddress.current(), username)) {
            throw new LockedException("Too many failed sign-in attempts");
        }
        if (username == null || username.isBlank() || account == null || !sameName(account.getUsername(), username.strip())) {
            throw new UsernameNotFoundException("No such account");
        }
        return account;
    }

    /** True when the deployment has configured an account, used to explain a closed door honestly. */
    public boolean isConfigured() {
        return account != null;
    }

    private static UserDetails buildAccount(CmsProperties properties, PasswordEncoder passwordEncoder) {
        String secret = properties.resolvedSecret();
        if (secret == null) {
            log.warn("CMS sign-in is disabled: no CMS_ADMIN_PASSWORD_HASH or CMS_ADMIN_PASSWORD is set, "
                    + "so /cms stays closed on this instance");
            return null;
        }
        String hash = secret;
        if (!properties.usesPreHashedSecret()) {
            if (secret.length() < MINIMUM_PLAIN_SECRET_LENGTH) {
                log.error("CMS sign-in is disabled: CMS_ADMIN_PASSWORD is shorter than {} characters. "
                        + "Supply a longer secret, or a BCrypt hash in CMS_ADMIN_PASSWORD_HASH",
                        MINIMUM_PLAIN_SECRET_LENGTH);
                return null;
            }
            hash = passwordEncoder.encode(secret);
            log.warn("CMS sign-in is using a plain secret from the environment; prefer CMS_ADMIN_PASSWORD_HASH "
                    + "so the secret is never readable by the application process");
        }
        return User.withUsername(properties.resolvedUsername())
                .password(hash)
                .roles("CMS_ADMIN")
                .build();
    }

    /** Length-independent comparison, so a wrong name cannot be spotted by how fast it is rejected. */
    private static boolean sameName(String expected, String submitted) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }
}
