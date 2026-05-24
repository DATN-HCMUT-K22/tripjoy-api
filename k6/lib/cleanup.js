import http from 'k6/http';
import { url } from '../config/environments.js';
import { extractData } from './http.js';

/**
 * Automates dabase cleanup for the test user by removing test groups and itineraries.
 * Deletes:
 * 1. Itineraries containing "Adventure" in their title or "Auto-generated" in description.
 * 2. Groups containing "Trip" or "k6" in their names/descriptions.
 *
 * @param {string} accessToken - User access token
 */
export function cleanupTestData(accessToken) {
    if (!accessToken) return;
    const headers = { 'Authorization': `Bearer ${accessToken}` };
    console.log('[cleanup] Starting database cleanup for test user...');

    // 1. Fetch and delete itineraries
    const itisRes = http.get(url('/itineraries/me'), { headers });
    if (itisRes.status === 200) {
        const itis = extractData(itisRes) || [];
        const testItis = itis.filter(i => 
            (i.title && i.title.includes('Adventure')) || 
            (i.description && i.description.includes('Auto-generated'))
        );
        if (testItis.length > 0) {
            console.log(`[cleanup] Found ${testItis.length} test itineraries to delete.`);
            const batchSize = 100;
            let failedCount = 0;
            for (let i = 0; i < testItis.length; i += batchSize) {
                const chunk = testItis.slice(i, i + batchSize);
                const reqs = chunk.map(item => [
                    'DELETE',
                    url(`/itineraries/${item.id}`),
                    null,
                    { headers }
                ]);
                const responses = http.batch(reqs);
                responses.forEach((res, index) => {
                    if (res.status !== 200 && res.status !== 204) {
                        failedCount++;
                    }
                });
            }
            if (failedCount > 0) {
                console.warn(`[cleanup] Failed to delete ${failedCount} itineraries.`);
            } else {
                console.log(`[cleanup] Successfully deleted all ${testItis.length} itineraries.`);
            }
        }
    }

    // 2. Fetch and delete groups
    const groupsRes = http.get(url('/groups'), { headers });
    if (groupsRes.status === 200) {
        const groups = extractData(groupsRes) || [];
        const testGroups = groups.filter(g => 
            (g.name && g.name.includes('Trip')) || 
            (g.description && g.description.includes('k6'))
        );
        if (testGroups.length > 0) {
            console.log(`[cleanup] Found ${testGroups.length} test groups to delete.`);
            const batchSize = 100;
            let failedCount = 0;
            for (let i = 0; i < testGroups.length; i += batchSize) {
                const chunk = testGroups.slice(i, i + batchSize);
                const reqs = chunk.map(g => [
                    'DELETE',
                    url(`/groups/${g.id}`),
                    null,
                    { headers }
                ]);
                const responses = http.batch(reqs);
                responses.forEach((res, index) => {
                    if (res.status !== 200 && res.status !== 204) {
                        failedCount++;
                    }
                });
            }
            if (failedCount > 0) {
                console.warn(`[cleanup] Failed to delete ${failedCount} groups.`);
            } else {
                console.log(`[cleanup] Successfully deleted all ${testGroups.length} groups.`);
            }
        }
    }
    console.log('[cleanup] Database cleanup finished.');
}

