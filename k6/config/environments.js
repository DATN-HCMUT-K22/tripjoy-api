/**
 * TripJoy k6 — Environment Configuration
 * Supports: local, staging, production
 *
 * Usage:
 *   k6 run -e ENV=local scenarios/smoke-test.js
 *   k6 run -e ENV=staging scenarios/load-test.js
 *   k6 run -e ENV=production scenarios/soak-test.js
 */

const ENVIRONMENTS = {
    local: {
        baseURL: 'http://localhost:8080',
        apiPrefix: '/api/v1',
        users: {
            regular: { username: 'testuser1', password: 'Test123!', email: 'testuser1@tripjoy.com' },
            regular2: { username: 'testuser2', password: 'Test123!', email: 'testuser2@tripjoy.com' },
            admin: { username: 'admin', password: 'admin123', email: 'admin@tripjoy.com' },
        },
        timeouts: { default: 30000, auth: 10000 },
    },
    staging: {
        baseURL: __ENV.STAGING_URL || 'https://staging-api.tripjoy.com',
        apiPrefix: '/api/v1',
        users: {
            regular: { username: __ENV.TEST_USER1 || 'stg_user1', password: __ENV.TEST_PASS1 || 'Test123!', email: 'stguser1@tripjoy.com' },
            regular2: { username: __ENV.TEST_USER2 || 'stg_user2', password: __ENV.TEST_PASS2 || 'Test123!', email: 'stguser2@tripjoy.com' },
            admin: { username: __ENV.ADMIN_USER || 'stg_admin', password: __ENV.ADMIN_PASS || 'Admin123!', email: 'stgadmin@tripjoy.com' },
        },
        timeouts: { default: 30000, auth: 10000 },
    },
    production: {
        baseURL: __ENV.PROD_URL || 'https://api.tripjoy.com',
        apiPrefix: '/api/v1',
        users: {
            regular: { username: __ENV.PROD_USER1 || '', password: __ENV.PROD_PASS1 || '', email: '' },
            regular2: { username: __ENV.PROD_USER2 || '', password: __ENV.PROD_PASS2 || '', email: '' },
            admin: { username: __ENV.PROD_ADMIN || '', password: __ENV.PROD_ADMIN_PASS || '', email: '' },
        },
        timeouts: { default: 60000, auth: 15000 },
    },
};

const ENV_NAME = __ENV.ENV || 'local';

export const env = ENVIRONMENTS[ENV_NAME] || ENVIRONMENTS.local;

/**
 * Build full URL from path (e.g. '/auth/login')
 */
export function url(path) {
    return `${env.baseURL}${env.apiPrefix}${path}`;
}

export default env;
