package com.infeara.site.cms;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Slows down password guessing with an in-memory lockout that has two independent keys.
 *
 * <ul>
 *   <li><b>Account</b> — after {@code cms.max-attempts} failures for one username the account is
 *       locked, whatever address the attempts came from.</li>
 *   <li><b>Network</b> — after four times that many failures from one address, every attempt from
 *       that address is locked, so guessing cannot simply rotate through usernames.</li>
 * </ul>
 *
 * <p>Each further failure past the threshold doubles the next lockout, up to a day, so a scripted
 * attack spends progressively longer locked out. Locked state is deliberately kept in memory: it
 * is per instance, it disappears on restart, and it never becomes a second source of truth about
 * who may sign in. A deployment that runs several instances should front this with a shared rate
 * limiter at the edge.</p>
 */
@Service
public class LoginAttemptService {
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_TRACKED_KEYS = 10_000;
    private static final int NETWORK_MULTIPLIER = 4;
    private static final int MAX_DOUBLINGS = 6;
    private static final long MAX_LOCK_MINUTES = 1_440;
    private static final Duration STALE_AFTER = Duration.ofHours(6);

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final CmsProperties properties;

    public LoginAttemptService(CmsProperties properties) {
        this.properties = properties;
    }

    /** True while either the account or the originating network is locked out. */
    public boolean isLocked(String clientAddress, String username) {
        return locked(accountKey(username)) || locked(networkKey(clientAddress));
    }

    /** Records one rejected sign-in and starts or extends a lockout once a threshold is crossed. */
    public void recordFailure(String clientAddress, String username) {
        register(accountKey(username), properties.resolvedMaxAttempts(), "account");
        register(networkKey(clientAddress), properties.resolvedMaxAttempts() * NETWORK_MULTIPLIER, "network");
    }

    /** Clears the counters after a successful sign-in. */
    public void recordSuccess(String clientAddress, String username) {
        attempts.remove(accountKey(username));
        attempts.remove(networkKey(clientAddress));
    }

    private void register(String key, int threshold, String kind) {
        if (key == null) {
            return;
        }
        evictIfCrowded();
        Instant now = Instant.now();
        attempts.compute(key, (ignored, current) -> {
            Attempt attempt = current == null ? new Attempt() : current;
            attempt.failures++;
            attempt.lastSeen = now;
            if (attempt.failures >= threshold && (attempt.lockedUntil == null || now.isAfter(attempt.lockedUntil))) {
                int beyondThreshold = attempt.failures - threshold;
                long minutes = (long) properties.resolvedLockoutMinutes() << Math.min(beyondThreshold, MAX_DOUBLINGS);
                attempt.lockedUntil = now.plus(Duration.ofMinutes(Math.min(minutes, MAX_LOCK_MINUTES)));
                // The address and name are recorded for operators; the attempted secret never is.
                log.warn("CMS sign-in locked out for {} {} for {} minute(s) after {} failure(s)",
                        kind, key, Math.min(minutes, MAX_LOCK_MINUTES), attempt.failures);
            }
            return attempt;
        });
    }

    private boolean locked(String key) {
        if (key == null) {
            return false;
        }
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(attempt.lockedUntil)) {
            attempts.remove(key, attempt);
            return false;
        }
        return true;
    }

    /**
     * Keeps the map bounded. Expired and long-idle keys are dropped first; if the map is still full
     * it is emptied, which can only ever end a lockout early and never grant access.
     */
    private void evictIfCrowded() {
        if (attempts.size() < MAX_TRACKED_KEYS) {
            return;
        }
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Attempt>> iterator = attempts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Attempt> entry = iterator.next();
            Attempt attempt = entry.getValue();
            boolean unlocked = attempt.lockedUntil == null || now.isAfter(attempt.lockedUntil);
            if (unlocked && attempt.lastSeen.plus(STALE_AFTER).isBefore(now)) {
                iterator.remove();
            }
        }
        if (attempts.size() >= MAX_TRACKED_KEYS) {
            attempts.clear();
        }
    }

    private static String accountKey(String username) {
        if (username == null || username.isBlank()) {
            return "account:";
        }
        String normalised = username.strip().toLowerCase(Locale.ROOT);
        return "account:" + (normalised.length() > 64 ? normalised.substring(0, 64) : normalised);
    }

    private static String networkKey(String clientAddress) {
        if (clientAddress == null || clientAddress.isBlank()) {
            return null;
        }
        return "network:" + clientAddress.strip().toLowerCase(Locale.ROOT);
    }

    private static final class Attempt {
        private int failures;
        private Instant lockedUntil;
        private Instant lastSeen = Instant.now();
    }
}
