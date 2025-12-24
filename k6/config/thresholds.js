// Performance Thresholds for k6 Tests
// These define the acceptance criteria for test success

export const thresholds = {
    // HTTP request duration
    'http_req_duration': [
        'p(95)<1000',   // 95% of requests must complete within 1s
        'p(99)<2000',   // 99% of requests must complete within 2s
        'max<5000'      // No request should take longer than 5s
    ],

    // HTTP request failure rate
    'http_req_failed': [
        'rate<0.01'     // Error rate must be less than 1%
    ],

    // Iteration duration (full test scenario iteration)
    'iteration_duration': [
        'p(95)<5000',   // 95% of iterations complete within 5s
        'p(99)<10000'   // 99% of iterations complete within 10s
    ],

    // Specific endpoint thresholds (can be customized per test)
    'http_req_duration{name:login}': [
        'p(95)<500'     // Login should be fast
    ],
    'http_req_duration{name:search}': [
        'p(95)<1500'    // Search can be slightly slower  
    ]
};

// Smoke test thresholds (stricter - system must be healthy)
export const smokeThresholds = {
    'http_req_duration': ['p(95)<500', 'p(99)<1000'],
    'http_req_failed': ['rate<0.001'],  // Less than 0.1% error rate
};

// Load test thresholds (normal traffic)
export const loadThresholds = thresholds;

// Stress test thresholds (more lenient - we expect some degradation)
export const stressThresholds = {
    'http_req_duration': ['p(95)<2000', 'p(99)<5000'],
    'http_req_failed': ['rate<0.05'],  // Up to 5% error rate acceptable
};
