// Configuration for Development Environment

export const config = {
    // Base URL for the API
    baseURL: __ENV.BASE_URL || 'http://localhost:8080',

    // API prefix
    apiPrefix: '/api/v1',

    // Default test users (should exist in DB or will be created during tests)
    defaultUsers: {
        admin: {
            username: 'admin',
            password: 'admin123',
            email: 'admin@tripjoy.com'
        },
        user1: {
            username: 'testuser1',
            password: 'Test123!',
            email: 'testuser1@tripjoy.com'
        },
        user2: {
            username: 'testuser2',
            password: 'Test123!',
            email: 'testuser2@tripjoy.com'
        }
    },

    // Request timeouts (milliseconds)
    timeouts: {
        default: 30000,      // 30 seconds
        auth: 10000,         // 10 seconds for auth requests
        upload: 60000        // 60 seconds for file uploads
    },

    // Connection settings
    connection: {
        maxIdleConnections: 100,
        maxConnectionsPerHost: 10
    }
};

// Helper to build full URL
export function buildURL(path) {
    return `${config.baseURL}${config.apiPrefix}${path}`;
}
