/**
 * TripJoy k6 — SOAK TEST
 *
 * Purpose: Verify system stability over an extended period.
 * Load:    50 VUs sustained for 30 minutes.
 */

import { sleep, group } from 'k6';
import { soakThresholds } from '../config/thresholds.js';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import { get, extractData } from '../lib/http.js';
import { cleanupTestData } from '../lib/cleanup.js';
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
        read: { executor: 'constant-vus', vus: 20, duration: '30m', exec: 'readScenario' },
        manage: { executor: 'constant-vus', vus: 15, duration: '30m', exec: 'manageScenario' },
        social: { executor: 'constant-vus', vus: 10, duration: '30m', exec: 'socialScenario' },
        chat: { executor: 'constant-vus', vus: 5, duration: '30m', exec: 'chatScenario' },
    },
    setupTimeout: '10m',
    teardownTimeout: '10m',
    thresholds: soakThresholds,
    tags: { testType: 'soak', project: 'tripjoy' },
};

export function setup() {
    const r1 = login(env.users.regular.username, env.users.regular.password);
    if (!r1) throw new Error('[soak] Cannot login');
    // Clean old database test data before soak test starts
    cleanupTestData(r1.access_token);
    return { access_token1: r1.access_token };
}

// ──────────────────────────────────────────────────────────────
// ENTRY POINTS
// ──────────────────────────────────────────────────────────────
export function readScenario(data) {
    const headers = authHeaders(data.access_token1);
    const step = Math.floor(Math.random() * 6);
    group('READ_HEAVY_BROWSING', function () {
        switch (step) {
            case 0: scenarioBrowseFeed(headers); break;
            case 1: scenarioSearchLocations(headers); break;
            case 2: scenarioLocationAutocomplete(headers); break;
            case 3: scenarioSearchUsers(headers); break;
            case 4: scenarioSearchGroups(headers); break;
            case 5: scenarioGetPublicProfile(headers, '00000000-0000-0000-0000-000000000001'); break;
        }
    });
    sleep(Math.random() * 2 + 1);
}

export function manageScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('GROUP_AND_ITINERARY_MGMT', function () {
        const groupId = scenarioCreateGroup(headers);
        if (groupId) {
            scenarioCreateSuggestion(headers, groupId);
            scenarioCreateItinerary(headers, groupId);
        }
    });
    sleep(Math.random() * 2 + 1);
}

export function socialScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('SOCIAL_INTERACTIONS', function () {
        scenarioBrowseFeed(headers);
        
        // Correctly fetch itineraries instead of groups
        const itisRes = get(url('/itineraries/me'), headers, 'GET /itineraries/me');
        const itis = extractData(itisRes);
        let itId = (itis && itis.length > 0) ? itis[0].id : null;

        // Fallback: If no itinerary exists, create a group and an itinerary for the test
        if (!itId) {
            const groupId = scenarioCreateGroup(headers);
            if (groupId) {
                itId = scenarioCreateItinerary(headers, groupId);
            }
        }

        if (itId) {
            const postId = scenarioCreatePost(headers, itId);
            if (postId) {
                scenarioSavePost(headers, postId);
            }
        }
    });
    sleep(Math.random() * 2 + 1);
}

export function chatScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('CHAT_AND_NOTIFICATIONS', function () {
        scenarioCheckNotifications(headers);
        const convs = scenarioGetConversations(headers);
        if (convs && convs.length > 0) {
            scenarioReadMessages(headers, convs[0].id);
            scenarioSendMessage(headers, convs[0].id);
        }
    });
    sleep(Math.random() * 2 + 1);
}

export function teardown(data) {
    console.log('[soak] Soak test completed.');
    // Clean database test data generated during this soak test
    if (data?.access_token1) {
        cleanupTestData(data.access_token1);
    }
}

export { handleSummary } from '../lib/summary.js';
