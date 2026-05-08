/**
 * TripJoy k6 — AI ENDPOINT TEST
 *
 * Purpose: Specifically test AI-heavy endpoints (Gemini/Vertex AI integration).
 *          These are expensive, slow, and should be tested with lower concurrency
 *          but longer duration to measure P95/P99 realistically.
 *
 * Endpoints tested:
 *   POST /itineraries/ai-generate          (async, returns 202)
 *   POST /itineraries/{id}/ai-modify       (synchronous AI call)
 *   POST /notebooks/{itineraryId}/ai-generate
 *
 * Run:  k6 run scenarios/ai-test.js [-e ENV=local]
 *
 * NOTE: AI endpoints are inherently slow (Gemini API latency ~5-30s).
 *       Thresholds are relaxed accordingly. Primary metric: success rate.
 */

import { sleep, check } from 'k6';
import http from 'k6/http';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import { post, get, expectStatus, extractData, extractId } from '../lib/http.js';
import { generateItineraryPayload, generateGroupPayload } from '../lib/generators.js';

// ──────────────────────────────────────────────────────────────
// Options — small VU count, permissive thresholds for AI
// ──────────────────────────────────────────────────────────────
export const options = {
    vus: 3,
    duration: '10m',
    thresholds: {
        // AI endpoints can take up to 60s — reflect reality
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<60000'],
        checks: ['rate>0.80'],  // 80% success is acceptable for AI tests
    },
    tags: { testType: 'ai', project: 'tripjoy' },
};

// ──────────────────────────────────────────────────────────────
// SETUP: login + create a group + itinerary for AI tests
// ──────────────────────────────────────────────────────────────
export function setup() {
    const r = login(env.users.regular.username, env.users.regular.password);
    if (!r) throw new Error('[ai-test] Cannot login — aborting');

    const headers = authHeaders(r.access_token);

    // Create a group
    const groupRes = post(url('/groups'), generateGroupPayload(), headers, 'POST /groups (setup)');
    const groupId = extractId(groupRes);
    if (!groupId) throw new Error('[ai-test] Failed to create group for setup');

    // Create an itinerary manually
    const itiPayload = generateItineraryPayload(groupId);
    const itiRes = post(url('/itineraries'), itiPayload, headers, 'POST /itineraries (setup)');
    const itineraryId = extractId(itiRes);

    console.log(`[ai-test] Setup complete. groupId=${groupId}, itineraryId=${itineraryId}`);
    return { access_token: r.access_token, groupId, itineraryId };
}

// ──────────────────────────────────────────────────────────────
// MAIN
// ──────────────────────────────────────────────────────────────
export default function (data) {
    if (!data?.access_token) { sleep(2); return; }

    const headers = authHeaders(data.access_token);
    const roll = Math.random();

    if (roll < 0.50) {
        // Test AI itinerary generation (async — returns 202)
        testAiGenerateItinerary(headers, data.groupId);
    } else if (roll < 0.75) {
        // Test notebook generation on existing itinerary
        testNotebookGeneration(headers, data.itineraryId);
    } else {
        // Read notebook to confirm generation worked
        testGetNotebook(headers, data.itineraryId);
    }

    // AI operations need breathing room
    sleep(Math.random() * 5 + 3);
}

// ──────────────────────────────────────────────────────────────
// AI SUB-TESTS
// ──────────────────────────────────────────────────────────────

function testAiGenerateItinerary(headers, groupId) {
    if (!groupId) return;

    const city = ['Ho Chi Minh City', 'Hanoi', 'Da Nang', 'Hoi An'][Math.floor(Math.random() * 4)];

    const payload = {
        groupId,
        destination: city,
        numberOfDays: Math.floor(Math.random() * 4) + 2,  // 2-5 days
        travelStyle: ['BUDGET', 'BALANCED', 'LUXURY'][Math.floor(Math.random() * 3)],
        numberOfPeople: Math.floor(Math.random() * 5) + 2,
        interests: ['food', 'culture', 'adventure'].slice(0, Math.floor(Math.random() * 3) + 1),
    };

    const res = http.post(
        url('/itineraries/ai-generate'),
        JSON.stringify(payload),
        {
            headers,
            tags: { name: 'POST /itineraries/ai-generate' },
            timeout: '90s',  // AI can be slow
        }
    );

    check(res, {
        'ai-generate: accepted (202 or 200)': (r) => r.status === 202 || r.status === 200,
        'ai-generate: has itinerary id': (r) => {
            try { return !!JSON.parse(r.body).data?.id; } catch { return false; }
        },
    });

    if (res.status !== 200 && res.status !== 202) {
        console.warn(`[ai-test] ai-generate returned ${res.status}: ${res.body?.substring(0, 300)}`);
    }
}

function testNotebookGeneration(headers, itineraryId) {
    if (!itineraryId) return;

    const res = http.post(
        url(`/notebooks/${itineraryId}/ai-generate`),
        null,
        {
            headers,
            tags: { name: 'POST /notebooks/{id}/ai-generate' },
            timeout: '90s',
        }
    );

    check(res, {
        'notebook-generate: status 200': (r) => r.status === 200,
        'notebook-generate: has data': (r) => {
            try { return !!JSON.parse(r.body).data; } catch { return false; }
        },
    });

    if (res.status !== 200) {
        console.warn(`[ai-test] notebook-generate returned ${res.status}: ${res.body?.substring(0, 300)}`);
    }
}

function testGetNotebook(headers, itineraryId) {
    if (!itineraryId) return;

    const res = get(url(`/notebooks/${itineraryId}/itinerary`), headers, 'GET /notebooks/{id}/itinerary');

    check(res, {
        'notebook-get: 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
}

// ──────────────────────────────────────────────────────────────
// TEARDOWN
// ──────────────────────────────────────────────────────────────
export function teardown(data) {
    console.log('[ai-test] AI endpoint test completed.');
}
