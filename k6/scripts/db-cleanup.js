/**
 * TripJoy k6 — Database Test Data Cleanup Script
 *
 * Purpose: Authenticates as all test users (stg_user1, stg_user2) 
 *          and dynamically deletes all their auto-generated itineraries and groups.
 *
 * Usage:
 *   k6 run -e ENV=staging scripts/db-cleanup.js
 *   k6 run -e ENV=local scripts/db-cleanup.js
 */

import { login } from '../lib/auth.js';
import { cleanupTestData } from '../lib/cleanup.js';
import { env } from '../config/environments.js';

export const options = {
    vus: 1,
    iterations: 1,
    setupTimeout: '10m',
    teardownTimeout: '10m',
};

export default function () {
    console.log('=== STARTING TEST DATA CLEANUP FOR ALL TEST ACCOUNTS ===');

    // 1. Clean up for user 1
    console.log(`[cleanup-script] Logging in as Regular User 1: ${env.users.regular.username}...`);
    const r1 = login(env.users.regular.username, env.users.regular.password);
    if (r1 && r1.access_token) {
        console.log('[cleanup-script] Login User 1 success. Executing cleanup...');
        cleanupTestData(r1.access_token);
    } else {
        console.warn('[cleanup-script] Failed to login User 1. Skipping.');
    }

    console.log('---------------------------------------------------------');

    // 2. Clean up for user 2
    console.log(`[cleanup-script] Logging in as Regular User 2: ${env.users.regular2.username}...`);
    const r2 = login(env.users.regular2.username, env.users.regular2.password);
    if (r2 && r2.access_token) {
        console.log('[cleanup-script] Login User 2 success. Executing cleanup...');
        cleanupTestData(r2.access_token);
    } else {
        console.warn('[cleanup-script] Failed to login User 2. Skipping.');
    }

    console.log('=== TEST DATA CLEANUP COMPLETED ===');
}
