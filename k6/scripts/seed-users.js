/**
 * TripJoy k6 — Seed Users Script
 * 
 * Purpose: Register testuser1 and testuser2 defined in config/environments.js
 * Run this ONCE before running any other tests.
 */

import { env } from '../config/environments.js';
import { register } from '../lib/auth.js';

export const options = {
    vus: 1,
    iterations: 1,
};

export default function () {
    console.log(`\n=== Seeding Test Users on ${env.baseURL} ===`);

    const users = [
        env.users.regular,
        env.users.regular2
    ];

    for (const userData of users) {
        console.log(`\nChecking/Registering user: ${userData.username}...`);
        const result = register(userData);
        
        if (result) {
            console.log(`[success] User ${userData.username} is ready (Registered or already exists).`);
        } else {
            console.log(`[error] Failed to register ${userData.username}. Check API logs.`);
        }
    }

    console.log('\n=== Seed Complete ===\n');
}
