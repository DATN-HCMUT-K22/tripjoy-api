import http from 'k6/http';
import { url } from '../config/environments.js';
import { extractData } from './http.js';

/**
 * Automates database cleanup for the test user by dynamically identifying
 * and removing all their test data: posts, itineraries, groups, suggestions, and comments.
 *
 * @param {string} accessToken - User access token
 */
export function cleanupTestData(accessToken) {
    if (!accessToken) return;
    const headers = { 
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    };
    console.log('[cleanup] Starting database cleanup for test user...');

    // 0. Fetch user profile to get own ID
    const meRes = http.get(url('/users/me'), { headers });
    if (meRes.status !== 200) {
        console.error('[cleanup] Failed to fetch user profile.');
        return;
    }
    let myUser;
    try {
        myUser = JSON.parse(meRes.body).data;
    } catch (e) {
        console.error('[cleanup] Failed to parse user profile response.');
        return;
    }
    const myUserId = myUser?.id;
    if (!myUserId) {
        console.error('[cleanup] User ID not found in profile.');
        return;
    }
    console.log(`[cleanup] Logged in as User ID: ${myUserId} (${myUser?.username})`);

    // 1. Fetch and delete ALL posts created by this user in a paginated loop
    let postsDeletedCount = 0;
    while (true) {
        const postsRes = http.get(url(`/posts?creator_id=${myUserId}&size=100`), { headers });
        if (postsRes.status !== 200) {
            console.error(`[cleanup] Failed to fetch posts: ${postsRes.status}`);
            break;
        }
        let postPage;
        try {
            postPage = JSON.parse(postsRes.body).data;
        } catch (e) {
            break;
        }
        const posts = postPage?.content || [];
        if (posts.length === 0) {
            break;
        }

        console.log(`[cleanup] Found ${posts.length} test posts to delete in this batch.`);
        const batchSize = 100;
        let failedCount = 0;
        for (let i = 0; i < posts.length; i += batchSize) {
            const chunk = posts.slice(i, i + batchSize);
            const reqs = chunk.map(item => [
                'DELETE',
                url(`/posts/${item.id}`),
                null,
                { headers }
            ]);
            const responses = http.batch(reqs);
            responses.forEach((res, index) => {
                if (res.status !== 200 && res.status !== 204) {
                    failedCount++;
                    console.warn(`[cleanup] Failed to delete post ${chunk[index].id}: ${res.status}`);
                }
            });
        }
        if (failedCount > 0) {
            console.warn(`[cleanup] Failed to delete ${failedCount} posts in this batch. Breaking loop to prevent infinite runs.`);
            break;
        }
        postsDeletedCount += posts.length;
        if (posts.length < 100) {
            break;
        }
    }
    if (postsDeletedCount > 0) {
        console.log(`[cleanup] Successfully deleted all ${postsDeletedCount} posts in total.`);
    }

    // 2. Fetch and delete ALL itineraries owned by this user (including manual and AI-generated ones)
    const itisRes = http.get(url('/itineraries/me'), { headers });
    if (itisRes.status === 200) {
        const itis = extractData(itisRes) || [];
        if (itis.length > 0) {
            console.log(`[cleanup] Found ${itis.length} test itineraries to delete.`);
            const batchSize = 100;
            let failedCount = 0;
            for (let i = 0; i < itis.length; i += batchSize) {
                const chunk = itis.slice(i, i + batchSize);
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
                        console.warn(`[cleanup] Failed to delete itinerary ${chunk[index].id}: ${res.status}`);
                    }
                });
            }
            if (failedCount > 0) {
                console.warn(`[cleanup] Failed to delete ${failedCount} itineraries.`);
            } else {
                console.log(`[cleanup] Successfully deleted all ${itis.length} itineraries.`);
            }
        }
    }

    // 3. Fetch and clean up Groups (including location suggestions) and membership
    const groupsRes = http.get(url('/groups'), { headers });
    if (groupsRes.status === 200) {
        const groups = extractData(groupsRes) || [];
        
        // Find groups where I am the LEADER
        const myGroups = groups.filter(g => 
            g.members && g.members.some(m => m.user && m.user.id === myUserId && m.role === 'LEADER')
        );
        
        // Find groups where I am a member but not the leader
        const joinedGroups = groups.filter(g => 
            g.members && g.members.some(m => m.user && m.user.id === myUserId && m.role !== 'LEADER')
        );

        if (myGroups.length > 0) {
            console.log(`[cleanup] Found ${myGroups.length} groups owned by me to clean up.`);
            
            myGroups.forEach(group => {
                // A. Clean up location suggestions inside this group first to avoid orphans
                const sugRes = http.get(url(`/groups/${group.id}/location-suggestions`), { headers });
                if (sugRes.status === 200) {
                    const suggestions = extractData(sugRes) || [];
                    if (suggestions.length > 0) {
                        console.log(`[cleanup] Deleting ${suggestions.length} location suggestions for group: ${group.name}`);
                        const sugReqs = suggestions.map(s => [
                            'DELETE',
                            url(`/groups/${group.id}/location-suggestions/${s.id}`),
                            null,
                            { headers }
                        ]);
                        http.batch(sugReqs);
                    }
                }
                
                // B. Delete the group
                const delRes = http.request('DELETE', url(`/groups/${group.id}`), null, { headers });
                if (delRes.status !== 200 && delRes.status !== 204) {
                    console.warn(`[cleanup] Failed to delete group ${group.id}: ${delRes.status}`);
                } else {
                    console.log(`[cleanup] Deleted group: ${group.name} (${group.id})`);
                }
            });
        }

        if (joinedGroups.length > 0) {
            console.log(`[cleanup] Leaving ${joinedGroups.length} groups...`);
            joinedGroups.forEach(group => {
                const leaveRes = http.request('DELETE', url(`/groups/${group.id}/members/me`), null, { headers });
                if (leaveRes.status !== 200 && leaveRes.status !== 204) {
                    console.warn(`[cleanup] Failed to leave group ${group.id}: ${leaveRes.status}`);
                } else {
                    console.log(`[cleanup] Successfully left group: ${group.name} (${group.id})`);
                }
            });
        }
    }
    console.log('[cleanup] Database cleanup finished.');
}

