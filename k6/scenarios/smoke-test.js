// Smoke Test - Quick validation that system is working
// Minimal load, tests only critical endpoints

import http from 'k6/http';
import { check, sleep } from 'k6';
import { buildURL, config } from '../config/dev.js';
import { smokeThresholds } from '../config/thresholds.js';
import { login, getAuthHeaders } from '../lib/auth.js';
import { checkSuccess } from '../lib/check-utils.js';

export const options = {
    vus: 2,
    duration: '1m',
    thresholds: smokeThresholds
};

export function setup() {
    // Login to get auth token for tests
    const token = login(config.defaultUsers.user1.username, config.defaultUsers.user1.password);

    if (!token) {
        console.error('Setup failed: Could not login with default user');
        return null;
    }

    return { token };
}

export default function (data) {
    if (!data || !data.token) {
        console.error('No auth token available, skipping tests');
        return;
    }

    const headers = getAuthHeaders(data.token);

    // Test 1: Get my info
    const myInfoRes = http.get(
        buildURL('/users/me'),
        { headers, tags: { name: 'get-me' } }
    );

    checkSuccess(myInfoRes, 'get-me');
    sleep(0.5);

    // Test 2: Get my groups
    const myGroupsRes = http.get(
        buildURL('/groups'),
        { headers, tags: { name: 'get-my-groups' } }
    );

    checkSuccess(myGroupsRes, 'get-my-groups');
    sleep(0.5);

    // Test 3: Get locations (paginated)
    const locationsRes = http.get(
        buildURL('/locations?page=0&size=10'),
        { headers, tags: { name: 'get-locations' } }
    );

    checkSuccess(locationsRes, 'get-locations');
    sleep(0.5);

    // Test 4: Get notifications
    const notificationsRes = http.get(
        buildURL('/notifications?page=0&size=10'),
        { headers, tags: { name: 'get-notifications' } }
    );

    checkSuccess(notificationsRes, 'get-notifications');
    sleep(1);
}
