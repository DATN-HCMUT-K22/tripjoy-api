/**
 * TripJoy k6 — LOAD TEST
 *
 * Purpose: Simulate realistic concurrent user traffic across all features.
 * Load:    Ramp up to 50 VUs over 3 stages, 20 min total.
 * Run:     k6 run scenarios/load-test.js [-e ENV=local]
 *
 * Traffic Mix (realistic weights):
 *   40% — Read-heavy browsing (feed, locations, search)
 *   30% — Group & itinerary management (core business)
 *   20% — Social interactions (posts, comments, likes)
 *   10% — Chat & notifications (real-time read via REST)
 */

import { sleep } from 'k6';
import { loadThresholds } from '../config/thresholds.js';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import { get, post, expectSuccess, extractData } from '../lib/http.js';
import {
    scenarioGetMyProfile,
    scenarioUpdateProfile,
    scenarioSearchUsers,
    scenarioGetMyGroups,
    scenarioCreateGroup,
    scenarioGetGroupMembers,
    scenarioGetGroupSuggestions,
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
} from '../lib/scenarios.js';

// ──────────────────────────────────────────────────────────────
// Options
// ──────────────────────────────────────────────────────────────
export const options = {
    stages: [
        { duration: '2m', target: 10 },   // ramp up
        { duration: '5m', target: 25 },   // ramp to half load
        { duration: '8m', target: 50 },   // full load plateau
        { duration: '3m', target: 25 },   // scale down
        { duration: '2m', target: 0 },    // cooldown
    ],
    thresholds: loadThresholds,
    tags: { testType: 'load', project: 'tripjoy' },
};

// ──────────────────────────────────────────────────────────────
// SETUP: acquire 2 tokens (user1 & user2 for DM test)
// ──────────────────────────────────────────────────────────────
export function setup() {
    const r1 = login(env.users.regular.username, env.users.regular.password);
    const r2 = login(env.users.regular2.username, env.users.regular2.password);

    if (!r1) throw new Error('[load] Cannot login user1 — aborting');
    if (!r2) console.warn('[load] Cannot login user2 — DM scenario disabled');

    return {
        access_token1: r1.access_token,
        access_token2: r2 ? r2.access_token : null,
    };
}

// ──────────────────────────────────────────────────────────────
// MAIN: weighted scenario distribution
// ──────────────────────────────────────────────────────────────
export default function (data) {
    if (!data?.access_token1) { sleep(1); return; }

    const access_token = data.access_token1;
    const headers = authHeaders(access_token);
    const roll = Math.random();

    if (roll < 0.40) {
        // ── 40%: READ-HEAVY BROWSING ─────────────────────────
        readHeavyJourney(headers);
    } else if (roll < 0.70) {
        // ── 30%: GROUP & ITINERARY MANAGEMENT ────────────────
        groupItineraryJourney(headers);
    } else if (roll < 0.90) {
        // ── 20%: SOCIAL INTERACTIONS ─────────────────────────
        socialJourney(headers);
    } else {
        // ── 10%: CHAT & NOTIFICATIONS ─────────────────────────
        chatNotifJourney(headers, data);
    }

    sleep(Math.random() * 2 + 0.5); // Think time 0.5–2.5s
}

// ──────────────────────────────────────────────────────────────
// JOURNEY FUNCTIONS
// ──────────────────────────────────────────────────────────────

function readHeavyJourney(headers) {
    const step = Math.floor(Math.random() * 5);
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
            scenarioSearchUsers(headers);
            scenarioGetMyProfile(headers);
            break;
        case 4:
            scenarioSearchGroups(headers);
            scenarioGetMyGroups(headers);
            break;
    }
}

function groupItineraryJourney(headers) {
    // Create group → create itinerary inside group
    const groupId = scenarioCreateGroup(headers);
    sleep(0.5);
    if (groupId) {
        scenarioGetGroupMembers(headers, groupId);
        sleep(0.3);
        scenarioGetGroupSuggestions(headers, groupId);
        sleep(0.3);
        const itineraryId = scenarioCreateItinerary(headers, groupId);
        if (itineraryId) {
            sleep(0.3);
            // Notebook check removed from general load test (has separate ai-test.js)
        }
    }
}

function socialJourney(headers) {
    // Browse feed
    scenarioBrowseFeed(headers);
    sleep(0.4);

    // Pick an itinerary to link the post to (mandatory)
    const itisRes = get(url('/itineraries/me'), headers, 'GET /itineraries/me');
    const itis = extractData(itisRes);
    let itineraryId = (itis && itis.length > 0) ? itis[0].id : null;

    // If user has no itineraries, create one quickly
    if (!itineraryId) {
        const groupId = scenarioCreateGroup(headers);
        if (groupId) {
            itineraryId = scenarioCreateItinerary(headers, groupId);
        }
    }

    const postId = scenarioCreatePost(headers, itineraryId);
    if (postId) {
        sleep(0.3);
        scenarioSavePost(headers, postId);
    }
    sleep(0.3);
    scenarioGetUploadSignature(headers);
}

function chatNotifJourney(headers, data) {
    scenarioCheckNotifications(headers);
    sleep(0.3);
    const conversations = scenarioGetConversations(headers);
    sleep(0.3);

    // If we have conversations, interact with the first one
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
}

// ──────────────────────────────────────────────────────────────
// TEARDOWN
// ──────────────────────────────────────────────────────────────
export function teardown(data) {
    console.log('[load] Load test completed.');
}

export { handleSummary } from '../lib/summary.js';
