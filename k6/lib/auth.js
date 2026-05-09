/**
 * TripJoy k6 — Auth Helper Library
 *
 * Provides token management with in-memory caching per VU.
 * Implements: login, register, logout, introspect, refresh.
 */

import http from 'k6/http';
import { check } from 'k6';
import { url } from '../config/environments.js';

const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ──────────────────────────────────────────────────────────────
// Core Auth API calls
// ──────────────────────────────────────────────────────────────

/**
 * Login with username + password. Returns { token, refreshToken } or null.
 */
export function login(username, password) {
    const res = http.post(
        url('/auth/login'),
        JSON.stringify({ username, password }),
        { headers: JSON_HEADERS, tags: { name: 'POST /auth/login', scenario: 'auth' } }
    );

    const ok = check(res, {
        'login: status 200': (r) => r.status === 200,
        'login: has token': (r) => {
            try { return !!JSON.parse(r.body).data?.access_token; } catch { return false; }
        },
    });

    if (!ok) {
        console.error(`[auth] login failed for ${username}: HTTP ${res.status} — ${res.body?.substring(0, 200)}`);
        return null;
    }

    try {
        const body = JSON.parse(res.body);
        return {
            access_token: body.data.access_token,
            refresh_token: body.data.refresh_token || null,
            authenticated: body.data.authenticated === true,
        };
    } catch {
        return null;
    }
}

/**
 * Register a new user. Returns { token } or null.
 */
export function register(userData) {
    const payload = {
        username: userData.username,
        password: userData.password,
        email: userData.email,
        fullName: userData.fullName || userData.username,
    };

    const res = http.post(
        url('/auth/register'),
        JSON.stringify(payload),
        { headers: JSON_HEADERS, tags: { name: 'POST /auth/register', scenario: 'auth' } }
    );

    const ok = check(res, {
        'register: status 200': (r) => r.status === 200,
        'register: has data': (r) => {
            try { return !!JSON.parse(r.body).data; } catch { return false; }
        },
    });

    if (!ok) {
        console.warn(`[auth] register failed for ${userData.username}: HTTP ${res.status} — ${res.body?.substring(0, 200)}. Trying login...`);
        // If registration fails, try to login (maybe user already exists)
        return login(userData.username, userData.password);
    }

    // After registration, login to get token
    return login(userData.username, userData.password);
}

/**
 * Introspect a token to validate it.
 */
export function introspect(token) {
    const res = http.post(
        url('/auth/introspect'),
        JSON.stringify({ token }),
        { headers: JSON_HEADERS, tags: { name: 'POST /auth/introspect', scenario: 'auth' } }
    );

    const ok = check(res, {
        'introspect: status 200': (r) => r.status === 200,
        'introspect: token valid': (r) => {
            try { return JSON.parse(r.body).data?.valid === true; } catch { return false; }
        },
    });

    return ok;
}

/**
 * Refresh an access token.
 */
export function refreshToken(token) {
    const res = http.post(
        url('/auth/refresh'),
        JSON.stringify({ token }),
        { headers: JSON_HEADERS, tags: { name: 'POST /auth/refresh', scenario: 'auth' } }
    );

    const ok = check(res, {
        'refresh: status 200': (r) => r.status === 200,
        'refresh: has token': (r) => {
            try { return !!JSON.parse(r.body).data?.access_token; } catch { return false; }
        },
    });

    if (!ok) return null;
    try {
        return JSON.parse(res.body).data.access_token;
    } catch {
        return null;
    }
}

/**
 * Logout — invalidates the given token.
 */
export function logout(access_token) {
    const res = http.post(
        url('/auth/logout'),
        JSON.stringify({ token: access_token }),
        {
            headers: { ...JSON_HEADERS, Authorization: `Bearer ${access_token}` },
            tags: { name: 'POST /auth/logout', scenario: 'auth' },
        }
    );

    check(res, { 'logout: status 200': (r) => r.status === 200 });
}

// ──────────────────────────────────────────────────────────────
// Convenience builders
// ──────────────────────────────────────────────────────────────

/**
 * Returns headers with Bearer token.
 */
export function authHeaders(access_token) {
    return {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${access_token}`,
    };
}

/**
 * Get-or-login: returns a cached token for the given credentials.
 * Call once in setup(), share via data bag.
 */
export function getOrLogin(username, password) {
    const result = login(username, password);
    if (!result) {
        throw new Error(`[auth] Cannot login as ${username} — aborting test`);
    }
    return result.access_token;
}
