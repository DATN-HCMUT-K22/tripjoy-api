/**
 * TripJoy k6 — STRESS TEST
 *
 * Purpose: Find the breaking point — ramp until degradation/failure is observed.
 * Load:    Ramp to 200 VUs aggressively, then spike, then verify recovery.
 * Run:     k6 run scenarios/stress-test.js [-e ENV=local]
 *
 * Focus:   High-impact endpoints only (not full journeys, to maximize concurrency).
 *          - Auth (rate limiting, token blacklist)
 *          - Search (DB/index pressure)
 *          - Group/Itinerary writes (transaction throughput)
 *          - Chat history (cursor pagination under load)
 */

import { sleep, check } from 'k6';
import http from 'k6/http';
import { stressThresholds } from '../config/thresholds.js';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import {
    scenarioBrowseFeed,
    scenarioSearchLocations,
    scenarioNearbyLocations,
    scenarioGetMyGroups,
    scenarioCreateGroup,
    scenarioCheckNotifications,
    scenarioGetConversations,
    scenarioSearchMessages,
    scenarioSearchPosts,
    scenarioSearchUsers,
    scenarioGetAdministrativeLocations,
    scenarioGetUploadSignature,
} from '../lib/scenarios.js';
import { Counter } from 'k6/metrics';

const stressErrors = new Counter('stress_errors');

// ──────────────────────────────────────────────────────────────
// Options
// ──────────────────────────────────────────────────────────────
export const options = {
    stages: [
        { duration: '1m',  target: 20  },  // warm up
        { duration: '2m',  target: 50  },  // approach normal load
        { duration: '3m',  target: 100 },  // double capacity
        { duration: '3m',  target: 150 },  // stress zone
        { duration: '3m',  target: 200 },  // breaking point
        { duration: '2m',  target: 50  },  // ramp down
        { duration: '3m',  target: 50  },  // recovery verification
        { duration: '1m',  target: 0   },  // cooldown
    ],
    thresholds: stressThresholds,
    tags: { testType: 'stress', project: 'tripjoy' },
};

// ──────────────────────────────────────────────────────────────
// SETUP
// ──────────────────────────────────────────────────────────────
export function setup() {
    const r1 = login(env.users.regular.username, env.users.regular.password);
    if (!r1) throw new Error('[stress] Cannot login — aborting stress test');
    return { access_token: r1.access_token };
}

// ──────────────────────────────────────────────────────────────
// MAIN: focused high-concurrency scenarios
// ──────────────────────────────────────────────────────────────
export default function (data) {
    if (!data?.access_token) { sleep(1); return; }

    const headers = authHeaders(data.access_token);
    const roll = Math.random();

    // Weight distribution for stress (favour read endpoints that stress DB/cache)
    if (roll < 0.30) {
        // 30%: location search (PostGIS + FTS pressure)
        scenarioSearchLocations(headers);
        sleep(0.1);
        scenarioNearbyLocations(headers);

    } else if (roll < 0.50) {
        // 20%: post feed + search (pagination pressure)
        scenarioBrowseFeed(headers);
        sleep(0.1);
        scenarioSearchPosts(headers);

    } else if (roll < 0.65) {
        // 15%: user + group listing (Redis cache hit/miss)
        scenarioSearchUsers(headers);
        sleep(0.1);
        scenarioGetMyGroups(headers);

    } else if (roll < 0.78) {
        // 13%: write load — group creation (DB write + transaction)
        scenarioCreateGroup(headers);

    } else if (roll < 0.88) {
        // 10%: notification + conversation reads
        scenarioCheckNotifications(headers);
        sleep(0.1);
        scenarioGetConversations(headers);

    } else if (roll < 0.94) {
        // 6%: message search (FTS pressure)
        scenarioSearchMessages(headers);

    } else {
        // 6%: admin/heavy endpoints
        scenarioGetAdministrativeLocations(headers);
        sleep(0.1);
        scenarioGetUploadSignature(headers);
    }

    // Minimal think time under stress
    sleep(Math.random() * 0.5);
}

// ──────────────────────────────────────────────────────────────
// TEARDOWN
// ──────────────────────────────────────────────────────────────
export function teardown(data) {
    console.log('[stress] Stress test completed. Review p(95) and error rate for breaking point.');
}
