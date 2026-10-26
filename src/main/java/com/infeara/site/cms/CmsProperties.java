package com.infeara.site.cms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the content manager, supplied entirely by the environment.
 *
 * <p>No credential is ever stored in the repository. A deployment provides either
 * {@code CMS_ADMIN_PASSWORD_HASH} (a BCrypt hash, preferred) or {@code CMS_ADMIN_PASSWORD}
 * (a value held by the platform secret store and hashed in memory at start-up). When neither is
 * present the account does not exist and the content manager stays closed, which is what keeps a
 * preview or a CI instance from opening an administration area with a default password.</p>
 */
@ConfigurationProperties(prefix = "cms")
public record CmsProperties(String username, String password, String passwordHash,
                            int maxAttempts, int lockoutMinutes) {

    /** The account name, defaulting to a neutral value rather than a well-known product default. */
    public String resolvedUsername() {
        return username == null || username.isBlank() ? "cms-admin" : username.strip();
    }

    /** Failed attempts allowed per account before a lockout starts. */
    public int resolvedMaxAttempts() {
        return maxAttempts > 0 ? maxAttempts : 5;
    }

    /** Minutes of lockout for the first threshold breach; later breaches double. */
    public int resolvedLockoutMinutes() {
        return lockoutMinutes > 0 ? lockoutMinutes : 15;
    }

    /** A hash is preferred; the plain value is accepted only so a secret store can hold one entry. */
    public String resolvedSecret() {
        if (passwordHash != null && !passwordHash.isBlank()) {
            return passwordHash;
        }
        return password != null && !password.isBlank() ? password : null;
    }

    /** True when the deployment has configured a credential and sign-in can be attempted. */
    public boolean hasCredential() {
        return resolvedSecret() != null;
    }

    /** True when the configured secret is already a password hash rather than a plain value. */
    public boolean usesPreHashedSecret() {
        String hash = passwordHash != null ? passwordHash.strip() : "";
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
