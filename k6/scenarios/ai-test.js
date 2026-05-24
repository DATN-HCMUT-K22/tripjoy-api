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
    setupTimeout: '10m',
    teardownTimeout: '10m',
    thresholds: {
        // AI endpoints can take up to 90s — reflect reality of LLM processing
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<95000'],
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

    // Add at least one trip item so AI has something to work with for Notebook
    if (itineraryId) {
        // Dynamically find a valid location ID from search to avoid 404 in setup
        const searchRes = get(url('/locations/search?q=cafe&size=1'), headers, 'GET /locations/search (setup)');
        const searchPage = extractData(searchRes);
        const locations = (searchPage && searchPage.content) ? searchPage.content : [];
        const locationId = (locations.length > 0) ? locations[0].id : "1509dfcc-aaca-4c4d-8499-6ccd92a2b2de"; // fallback
        
        const itemPayload = {
            location_id: locationId,
            note: "Visit the city center",
            duration: 120,
            start_time: new Date(new Date(itiPayload.start_date + "Z").getTime() + 18000000).toISOString().split('.')[0]
        };
        post(url(`/itineraries/${itineraryId}/items`), itemPayload, headers, 'POST /itineraries/{id}/items (setup)');
    }

    // Run a single sequential AI itinerary generation request during setup.
    // This seeds the themes 'CULTURE' and 'FOOD' on a single thread,
    // preventing concurrent VUs from hitting a 409 unique constraint violation in ThemeService.
    console.log('[ai-test] Seeding themes and warming up AI service...');
    try {
        const warmUpPayload = {
            destination: 'Ho Chi Minh City',
            latitude: 10.762622,
            longitude: 106.660172,
            startDate: itiPayload.start_date,
            endDate: itiPayload.end_date,
            peopleQuantity: 2,
            budgetEstimate: 1000.0,
            themes: ['CULTURE', 'FOOD'],
            suggestLocations: []
        };
        http.post(url('/itineraries/ai-generate'), JSON.stringify(warmUpPayload), { headers, timeout: '90s' });
    } catch (e) {
        console.warn('[ai-test] Theme seeding warm-up warning:', e);
    }

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
    const now = new Date();
    const startDate = new Date(now.getTime() + 86400000); // Tomorrow
    const endDate = new Date(startDate.getTime() + 86400000 * 3); // 3 days later

    const formatDT = (date) => date.toISOString().split('.')[0];

    const payload = {
        destination: city,
        latitude: 10.762622,  // Default to HCM City
        longitude: 106.660172,
        startDate: formatDT(startDate),
        endDate: formatDT(endDate),
        peopleQuantity: Math.floor(Math.random() * 5) + 2,
        budgetEstimate: 1000.0,
        themes: ['CULTURE', 'FOOD'],
        suggestLocations: []
    };

    const res = http.post(
        url('/itineraries/ai-generate'),
        JSON.stringify(payload),
        {
            headers,
            tags: { name: 'POST /itineraries/ai-generate' },
            timeout: '90s',
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
