/**
 * TripJoy k6 — SMOKE TEST
 *
 * Purpose: Verify the system is alive and all critical endpoints respond.
 * Load:    1 VU, 2 minutes, extremely strict thresholds.
 * Run:     k6 run scenarios/smoke-test.js [-e ENV=local]
 */

import { sleep } from 'k6';
import { smokeThresholds } from '../config/thresholds.js';
import { env } from '../config/environments.js';
import { login, authHeaders, introspect } from '../lib/auth.js';
import {
    scenarioGetMyProfile,
    scenarioGetMyGroups,
    scenarioGetAdministrativeLocations,
    scenarioSearchLocations,
    scenarioNearbyLocations,
    scenarioBrowseFeed,
    scenarioCheckNotifications,
    scenarioGetConversations,
    scenarioSearchUsers,
} from '../lib/scenarios.js';

export const options = {
    vus: 1,
    duration: '2m',
    thresholds: smokeThresholds,
    tags: { testType: 'smoke', project: 'tripjoy' },
};

// ──────────────────────────────────────────────────────────────
// SETUP: login once, share token
// ──────────────────────────────────────────────────────────────
export function setup() {
    const result = login(env.users.regular.username, env.users.regular.password);
    if (!result) throw new Error('[smoke] Cannot login — aborting smoke test');

    console.log('[smoke] Setup complete. Token acquired.');
    return { access_token: result.access_token };
}

// ──────────────────────────────────────────────────────────────
// MAIN: run one complete health check per iteration
// ──────────────────────────────────────────────────────────────
export default function (data) {
    if (!data?.access_token) {
        console.error('[smoke] No token available, skipping iteration');
        return;
    }

    const headers = authHeaders(data.access_token);

    // ─── Auth ───────────────────────────────────────────
    introspect(data.access_token);
    sleep(0.2);

    // ─── Users ──────────────────────────────────────────
    scenarioGetMyProfile(headers);

    // ─── Groups ─────────────────────────────────────────
    scenarioGetMyGroups(headers);

    // ─── Location ───────────────────────────────────────
    scenarioGetAdministrativeLocations(headers);
    scenarioSearchLocations(headers);
    scenarioNearbyLocations(headers);

    // ─── Posts ──────────────────────────────────────────
    scenarioBrowseFeed(headers);

    // ─── Notifications ──────────────────────────────────
    scenarioCheckNotifications(headers);

    // ─── Conversations ──────────────────────────────────
    scenarioGetConversations(headers);

    // ─── User Search ─────────────────────────────────────
    scenarioSearchUsers(headers);

    sleep(1); // Think time between iterations
}

// ──────────────────────────────────────────────────────────────
// TEARDOWN: print summary
// ──────────────────────────────────────────────────────────────
export function teardown(data) {
    console.log('[smoke] Smoke test completed.');
}
