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
import { Trend } from 'k6/metrics';
import http from 'k6/http';
import { env, url } from '../config/environments.js';
import { login, authHeaders } from '../lib/auth.js';
import { post, get, expectStatus, extractData, extractId } from '../lib/http.js';
import { generateItineraryPayload, generateGroupPayload } from '../lib/generators.js';

// ──────────────────────────────────────────────────────────────
// Options — small VU count, permissive thresholds for AI
// ──────────────────────────────────────────────────────────────
const aiGenE2eTrend = new Trend('ai_generation_e2e_duration', true);
const aiChatbotTrend = new Trend('ai_chatbot_response_time', true);

export const options = {
    vus: 3,
    duration: '10m',
    setupTimeout: '10m',
    teardownTimeout: '10m',
    thresholds: {
        // AI endpoints can take up to 150s under concurrent load — reflect reality of LLM processing
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<150000'],
        checks: ['rate>0.80'],  // 80% success is acceptable for AI tests
        ai_generation_e2e_duration: ['p(95)<120000'], // 95% of generations must complete in under 120 seconds (2 minutes)
        ai_chatbot_response_time: ['p(95)<30000'],   // 95% of chatbot responses must be under 30 seconds
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
    let tripItemId = null;
    if (itineraryId) {
        // Dynamically find a valid location ID from search to avoid 404 in setup
        const searchRes = get(url('/locations/search?q=cafe&size=1'), headers, 'GET /locations/search (setup)');
        const locations = extractData(searchRes) || [];
        const locationId = (locations.length > 0) ? locations[0].id : "1509dfcc-aaca-4c4d-8499-6ccd92a2b2de"; // fallback
        
        const itemPayload = {
            location_id: locationId,
            note: "Visit the city center",
            duration: 120,
            start_time: new Date(new Date(itiPayload.start_date + "Z").getTime() + 18000000).toISOString().split('.')[0]
        };
        const itemRes = post(url(`/itineraries/${itineraryId}/items`), itemPayload, headers, 'POST /itineraries/{id}/items (setup)');
        tripItemId = extractId(itemRes);
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
        http.post(url('/itineraries/ai-generate'), JSON.stringify(warmUpPayload), { headers, timeout: '150s' });
    } catch (e) {
        console.warn('[ai-test] Theme seeding warm-up warning:', e);
    }

    // Fetch user conversations list to get a valid conversationId for Chatbot test
    let conversationId = null;
    try {
        const convRes = get(url('/conversations'), headers, 'GET /conversations (setup)');
        const convs = extractData(convRes) || [];
        if (convs.length > 0) {
            conversationId = convs[0].id;
        } else {
            // Search for a user dynamically to create a direct conversation
            const searchRes = get(url('/users/search?q=user&size=5'), headers, 'GET /users/search (setup)');
            const users = extractData(searchRes) || [];
            const targetUser = users.find(u => u.username !== env.users.regular.username);
            if (targetUser) {
                const createRes = post(
                    url('/conversations'), 
                    JSON.stringify({ targetUserId: targetUser.id }), 
                    headers, 
                    'POST /conversations (setup)'
                );
                conversationId = extractId(createRes);
            }
        }
    } catch (e) {
        console.warn('[ai-test] Chatbot conversation setup warning:', e);
    }

    console.log(`[ai-test] Setup complete. groupId=${groupId}, itineraryId=${itineraryId}, conversationId=${conversationId}, tripItemId=${tripItemId}`);
    return { access_token: r.access_token, groupId, itineraryId, conversationId, tripItemId };
}

// ──────────────────────────────────────────────────────────────
// MAIN
// ──────────────────────────────────────────────────────────────
export default function (data) {
    if (!data?.access_token) { sleep(2); return; }

    const headers = authHeaders(data.access_token);
    const roll = Math.random();

    if (roll < 0.25) {
        // Test AI itinerary generation (async — returns 202)
        testAiGenerateItinerary(headers, data.groupId);
    } else if (roll < 0.45) {
        // Test notebook generation on existing itinerary
        testNotebookGeneration(headers, data.itineraryId);
    } else if (roll < 0.60) {
        // Read notebook to confirm generation worked
        testGetNotebook(headers, data.itineraryId);
    } else if (roll < 0.70) {
        // Test AI itinerary modification (synchronous AI call)
        testAiModifyItinerary(headers, data.itineraryId, data.tripItemId);
    } else if (roll < 0.80) {
        // Test AI suggest location (synchronous AI call)
        testAiSuggestLocation(headers, data.itineraryId, data.tripItemId);
    } else {
        // Test AI chatbot E2E conversation response
        testAiChatbot(headers, data.conversationId);
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

    const start = Date.now();
    const res = http.post(
        url('/itineraries/ai-generate'),
        JSON.stringify(payload),
        {
            headers,
            tags: { name: 'POST /itineraries/ai-generate' },
            timeout: '150s',
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
        return;
    }

    // Extract itinerary ID to perform polling and measure true E2E duration
    let itineraryId = null;
    try {
        itineraryId = JSON.parse(res.body).data?.id;
    } catch (e) {
        return;
    }

    if (itineraryId) {
        let status = 'GENERATING';
        let retries = 0;
        const maxRetries = 75; // 75 * 2s = 150s max polling duration
        
        while (status === 'GENERATING' && retries < maxRetries) {
            sleep(2); // poll every 2s
            const pollRes = http.get(url(`/itineraries/${itineraryId}`), { 
                headers, 
                tags: { name: 'GET /itineraries/{id} (polling)' } 
            });
            try {
                const pollBody = JSON.parse(pollRes.body);
                status = pollBody.data?.status || 'FAILED';
            } catch (e) {
                status = 'FAILED';
            }
            retries++;
        }
        
        const durationMs = Date.now() - start;
        
        // Record the E2E duration to our custom Trend metric
        aiGenE2eTrend.add(durationMs);
        
        check(status, {
            'ai-generate: e2e success (DRAFT)': (s) => s === 'DRAFT',
        });
        
        if (status !== 'DRAFT') {
            console.warn(`[ai-test] Itinerary generation failed or timed out. Final status: ${status}`);
        }
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
            timeout: '150s',
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

function testAiModifyItinerary(headers, itineraryId, tripItemId) {
    if (!itineraryId) return;

    // Use dynamically created trip item ID or fallback to standard place_id if unavailable
    const targetPlaceId = tripItemId || "ChIJ0T2NLikpdTERgJJ6o5gX1Kw";

    const payload = {
        unwantedPlaceIds: [targetPlaceId]
    };

    const res = http.post(
        url(`/itineraries/${itineraryId}/ai-modify`),
        JSON.stringify(payload),
        {
            headers,
            tags: { name: 'POST /itineraries/{id}/ai-modify' },
            timeout: '150s',
        }
    );

    check(res, {
        'ai-modify: status 200': (r) => r.status === 200,
        'ai-modify: has data': (r) => {
            try { return !!JSON.parse(r.body).data; } catch { return false; }
        },
    });

    if (res.status !== 200) {
        console.warn(`[ai-test] ai-modify returned ${res.status}: ${res.body?.substring(0, 300)}`);
    }
}

function testAiSuggestLocation(headers, itineraryId, tripItemId) {
    if (!itineraryId) return;

    // Use dynamically created trip item ID or fallback to standard place_id if unavailable
    const targetPlaceId = tripItemId || "ChIJ0T2NLikpdTERgJJ6o5gX1Kw";

    const payload = {
        unwantedPlaceId: targetPlaceId,
        latitude: 10.762622,
        longitude: 106.660172
    };

    const res = http.post(
        url(`/itineraries/${itineraryId}/ai-suggest-location`),
        JSON.stringify(payload),
        {
            headers,
            tags: { name: 'POST /itineraries/{id}/ai-suggest-location' },
            timeout: '150s',
        }
    );

    check(res, {
        'ai-suggest: status 200': (r) => r.status === 200,
        'ai-suggest: has suggestions': (r) => {
            try { return Array.isArray(JSON.parse(r.body).data); } catch { return false; }
        },
    });

    if (res.status !== 200) {
        console.warn(`[ai-test] ai-suggest returned ${res.status}: ${res.body?.substring(0, 300)}`);
    }
}

function testAiChatbot(headers, conversationId) {
    if (!conversationId) return;

    const payload = {
        message_content: '@Tripjoy suggest 3 interesting things to do in Hanoi',
        message_type: 'TEXT'
    };

    const start = Date.now();
    const sendRes = http.post(
        url(`/conversations/${conversationId}/messages`),
        JSON.stringify(payload),
        {
            headers,
            tags: { name: 'POST /conversations/{id}/messages (@Tripjoy)' },
            timeout: '60s',
        }
    );

    check(sendRes, {
        'chatbot-send: status 200': (r) => r.status === 200,
        'chatbot-send: has message id': (r) => {
            try { return !!JSON.parse(r.body).data?.id; } catch { return false; }
        },
    });

    if (sendRes.status !== 200) {
        console.warn(`[ai-test] chatbot-send returned ${sendRes.status}: ${sendRes.body?.substring(0, 300)}`);
        return;
    }

    // Start polling for chatbot response (looking for is_bot === true)
    let replyFound = false;
    let retries = 0;
    const maxRetries = 15; // 15 * 2s = 30s max polling duration
    
    while (!replyFound && retries < maxRetries) {
        sleep(2); // poll every 2s
        const historyRes = http.get(
            url(`/conversations/${conversationId}/messages?limit=10`),
            {
                headers,
                tags: { name: 'GET /conversations/{id}/messages (polling)' }
            }
        );
        
        try {
            const body = JSON.parse(historyRes.body);
            const messages = body.data?.messages || [];
            const botReply = messages.find(m => m.is_bot === true);
            
            if (botReply) {
                const durationMs = Date.now() - start;
                aiChatbotTrend.add(durationMs);
                replyFound = true;
                break;
            }
        } catch (e) {
            // ignore JSON parse errors
        }
        retries++;
    }

    check(replyFound, {
        'chatbot-reply: received bot reply (is_bot)': (f) => f === true,
    });

    if (!replyFound) {
        console.warn(`[ai-test] Chatbot failed to reply within 30s in conversation: ${conversationId}`);
    }
}

// ──────────────────────────────────────────────────────────────
// TEARDOWN
// ──────────────────────────────────────────────────────────────
export function teardown(data) {
    console.log('[ai-test] AI endpoint test completed.');
}

export { handleSummary } from '../lib/summary.js';

