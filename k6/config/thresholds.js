/**
 * TripJoy k6 — Performance Thresholds (Best Practice)
 *
 * Standards based on Google RAIL model & industry SLOs:
 *  - p(95) < 500ms  → ideal user experience
 *  - p(95) < 1000ms → acceptable
 *  - p(95) < 2000ms → degraded
 *  - error rate < 1% → healthy
 */

// ──────────────────────────────────────────────
// BASE thresholds (applied to ALL scenarios)
// ──────────────────────────────────────────────
export const baseThresholds = {
    // Overall HTTP error rate
    http_req_failed: ['rate<0.01'],     // < 1% errors

    // Overall duration
    http_req_duration: [
        'p(90)<1000',   // 90th pct under 1 s
        'p(95)<2000',   // 95th pct under 2 s
        'p(99)<5000',   // 99th pct under 5 s
    ],

    // Iteration (full user journey)
    iteration_duration: ['p(95)<10000'],

    // Custom counters
    checks: ['rate>0.95'],              // > 95% checks pass
};

// ──────────────────────────────────────────────
// SMOKE — verify system is alive (1-2 VUs)
// ──────────────────────────────────────────────
export const smokeThresholds = {
    http_req_failed: ['rate<0.001'],    // < 0.1% errors
    http_req_duration: [
        'p(95)<500',
        'p(99)<1000',
    ],
    checks: ['rate>0.99'],
};

// ──────────────────────────────────────────────
// LOAD — normal expected traffic
// ──────────────────────────────────────────────
export const loadThresholds = {
    http_req_failed: ['rate<0.01'],
    http_req_duration: [
        'p(90)<800',
        'p(95)<1500',
        'p(99)<3000',
    ],

    // Per-endpoint SLOs (using named requests)
    'http_req_duration{scenario:auth}': ['p(95)<500'],
    'http_req_duration{scenario:read}': ['p(95)<1000'],
    'http_req_duration{scenario:write}': ['p(95)<2000'],
    'http_req_duration{scenario:search}': ['p(95)<2000'],

    checks: ['rate>0.95'],
};

// ──────────────────────────────────────────────
// STRESS — ramp beyond normal capacity
// ──────────────────────────────────────────────
export const stressThresholds = {
    http_req_failed: ['rate<0.05'],     // tolerate up to 5%
    http_req_duration: [
        'p(95)<3000',
        'p(99)<8000',
    ],
    checks: ['rate>0.90'],
};

// ──────────────────────────────────────────────
// SOAK — extended duration (memory leaks, etc.)
// ──────────────────────────────────────────────
export const soakThresholds = {
    http_req_failed: ['rate<0.01'],
    http_req_duration: [
        'p(95)<2000',
        'p(99)<5000',
    ],
    checks: ['rate>0.95'],
};

// ──────────────────────────────────────────────
// SPIKE — sudden burst (10x normal traffic)
// ──────────────────────────────────────────────
export const spikeThresholds = {
    http_req_failed: ['rate<0.10'],     // tolerate up to 10%
    http_req_duration: ['p(95)<5000'],
    checks: ['rate>0.85'],
};

export default baseThresholds;
