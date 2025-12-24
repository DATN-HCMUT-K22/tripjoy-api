// Script to verify test users can login
// Run this to debug login issues

import http from 'k6/http';
import { check } from 'k6';
import { buildURL, config } from '../config/dev.js';

export const options = {
    vus: 1,
    iterations: 1
};

export default function () {
    console.log('=== Verifying test users can login ===\n');

    // Test user1
    testLogin(config.defaultUsers.user1);

    // Test user2
    testLogin(config.defaultUsers.user2);

    console.log('\n=== Verification complete ===');
}

function testLogin(userData) {
    console.log(`Testing login for: ${userData.username}...`);

    const payload = JSON.stringify({
        username: userData.username,
        password: userData.password
    });

    const res = http.post(
        buildURL('/auth/login'),
        payload,
        {
            headers: { 'Content-Type': 'application/json' }
        }
    );

    const success = check(res, {
        'status is 200': (r) => r.status === 200,
        'has token': (r) => {
            try {
                return r.json('data.token') !== undefined;
            } catch (e) {
                return false;
            }
        }
    });

    if (success) {
        console.log(`✓ Login successful for ${userData.username}`);
        console.log(`  Token: ${res.json('data.token').substring(0, 20)}...`);
    } else {
        console.log(`✗ Login failed for ${userData.username}`);
        console.log(`  Status: ${res.status}`);
        console.log(`  Response: ${res.body}`);
    }

    console.log('');
}
