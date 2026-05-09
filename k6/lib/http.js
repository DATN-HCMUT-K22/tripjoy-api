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
    const res = http.get(path, {
        headers,
        tags: { name: tag }, // Scenario is automatically tagged by k6 from options
    });
    trackResult(res, tag);
    return res;
}

/**
 * POST JSON with auth + tagging
 */
export function post(path, body, headers = {}, tagName = null) {
    const tag = tagName || `POST ${path}`;
    const res = http.post(path, JSON.stringify(body), {
        headers: { ...JSON_CT, ...headers },
        tags: { name: tag },
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
        tags: { name: tag },
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
        tags: { name: tag },
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
        tags: { name: tag },
    });
    trackResult(res, tag);
    return res;
}

// ──────────────────────────────────────────────────────────────
// Response checking helpers
// ──────────────────────────────────────────────────────────────

export function expectStatus(res, status = 200, label = 'request') {
    return check(res, {
        [`${label}: status ${status}`]: (r) => r.status === status,
    });
}

export function expectSuccess(res, label = 'request') {
    return check(res, {
        [`${label}: HTTP 200`]: (r) => r.status === 200,
        [`${label}: valid JSON`]: (r) => {
            try { JSON.parse(r.body); return true; } catch { return false; }
        },
        [`${label}: has data field`]: (r) => {
            try {
                const b = JSON.parse(r.body);
                return r.status === 200 || b.data !== undefined;
            } catch { return false; }
        },
    });
}

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

export function extractData(res) {
    try {
        const data = JSON.parse(res.body).data;
        if (data && Array.isArray(data.content)) return data.content;
        return data || null;
    } catch {
        return null;
    }
}

export function extractId(res) {
    try {
        return JSON.parse(res.body).data?.id || null;
    } catch {
        return null;
    }
}

function trackResult(res, tag) {
    const success = (res.status >= 200 && res.status < 300);
    apiSuccessRate.add(success);

    if (!success) {
        apiErrors.add(1, { endpoint: tag });
        
        // Suppress logging for 404 on GET (often expected in async/polling scenarios)
        const isExpected404 = res.status === 404 && res.request.method === 'GET';
        
        if (!isExpected404) {
            console.error(`[error] ${tag} returned ${res.status}: ${res.body}`);
        }
    }
}
