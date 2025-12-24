// Stress Test - Find the breaking point
// Gradually increase load to identify system limits

import http from 'k6/http';
import { check, sleep } from 'k6';
import { buildURL, config } from '../config/dev.js';
import { stressThresholds } from '../config/thresholds.js';
import { login, getAuthHeaders } from '../lib/auth.js';
import { checkSuccess } from '../lib/check-utils.js';

export const options = {
    stages: [
        { duration: '2m', target: 50 },   // Ramp up to 50 VUs
        { duration: '3m', target: 100 },  // Ramp up to 100 VUs
        { duration: '5m', target: 100 },  // Stay at 100 VUs
        { duration: '2m', target: 0 },    // Ramp down
    ],
    thresholds: stressThresholds
};

export function setup() {
    const token = login(config.defaultUsers.user1.username, config.defaultUsers.user1.password);

    if (!token) {
        console.error('Setup failed: Could not login');
        return null;
    }

    return { token };
}

export default function (data) {
    if (!data || !data.token) {
        return;
    }

    const headers = getAuthHeaders(data.token);

    // High-frequency operations

    // Read operations (most common)
    http.get(buildURL('/users/me'), { headers, tags: { name: 'get-me' } });
    sleep(0.2);

    http.get(buildURL('/groups'), { headers, tags: { name: 'get-groups' } });
    sleep(0.2);

    http.get(buildURL('/locations?page=0&size=10'), { headers, tags: { name: 'get-locations' } });
    sleep(0.2);

    http.get(buildURL('/notifications?page=0&size=10'), { headers, tags: { name: 'get-notifications' } });
    sleep(0.2);

    http.get(buildURL('/notifications/unread-count'), { headers, tags: { name: 'get-unread-count' } });

    sleep(0.5);
}
