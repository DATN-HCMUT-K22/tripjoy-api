/**
 * TripJoy k6 — Balanced Performance Thresholds
 * 
 * This set combines Industry Best Practices (Google RAIL) with 
 * realistic constraints of a local development environment.
 * Differentiated by scenario type to demonstrate professional SLA design.
 */

// ──────────────────────────────────────────────
// BASE thresholds (General system health)
// ──────────────────────────────────────────────
export const baseThresholds = {
    http_req_failed: ['rate<0.01'], 
    http_req_duration: [
        'p(90)<1000', // The classic 1-second limit for most users
        'p(95)<1500', // Threshold for heavy processing
        'p(99)<3000', // Outlier protection
    ],
    checks: ['rate>0.99'],

    // Dummy thresholds for request counting
    'http_reqs{scenario:auth}': ['count>=0'],
    'http_reqs{scenario:read}': ['count>=0'],
    'http_reqs{scenario:manage}': ['count>=0'],
    'http_reqs{scenario:social}': ['count>=0'],
    'http_reqs{scenario:chat}': ['count>=0'],
};

// ──────────────────────────────────────────────
// SMOKE — Verify system stability (Balanced SLOs)
// ──────────────────────────────────────────────
export const smokeThresholds = {
    ...baseThresholds,
    // READ: Heavy DB queries, allowing slightly over 1s for local environment
    'http_req_duration{scenario:read}': ['p(95)<1300'],
    
    // MANAGE: Standard CRUD operations, targeting the 1s limit
    'http_req_duration{scenario:manage}': ['p(95)<1000'],
    
    // SOCIAL: Content feed with media logic
    'http_req_duration{scenario:social}': ['p(95)<1400'],
    
    // CHAT: Real-time messaging should be the snappiest
    'http_req_duration{scenario:chat}': ['p(95)<800'],
};

// ──────────────────────────────────────────────
// LOAD — Production-like load simulation
// ──────────────────────────────────────────────
export const loadThresholds = {
    ...baseThresholds,
    'http_req_duration{scenario:read}': ['p(95)<1500'],
    'http_req_duration{scenario:manage}': ['p(95)<1200'],
    'http_req_duration{scenario:social}': ['p(95)<1800'],
    'http_req_duration{scenario:chat}': ['p(95)<1000'],
};

// ──────────────────────────────────────────────
// STRESS — Pushing system limits
// ──────────────────────────────────────────────
export const stressThresholds = {
    ...baseThresholds,
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<3000'],
    'http_req_duration{scenario:read}': ['p(95)<2500'],
};

// ──────────────────────────────────────────────
// SOAK — Reliability over time
// ──────────────────────────────────────────────
export const soakThresholds = {
    ...baseThresholds,
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
};

// ──────────────────────────────────────────────
// SPIKE — Sudden traffic surges
// ──────────────────────────────────────────────
export const spikeThresholds = {
    ...baseThresholds,
    http_req_failed: ['rate<0.05'], // Allow some failure under extreme spike
    http_req_duration: ['p(95)<5000'], // Allow higher latency during spike
};

// ──────────────────────────────────────────────
// AI — Complex LLM processing
// ──────────────────────────────────────────────
export const aiThresholds = {
    ...baseThresholds,
    http_req_failed: ['rate<0.05'],
    // AI processing is naturally slow (60s - 90s for complex LLM tasks)
    http_req_duration: ['p(95)<95000'], 
    'http_req_duration{scenario:ai}': ['p(95)<90000'],
};

export default baseThresholds;
