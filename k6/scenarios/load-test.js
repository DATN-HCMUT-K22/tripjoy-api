/**
 * TripJoy k6 — LOAD TEST
 *
 * Purpose: Simulate realistic concurrent user traffic across all features.
 * Load:    Ramp up to 50 VUs over 3 stages, 5 min total.
 */

import { sleep, group } from 'k6';
import { loadThresholds } from '../config/thresholds.js';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import { get, post, expectSuccess, extractData } from '../lib/http.js';
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

// ──────────────────────────────────────────────────────────────
// Options
// ──────────────────────────────────────────────────────────────
export const options = {
    scenarios: {
        read: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 20 }, // 40% of 50
                { duration: '3m', target: 20 },
                { duration: '1m', target: 0 },
            ],
            exec: 'readScenario',
        },
        manage: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 15 }, // 30% of 50
                { duration: '3m', target: 15 },
                { duration: '1m', target: 0 },
            ],
            exec: 'manageScenario',
        },
        social: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 10 }, // 20% of 50
                { duration: '3m', target: 10 },
                { duration: '1m', target: 0 },
            ],
            exec: 'socialScenario',
        },
        chat: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 5 }, // 10% of 50
                { duration: '3m', target: 5 },
                { duration: '1m', target: 0 },
            ],
            exec: 'chatScenario',
        },
    },
    thresholds: loadThresholds,
    tags: { testType: 'load', project: 'tripjoy' },
};

// ──────────────────────────────────────────────────────────────
// SETUP
// ──────────────────────────────────────────────────────────────
export function setup() {
    const r1 = login(env.users.regular.username, env.users.regular.password);
    const r2 = login(env.users.regular2.username, env.users.regular2.password);
    if (!r1) throw new Error('[load] Cannot login user1');
    return {
        access_token1: r1.access_token,
        access_token2: r2 ? r2.access_token : null,
    };
}

// ──────────────────────────────────────────────────────────────
// ENTRY POINTS FOR SCENARIOS
// ──────────────────────────────────────────────────────────────
export function readScenario(data) {
    const headers = authHeaders(data.access_token1);
    readHeavyJourney(headers);
    sleep(Math.random() * 2 + 1);
}

export function manageScenario(data) {
    const headers = authHeaders(data.access_token1);
    groupItineraryJourney(headers);
    sleep(Math.random() * 2 + 1);
}

export function socialScenario(data) {
    const headers = authHeaders(data.access_token1);
    socialJourney(headers);
    sleep(Math.random() * 2 + 1);
}

export function chatScenario(data) {
    const headers = authHeaders(data.access_token1);
    chatNotifJourney(headers, data);
    sleep(Math.random() * 2 + 1);
}

// ──────────────────────────────────────────────────────────────
// JOURNEY FUNCTIONS
// ──────────────────────────────────────────────────────────────

function readHeavyJourney(headers) {
    group('READ_HEAVY_BROWSING', function () {
        const step = Math.floor(Math.random() * 6);
        switch (step) {
            case 0:
                scenarioBrowseFeed(headers);
                scenarioSearchPosts(headers);
                break;
            case 1:
                scenarioSearchLocations(headers);
                scenarioNearbyLocations(headers);
                break;
            case 2:
                scenarioLocationAutocomplete(headers);
                scenarioGetAdministrativeLocations(headers);
                break;
            case 3:
                const users = scenarioSearchUsers(headers);
                scenarioGetMyProfile(headers);
                if (Array.isArray(users) && users.length > 0) {
                    scenarioGetPublicProfile(headers, users[0].id);
                }
                break;
            case 4:
                scenarioSearchGroups(headers);
                scenarioGetMyGroups(headers);
                break;
            case 5:
                // Fallback search + view
                const uList = scenarioSearchUsers(headers);
                if (Array.isArray(uList) && uList.length > 0) {
                    scenarioGetPublicProfile(headers, uList[0].id);
                }
                break;
        }
    });
}

function groupItineraryJourney(headers) {
    group('GROUP_AND_ITINERARY_MGMT', function () {
        // Create group → create itinerary inside group
        const groupId = scenarioCreateGroup(headers);
        sleep(0.5);
        if (groupId) {
            scenarioGetGroupMembers(headers, groupId);
            sleep(0.3);
            
            // Suggestion flow
            scenarioGetGroupSuggestions(headers, groupId);
            if (Math.random() < 0.5) {
                const sid = scenarioCreateSuggestion(headers, groupId);
                if (sid && Math.random() < 0.3) {
                    scenarioDeleteSuggestion(headers, groupId, sid);
                }
            }

            sleep(0.3);
            const itineraryId = scenarioCreateItinerary(headers, groupId);
            if (itineraryId) {
                sleep(0.3);
            }
        }
    });
}

function socialJourney(headers) {
    group('SOCIAL_INTERACTIONS', function () {
        const roll = Math.random();
        if (roll < 0.7) {
            // 70% just browse feed and look at profiles
            scenarioBrowseFeed(headers);
            sleep(0.4);
            const uList = scenarioSearchUsers(headers);
            if (Array.isArray(uList) && uList.length > 0) {
                scenarioGetPublicProfile(headers, uList[0].id);
            }
        } else {
            // 30% create posts and interact
            const itisRes = get(url('/itineraries/me'), headers, 'GET /itineraries/me');
            const itis = extractData(itisRes);
            let itineraryId = (itis && itis.length > 0) ? itis[0].id : null;

            if (!itineraryId) {
                const groupId = scenarioCreateGroup(headers);
                if (groupId) itineraryId = scenarioCreateItinerary(headers, groupId);
            }

            const postId = scenarioCreatePost(headers, itineraryId);
            if (postId) {
                sleep(0.3);
                scenarioSavePost(headers, postId);
            }
        }
        sleep(0.3);
        scenarioGetUploadSignature(headers);
    });
}

function chatNotifJourney(headers, data) {
    group('CHAT_AND_NOTIFICATIONS', function () {
        scenarioCheckNotifications(headers);
        sleep(0.3);
        const conversations = scenarioGetConversations(headers);
        sleep(0.3);

        if (Array.isArray(conversations) && conversations.length > 0) {
            const convId = conversations[0].id;
            if (convId) {
                scenarioReadMessages(headers, convId);
                sleep(0.3);
                scenarioSendMessage(headers, convId);
            }
        }

        sleep(0.3);
        scenarioSearchMessages(headers);
    });
}

// ──────────────────────────────────────────────────────────────
// TEARDOWN
// ──────────────────────────────────────────────────────────────
export function teardown(data) {
    console.log('[load] Load test completed.');
}

export { handleSummary } from '../lib/summary.js';
