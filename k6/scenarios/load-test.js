// Load Test - Expected normal traffic
// Tests all implemented endpoints with realistic load

import http from 'k6/http';
import { check, sleep } from 'k6';
import { buildURL, config } from '../config/dev.js';
import { loadThresholds } from '../config/thresholds.js';
import { login, getAuthHeaders } from '../lib/auth.js';
import { generateGroupName, generateLocationData, generateUserData } from '../lib/utils.js';
import { checkSuccess, checkHasData } from '../lib/check-utils.js';

export const options = {
    stages: [
        { duration: '1m', target: 20 },   // Ramp up to 20 VUs
        { duration: '5m', target: 20 },   // Stay at 20 VUs
        { duration: '1m', target: 0 },    // Ramp down to 0
    ],
    thresholds: loadThresholds
};

export function setup() {
    // Create test users and login
    const user1Token = login(config.defaultUsers.user1.username, config.defaultUsers.user1.password);
    const user2Token = login(config.defaultUsers.user2.username, config.defaultUsers.user2.password);

    if (!user1Token || !user2Token) {
        console.error('Setup failed: Could not login with default users');
        return null;
    }

    return {
        user1Token,
        user2Token
    };
}

export default function (data) {
    if (!data || !data.user1Token) {
        console.error('No auth tokens available');
        return;
    }

    // Randomly choose a user token
    const token = Math.random() > 0.5 ? data.user1Token : data.user2Token;
    const headers = getAuthHeaders(token);

    // Simulate realistic user behavior
    const scenario = Math.floor(Math.random() * 4);

    switch (scenario) {
        case 0:
            // Scenario 1: Browse locations
            browseLocations(headers);
            break;
        case 1:
            // Scenario 2: Manage groups
            manageGroups(headers);
            break;
        case 2:
            // Scenario 3: Check notifications
            checkNotifications(headers);
            break;
        case 3:
            // Scenario 4: User profile
            viewProfile(headers);
            break;
    }

    sleep(Math.random() * 3 + 1); // Random sleep 1-4 seconds
}

function browseLocations(headers) {
    // Get all locations
    const locationsRes = http.get(
        buildURL('/locations?page=0&size=20'),
        { headers, tags: { name: 'get-locations' } }
    );
    checkSuccess(locationsRes, 'get-locations');
    sleep(0.5);

    // Search locations
    const searchRes = http.get(
        buildURL('/locations/search?query=coffee&page=0&size=10'),
        { headers, tags: { name: 'search-locations' } }
    );
    checkSuccess(searchRes, 'search-locations');
    sleep(0.5);

    // Find nearby locations
    const nearbyRes = http.get(
        buildURL('/locations/nearby?latitude=10.8231&longitude=106.6297&radius=5000'),
        { headers, tags: { name: 'nearby-locations' } }
    );
    checkSuccess(nearbyRes, 'nearby-locations');
}

function manageGroups(headers) {
    // Get my groups
    const myGroupsRes = http.get(
        buildURL('/groups'),
        { headers, tags: { name: 'get-my-groups' } }
    );
    checkSuccess(myGroupsRes, 'get-my-groups');
    sleep(0.5);

    // Create a new group (some iterations)
    if (Math.random() > 0.7) {
        const groupData = {
            name: generateGroupName(),
            description: 'Test group for load testing'
        };

        const createGroupRes = http.post(
            buildURL('/groups'),
            JSON.stringify(groupData),
            { headers, tags: { name: 'create-group' } }
        );
        checkSuccess(createGroupRes, 'create-group');

        // If group created, get its details
        if (createGroupRes.status === 200) {
            const groupId = createGroupRes.json('data.id');
            if (groupId) {
                sleep(0.5);
                const groupDetailsRes = http.get(
                    buildURL(`/groups/${groupId}`),
                    { headers, tags: { name: 'get-group-details' } }
                );
                checkSuccess(groupDetailsRes, 'get-group-details');
            }
        }
    }
}

function checkNotifications(headers) {
    // Get notifications
    const notificationsRes = http.get(
        buildURL('/notifications?page=0&size=20'),
        { headers, tags: { name: 'get-notifications' } }
    );
    checkSuccess(notificationsRes, 'get-notifications');
    sleep(0.5);

    // Get unread count
    const unreadCountRes = http.get(
        buildURL('/notifications/unread-count'),
        { headers, tags: { name: 'get-unread-count' } }
    );
    checkSuccess(unreadCountRes, 'get-unread-count');
}

function viewProfile(headers) {
    // Get my info
    const myInfoRes = http.get(
        buildURL('/users/me'),
        { headers, tags: { name: 'get-my-info' } }
    );
    checkSuccess(myInfoRes, 'get-my-info');
}
