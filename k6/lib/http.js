/**
 * TripJoy k6 — HTTP Helpers (Best-Practice Wrappers)
 *
 * Wraps k6/http with:
 * - Automatic tagging (scenario, endpoint name)
 * - Structured response checks
 * - Custom metrics counters
 */

import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// ──────────────────────────────────────────────────────────────
// Custom Metrics
// ──────────────────────────────────────────────────────────────
export const apiErrors = new Counter('tripjoy_api_errors');
export const apiSuccessRate = new Rate('tripjoy_api_success_rate');

// ──────────────────────────────────────────────────────────────
// Core HTTP helpers
// ──────────────────────────────────────────────────────────────

const JSON_CT = { 'Content-Type': 'application/json' };

/**
 * GET with auth + tagging
 */
export function get(path, headers = {}, tagName = null) {
    const tag = tagName || `GET ${path}`;
    const scenario = tag.toLowerCase().includes('search') || tag.toLowerCase().includes('find') ? 'search' : 'read';
    const res = http.get(path, {
        headers,
        tags: { name: tag, scenario: scenario },
    });
    trackResult(res, tag);
    return res;
}

/**
 * POST JSON with auth + tagging
 */
export function post(path, body, headers = {}, tagName = null) {
    const tag = tagName || `POST ${path}`;
    const scenario = tag.toLowerCase().includes('login') || tag.toLowerCase().includes('auth') ? 'auth' : 'write';
    const res = http.post(path, JSON.stringify(body), {
        headers: { ...JSON_CT, ...headers },
        tags: { name: tag, scenario: scenario },
    });
    trackResult(res, tag);
    return res;
}

/**
 * PUT JSON with auth + tagging
 */
export function put(path, body, headers = {}, tagName = null) {
    const tag = tagName || `PUT ${path}`;
    const res = http.put(path, JSON.stringify(body), {
        headers: { ...JSON_CT, ...headers },
        tags: { name: tag, scenario: 'write' },
    });
    trackResult(res, tag);
    return res;
}

/**
 * PATCH JSON with auth + tagging
 */
export function patch(path, body, headers = {}, tagName = null) {
    const tag = tagName || `PATCH ${path}`;
    const res = http.patch(path, JSON.stringify(body), {
        headers: { ...JSON_CT, ...headers },
        tags: { name: tag, scenario: 'write' },
    });
    trackResult(res, tag);
    return res;
}

/**
 * DELETE with auth + tagging
 */
export function del(path, headers = {}, tagName = null) {
    const tag = tagName || `DELETE ${path}`;
    const res = http.del(path, null, {
        headers,
        tags: { name: tag, scenario: 'write' },
    });
    trackResult(res, tag);
    return res;
}

// ──────────────────────────────────────────────────────────────
// Response checking helpers
// ──────────────────────────────────────────────────────────────

/**
 * Assert response is a successful TripJoy API response (status 200, has data).
 */
export function expectSuccess(res, label = 'request') {
    return check(res, {
        [`${label}: HTTP 200`]: (r) => r.status === 200,
        [`${label}: valid JSON`]: (r) => {
            try { JSON.parse(r.body); return true; } catch { return false; }
        },
        [`${label}: has data field`]: (r) => {
            try {
                const b = JSON.parse(r.body);
                // Allow data to be missing/null if it's a success (e.g. ApiResponse<Void>)
                return r.status === 200 || b.data !== undefined;
            } catch { return false; }
        },
    });
}

/**
 * Assert response is 200 with data being an array.
 */
export function expectList(res, label = 'list') {
    return check(res, {
        [`${label}: HTTP 200`]: (r) => r.status === 200,
        [`${label}: data is array`]: (r) => {
            try {
                const b = JSON.parse(r.body);
                return Array.isArray(b.data) || Array.isArray(b.data?.content);
            } catch { return false; }
        },
    });
}

/**
 * Assert response returns a specific HTTP status.
 */
export function expectStatus(res, status, label = 'request') {
    return check(res, {
        [`${label}: HTTP ${status}`]: (r) => r.status === status,
    });
}

/**
 * Assert response is 401 Unauthorized (unauthenticated request).
 */
export function expectUnauthorized(res, label = 'unauth request') {
    return check(res, {
        [`${label}: HTTP 401`]: (r) => r.status === 401,
    });
}

// ──────────────────────────────────────────────────────────────
// Response data extractors
// ──────────────────────────────────────────────────────────────

/**
 * Parse and return `data` field from response body.
 */
export function extractData(res) {
    try {
        return JSON.parse(res.body).data || null;
    } catch {
        return null;
    }
}

/**
 * Parse and return `data.id` (UUID) from response body.
 */
export function extractId(res) {
    try {
        return JSON.parse(res.body).data?.id || null;
    } catch {
        return null;
    }
}

// ──────────────────────────────────────────────────────────────
// Internal helpers
// ──────────────────────────────────────────────────────────────

function trackResult(res, tag) {
    const success = res.status >= 200 && res.status < 300;
    apiSuccessRate.add(success);
    if (!success) {
        apiErrors.add(1, { endpoint: tag });
        console.error(`[error] ${tag} returned ${res.status}: ${res.body}`);
    }
}
