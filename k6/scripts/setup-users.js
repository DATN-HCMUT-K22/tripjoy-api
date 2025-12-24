// Script to create test users for k6 testing
// Run this ONCE before running smoke tests

import http from 'k6/http';
import { check } from 'k6';
import { buildURL, config } from '../config/dev.js';

export const options = {
    vus: 1,
    iterations: 1
};

export default function () {
    console.log('=== Creating test users for k6 testing ===\n');

    // Create testuser1
    createUser(config.defaultUsers.user1);

    // Create testuser2
    createUser(config.defaultUsers.user2);

    console.log('\n=== Test users setup complete! ===');
    console.log('You can now run: k6 run k6/scenarios/smoke-test.js');
}

function createUser(userData) {
    console.log(`Creating user: ${userData.username}...`);

    const payload = JSON.stringify({
        username: userData.username,
        password: userData.password,
        email: userData.email,
        fullName: userData.username
    });

    const res = http.post(
        buildURL('/auth/register'),
        payload,
        {
            headers: { 'Content-Type': 'application/json' }
        }
    );

    const success = check(res, {
        'register success': (r) => r.status === 200
    });

    if (success) {
        console.log(`✓ User ${userData.username} created successfully`);
    } else if (res.status === 400 || res.status === 409) {
        console.log(`⚠ User ${userData.username} already exists (${res.status})`);
    } else {
        console.log(`✗ Failed to create ${userData.username}: ${res.status} - ${res.body}`);
    }
}
