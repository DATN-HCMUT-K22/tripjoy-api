/**
 * TripJoy k6 — STRESS TEST (Extremely High Load)
 *
 * Purpose: Find the absolute breaking point — ramp to 500 VUs.
 * Intensity: Increased by reducing sleep intervals.
 */

import { sleep, group } from 'k6';
import { stressThresholds } from '../config/thresholds.js';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import {
    scenarioSearchUsers,
    scenarioCreateGroup,
    scenarioCreateSuggestion,
    scenarioSearchGroups,
    scenarioBrowseFeed,
    scenarioCreatePost,
    scenarioSavePost,
    scenarioSearchLocations,
    scenarioLocationAutocomplete,
    scenarioCreateItinerary,
    scenarioCheckNotifications,
    scenarioGetConversations,
    scenarioReadMessages,
    scenarioSendMessage,
    scenarioGetPublicProfile,
} from '../lib/scenarios.js';

export const options = {
    scenarios: {
        read: {
            executor: 'ramping-vus',
            stages: [
                { duration: '1m', target: 200 }, // Fast ramp
                { duration: '3m', target: 200 },
                { duration: '1m', target: 0 },
            ],
            exec: 'readScenario',
        },
        manage: {
            executor: 'ramping-vus',
            stages: [
                { duration: '1m', target: 150 },
                { duration: '3m', target: 150 },
                { duration: '1m', target: 0 },
            ],
            exec: 'manageScenario',
        },
        social: {
            executor: 'ramping-vus',
            stages: [
                { duration: '1m', target: 100 },
                { duration: '3m', target: 100 },
                { duration: '1m', target: 0 },
            ],
            exec: 'socialScenario',
        },
        chat: {
            executor: 'ramping-vus',
            stages: [
                { duration: '1m', target: 50 },
                { duration: '3m', target: 50 },
                { duration: '1m', target: 0 },
            ],
            exec: 'chatScenario',
        },
    },
    thresholds: stressThresholds,
    tags: { testType: 'extreme-stress', project: 'tripjoy' },
};

export function setup() {
    const r1 = login(env.users.regular.username, env.users.regular.password);
    if (!r1) throw new Error('[stress] Cannot login');
    return { access_token1: r1.access_token };
}

export function readScenario(data) {
    const headers = authHeaders(data.access_token1);
    const step = Math.floor(Math.random() * 5);
    group('READ_HEAVY_BROWSING', function () {
        switch (step) {
            case 0: scenarioBrowseFeed(headers); break;
            case 1: scenarioSearchLocations(headers); break;
            case 2: scenarioLocationAutocomplete(headers); break;
            case 3: scenarioSearchUsers(headers); break;
            case 4: scenarioSearchGroups(headers); break;
        }
    });
    sleep(Math.random() * 0.5 + 0.1); // Fast pace
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
    sleep(Math.random() * 0.5 + 0.1);
}

export function socialScenario(data) {
    const headers = authHeaders(data.access_token1);
    group('SOCIAL_INTERACTIONS', function () {
        const feed = scenarioBrowseFeed(headers);
        let itiId = null;
        // Try to find a valid itinerary from the feed to link to the post
        if (feed && feed.data && feed.data.length > 0) {
            itiId = feed.data[0].itineraryId || feed.data[0].id;
        }
        
        if (itiId) {
            const postId = scenarioCreatePost(headers, itiId);
            if (postId) scenarioSavePost(headers, postId);
        }
    });
    sleep(Math.random() * 0.5 + 0.1);
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
    sleep(Math.random() * 0.5 + 0.1);
}

export function teardown(data) {
    console.log('[stress] Extreme stress test completed.');
}

export { handleSummary } from '../lib/summary.js';
