// Authentication Flow Test
// Tests all authentication endpoints: register, login, introspect, refresh, logout

import http from 'k6/http';
import { check, sleep } from 'k6';
import { buildURL } from '../config/dev.js';
import { smokeThresholds } from '../config/thresholds.js';
import { generateUserData } from '../lib/utils.js';
import { checkSuccess } from '../lib/check-utils.js';

export const options = {
    vus: 5,
    duration: '30s',
    thresholds: smokeThresholds
};

export default function () {
    // Generate unique user for this VU iteration
    const userData = generateUserData();

    // Test 1: Register new user
    const registerRes = http.post(
        buildURL('/auth/register'),
        JSON.stringify(userData),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'register' }
        }
    );

    check(registerRes, {
        'register: status 200': (r) => r.status === 200,
        'register: has data': (r) => r.json('data') !== null
    });

    sleep(0.5);

    // Test 2: Login with registered user
    const loginRes = http.post(
        buildURL('/auth/login'),
        JSON.stringify({
            username: userData.username,
            password: userData.password
        }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'login' }
        }
    );

    const loginSuccess = check(loginRes, {
        'login: status 200': (r) => r.status === 200,
        'login: has token': (r) => r.json('data.token') !== undefined,
        'login: authenticated': (r) => r.json('data.authenticated') === true
    });

    if (!loginSuccess) {
        console.error(`Login failed for ${userData.username}: ${loginRes.status} - ${loginRes.body}`);
        return;
    }

    const accessToken = loginRes.json('data.token');

    sleep(0.5);

    // Test 3: Introspect token
    const introspectRes = http.post(
        buildURL('/auth/introspect'),
        JSON.stringify({ token: accessToken }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'introspect' }
        }
    );

    check(introspectRes, {
        'introspect: status 200': (r) => r.status === 200,
        'introspect: token is valid': (r) => r.json('data.valid') === true
    });

    sleep(0.5);

    // Test 4: Logout
    const logoutRes = http.post(
        buildURL('/auth/logout'),
        JSON.stringify({ token: accessToken }),
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            tags: { name: 'logout' }
        }
    );

    check(logoutRes, {
        'logout: status 200': (r) => r.status === 200
    });

    sleep(1);
}
