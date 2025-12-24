// Authentication Helper Functions

import http from 'k6/http';
import { check } from 'k6';
import { buildURL } from '../config/dev.js';

/**
 * Register a new user
 * @param {Object} userData - User registration data
 * @returns {Object} Response with user data
 */
export function registerUser(userData) {
    const url = buildURL('/auth/register');
    const payload = JSON.stringify({
        username: userData.username,
        password: userData.password,
        email: userData.email,
        fullName: userData.fullName || userData.username
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'register' }
    };

    const res = http.post(url, payload, params);

    check(res, {
        'register: status is 200': (r) => r.status === 200,
        'register: has data': (r) => r.json('data') !== null
    });

    return res;
}

/**
 * Login and get access token
 * @param {string} username - Username
 * @param {string} password - Password
 * @returns {string|null} Access token or null if login failed
 */
export function login(username, password) {
    const url = buildURL('/auth/login');
    const payload = JSON.stringify({
        username: username,
        password: password
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'login' }
    };

    const res = http.post(url, payload, params);

    const loginSuccess = check(res, {
        'login: status is 200': (r) => r.status === 200,
        'login: has token': (r) => r.json('data.token') !== undefined
    });

    if (loginSuccess && res.json('data.token')) {
        return res.json('data.token');
    }

    console.error(`Login failed for ${username}: ${res.status} - ${res.body}`);
    return null;
}

/**
 * Refresh access token
 * @param {string} refreshToken - Refresh token
 * @returns {string|null} New access token or null if refresh failed
 */
export function refreshToken(refreshToken) {
    const url = buildURL('/auth/refresh');
    const payload = JSON.stringify({
        token: refreshToken
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'refresh' }
    };

    const res = http.post(url, payload, params);

    const refreshSuccess = check(res, {
        'refresh: status is 200': (r) => r.status === 200,
        'refresh: has token': (r) => r.json('data.token') !== undefined
    });

    if (refreshSuccess && res.json('data.token')) {
        return res.json('data.token');
    }

    return null;
}

/**
 * Introspect token to validate
 * @param {string} token - Access token to validate
 * @returns {boolean} True if token is valid
 */
export function introspectToken(token) {
    const url = buildURL('/auth/introspect');
    const payload = JSON.stringify({
        token: token
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'introspect' }
    };

    const res = http.post(url, payload, params);

    return check(res, {
        'introspect: status is 200': (r) => r.status === 200,
        'introspect: token is valid': (r) => r.json('data.valid') === true
    });
}

/**
 * Logout from the system
 * @param {string} token - Access token
 * @returns {boolean} True if logout successful
 */
export function logout(token) {
    const url = buildURL('/auth/logout');
    const payload = JSON.stringify({
        token: token
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        tags: { name: 'logout' }
    };

    const res = http.post(url, payload, params);

    return check(res, {
        'logout: status is 200': (r) => r.status === 200
    });
}

/**
 * Get authorization headers with bearer token
 * @param {string} token - Access token
 * @returns {Object} Headers object with Authorization
 */
export function getAuthHeaders(token) {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}
