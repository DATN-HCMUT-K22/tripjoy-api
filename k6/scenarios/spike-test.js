/**
 * TripJoy k6 — SPIKE TEST
 *
 * Purpose: Simulate a sudden traffic burst (e.g. viral post, launch day).
 *          Test auto-scaling and queue behavior under instantaneous load.
 * Load:    Jump from 5 → 200 VUs in under 30 seconds, then drop back.
 * Run:     k6 run scenarios/spike-test.js [-e ENV=local]
 */

import { sleep } from 'k6';
import { spikeThresholds } from '../config/thresholds.js';
import { env } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import {
    scenarioBrowseFeed,
    scenarioSearchLocations,
    scenarioGetMyGroups,
    scenarioCheckNotifications,
    scenarioGetConversations,
} from '../lib/scenarios.js';

// ──────────────────────────────────────────────────────────────
// Options
// ──────────────────────────────────────────────────────────────
export const options = {
    stages: [
        { duration: '30s', target: 5   },  // baseline traffic
        { duration: '30s', target: 200 },  // spike!
        { duration: '2m',  target: 200 },  // sustain spike
        { duration: '30s', target: 5   },  // drop back
        { duration: '3m',  target: 5   },  // recovery observation
        { duration: '30s', target: 0   },  // cooldown
    ],
    thresholds: spikeThresholds,
    tags: { testType: 'spike', project: 'tripjoy' },
};

// ──────────────────────────────────────────────────────────────
// SETUP
// ──────────────────────────────────────────────────────────────
export function setup() {
    const r = login(env.users.regular.username, env.users.regular.password);
    if (!r) throw new Error('[spike] Cannot login — aborting');
    return { access_token: r.access_token };
}

// ──────────────────────────────────────────────────────────────
// MAIN: lightweight reads only (spike focuses on throughput)
// ──────────────────────────────────────────────────────────────
export default function (data) {
    if (!data?.access_token) { sleep(0.5); return; }

    const headers = authHeaders(data.access_token);
    const roll = Math.random();

    if (roll < 0.50) {
        scenarioBrowseFeed(headers);
    } else if (roll < 0.75) {
        scenarioSearchLocations(headers);
    } else if (roll < 0.88) {
        scenarioGetMyGroups(headers);
    } else if (roll < 0.94) {
        scenarioCheckNotifications(headers);
    } else {
        scenarioGetConversations(headers);
    }

    sleep(Math.random() * 0.3);
}

export function teardown(data) {
    console.log('[spike] Spike test completed. Check error rate and recovery time.');
}
