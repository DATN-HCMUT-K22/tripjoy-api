/**
 * TripJoy k6 — SPIKE TEST
 *
 * Purpose: Verify system recovery after sudden massive load jumps.
 * Load:    Instant jump to 300 VUs.
 */

import { sleep, group } from 'k6';
import { stressThresholds } from '../config/thresholds.js';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import {
    scenarioGetMyProfile,
    scenarioSearchUsers,
    scenarioGetMyGroups,
    scenarioCreateGroup,
    scenarioGetGroupMembers,
    scenarioGetGroupSuggestions,
    scenarioCreateSuggestion,
    scenarioDeleteSuggestion,
    scenarioSearchGroups,
    scenarioBrowseFeed,
    scenarioSearchPosts,
    scenarioCreatePost,
    scenarioSavePost,
    scenarioSearchLocations,
    scenarioNearbyLocations,
    scenarioLocationAutocomplete,
    scenarioGetAdministrativeLocations,
    scenarioCreateItinerary,
    scenarioCheckNotifications,
    scenarioGetConversations,
    scenarioReadMessages,
    scenarioSendMessage,
    scenarioSearchMessages,
    scenarioGetUploadSignature,
    scenarioGetPublicProfile,
} from '../lib/scenarios.js';

export const options = {
    scenarios: {
        read: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 120 },
                { duration: '1m',  target: 120 },
                { duration: '10s', target: 0 },
            ],
            exec: 'readScenario',
        },
        manage: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 90 },
                { duration: '1m',  target: 90 },
                { duration: '10s', target: 0 },
            ],
            exec: 'manageScenario',
        },
        social: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 60 },
                { duration: '1m',  target: 60 },
                { duration: '10s', target: 0 },
            ],
            exec: 'socialScenario',
        },
        chat: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 30 },
                { duration: '1m',  target: 30 },
                { duration: '10s', target: 0 },
            ],
            exec: 'chatScenario',
        },
    },
    thresholds: stressThresholds,
    tags: { testType: 'spike', project: 'tripjoy' },
};

export function setup() {
    const r1 = login(env.users.regular.username, env.users.regular.password);
    if (!r1) throw new Error('[spike] Cannot login');
    return { access_token1: r1.access_token };
}

// ──────────────────────────────────────────────────────────────
// ENTRY POINTS
// ──────────────────────────────────────────────────────────────
export function readScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('READ_HEAVY_BROWSING', function () {
        scenarioBrowseFeed(headers);
        scenarioSearchLocations(headers);
    });
    sleep(0.1); // Fast iterations for spike
}

export function manageScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('GROUP_AND_ITINERARY_MGMT', function () {
        scenarioCreateGroup(headers);
    });
    sleep(0.1);
}

export function socialScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('SOCIAL_INTERACTIONS', function () {
        scenarioBrowseFeed(headers);
    });
    sleep(0.1);
}

export function chatScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('CHAT_AND_NOTIFICATIONS', function () {
        scenarioCheckNotifications(headers);
    });
    sleep(0.1);
}

export function teardown(data) {
    console.log('[spike] Spike test completed.');
}

export { handleSummary } from '../lib/summary.js';
