/**
 * TripJoy k6 — SOAK TEST
 *
 * Purpose: Detect memory leaks, connection pool exhaustion, and performance
 *          degradation over extended periods (simulate a full business day).
 * Load:    Moderate steady 30 VUs over 30 minutes.
 * Run:     k6 run scenarios/soak-test.js [-e ENV=local]
 *
 * Key Checks:
 *   - p(95) should NOT drift upward over time (indicative of memory leak)
 *   - Error rate should stay stable
 *   - Resource counts stay consistent (no unbounded growth)
 */

import { sleep } from 'k6';
import { soakThresholds } from '../config/thresholds.js';
import { env } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import {
    scenarioGetMyProfile,
    scenarioGetMyGroups,
    scenarioBrowseFeed,
    scenarioSearchPosts,
    scenarioSearchLocations,
    scenarioNearbyLocations,
    scenarioLocationAutocomplete,
    scenarioCheckNotifications,
    scenarioGetConversations,
    scenarioSearchMessages,
    scenarioGetAdministrativeLocations,
    scenarioSearchUsers,
    scenarioGetUploadSignature,
} from '../lib/scenarios.js';

// ──────────────────────────────────────────────────────────────
// Options
// ──────────────────────────────────────────────────────────────
export const options = {
    stages: [
        { duration: '3m',  target: 30 },   // warm up
        { duration: '24m', target: 30 },   // steady state (main soak)
        { duration: '3m',  target: 0 },    // cooldown
    ],
    thresholds: soakThresholds,
    tags: { testType: 'soak', project: 'tripjoy' },
};

// ──────────────────────────────────────────────────────────────
// SETUP
// ──────────────────────────────────────────────────────────────
export function setup() {
    const r = login(env.users.regular.username, env.users.regular.password);
    if (!r) throw new Error('[soak] Cannot login — aborting soak test');
    return { access_token: r.access_token };
}

// ──────────────────────────────────────────────────────────────
// MAIN: steady, diverse workload representing a typical user session
// ──────────────────────────────────────────────────────────────
export default function (data) {
    if (!data?.access_token) { sleep(2); return; }

    const headers = authHeaders(data.access_token);
    const roll = Math.random();

    if (roll < 0.25) {
        // Feed browsing
        scenarioBrowseFeed(headers);
        sleep(1);
        scenarioSearchPosts(headers);

    } else if (roll < 0.50) {
        // Location discovery
        scenarioSearchLocations(headers);
        sleep(0.5);
        scenarioLocationAutocomplete(headers);
        sleep(0.5);
        scenarioNearbyLocations(headers);

    } else if (roll < 0.70) {
        // Profile + group read
        scenarioGetMyProfile(headers);
        sleep(0.5);
        scenarioGetMyGroups(headers);
        sleep(0.5);
        scenarioSearchUsers(headers);

    } else if (roll < 0.85) {
        // Notifications + chat inbox
        scenarioCheckNotifications(headers);
        sleep(0.5);
        scenarioGetConversations(headers);
        sleep(0.5);
        scenarioSearchMessages(headers);

    } else {
        // Misc reads
        scenarioGetAdministrativeLocations(headers);
        sleep(0.5);
        scenarioGetUploadSignature(headers);
    }

    // Realistic think time (user reading content)
    sleep(Math.random() * 3 + 1);
}

// ──────────────────────────────────────────────────────────────
// TEARDOWN
// ──────────────────────────────────────────────────────────────
export function teardown(data) {
    console.log('[soak] Soak test completed. Check metrics for drift patterns.');
}
