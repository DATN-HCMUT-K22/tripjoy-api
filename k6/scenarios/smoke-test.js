/**
 * TripJoy k6 — SMOKE TEST
 *
 * Purpose: Verify the system is alive and all critical endpoints respond.
 * Load:    1 VU per scenario, 2 minutes.
 */

import { sleep, group } from 'k6';
import { smokeThresholds } from '../config/thresholds.js';
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
        read: { executor: 'constant-vus', vus: 1, duration: '2m', exec: 'readScenario' },
        manage: { executor: 'constant-vus', vus: 1, duration: '2m', exec: 'manageScenario' },
        social: { executor: 'constant-vus', vus: 1, duration: '2m', exec: 'socialScenario' },
        chat: { executor: 'constant-vus', vus: 1, duration: '2m', exec: 'chatScenario' },
    },
    thresholds: smokeThresholds,
    tags: { testType: 'smoke', project: 'tripjoy' },
};

export function setup() {
    const r1 = login(env.users.regular.username, env.users.regular.password);
    if (!r1) throw new Error('[smoke] Cannot login');
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
    sleep(1);
}

export function manageScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('GROUP_AND_ITINERARY_MGMT', function () {
        scenarioGetMyGroups(headers);
    });
    sleep(1);
}

export function socialScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('SOCIAL_INTERACTIONS', function () {
        scenarioBrowseFeed(headers);
    });
    sleep(1);
}

export function chatScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('CHAT_AND_NOTIFICATIONS', function () {
        scenarioCheckNotifications(headers);
    });
    sleep(1);
}

export function teardown(data) {
    console.log('[smoke] Smoke test completed.');
}

export { handleSummary } from '../lib/summary.js';
