/**
 * TripJoy k6 — Scenario Modules (Domain-level test flows)
 *
 * Each function represents a self-contained user journey (scenario).
 * All scenarios receive `{ token, headers }` and optional pre-created IDs.
 */

import { sleep } from 'k6';
import { url } from '../config/environments.js';
import { get, post, put, del, patch, expectSuccess, expectList, extractId, extractData } from './http.js';
import {
    generateGroupPayload,
    generatePostPayload,
    generateCommentPayload,
    generateItineraryPayload,
    generateExpensePayload,
    generateMessagePayload,
    randomCity,
} from './generators.js';

// ──────────────────────────────────────────────────────────────
// 1. USER SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-USER-01: Get my profile
 */
export function scenarioGetMyProfile(headers) {
    const res = get(url('/users/me'), headers, 'GET /users/me');
    expectSuccess(res, 'get-my-profile');
    sleep(0.3);
}

/**
 * S-USER-02: Update my profile
 */
export function scenarioUpdateProfile(headers) {
    const res = patch(
        url('/users/me'),
        { fullName: `K6 Tester ${Date.now()}`, bio: 'Testing with k6' },
        headers,
        'PATCH /users/me'
    );
    expectSuccess(res, 'update-profile');
    sleep(0.3);
}

/**
 * S-USER-03: Search users
 */
export function scenarioSearchUsers(headers) {
    const queries = ['test', 'nguyen', 'tran', 'admin'];
    const q = queries[Math.floor(Math.random() * queries.length)];
    const res = get(url(`/users/search?q=${q}&page=0&size=10`), headers, 'GET /users/search');
    expectList(res, 'search-users');
    sleep(0.2);
}

/**
 * S-USER-04: Get public profile of a user by ID
 */
export function scenarioGetPublicProfile(headers, userId) {
    if (!userId) return;
    const res = get(url(`/users/${userId}/profile`), headers, 'GET /users/{id}/profile');
    expectSuccess(res, 'get-public-profile');
    sleep(0.2);
}

// ──────────────────────────────────────────────────────────────
// 2. GROUP SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-GROUP-01: Get my groups list
 */
export function scenarioGetMyGroups(headers) {
    const res = get(url('/groups'), headers, 'GET /groups');
    expectSuccess(res, 'get-my-groups');
    sleep(0.3);
    return extractData(res);
}

/**
 * S-GROUP-02: Create group + get details
 */
export function scenarioCreateGroup(headers) {
    const payload = generateGroupPayload();
    const res = post(url('/groups'), payload, headers, 'POST /groups');
    expectSuccess(res, 'create-group');
    const groupId = extractId(res);
    if (groupId) {
        sleep(0.3);
        const detailRes = get(url(`/groups/${groupId}`), headers, 'GET /groups/{id}');
        expectSuccess(detailRes, 'get-group-detail');
    }
    sleep(0.5);
    return groupId;
}

/**
 * S-GROUP-03: Get members of a group
 */
export function scenarioGetGroupMembers(headers, groupId) {
    if (!groupId) return;
    const res = get(url(`/groups/${groupId}/members`), headers, 'GET /groups/{id}/members');
    expectList(res, 'get-group-members');
    sleep(0.2);
}

/**
 * S-GROUP-04: Get location suggestions for a group
 */
export function scenarioGetGroupSuggestions(headers, groupId) {
    if (!groupId) return;
    const res = get(url(`/groups/${groupId}/location-suggestions`), headers, 'GET /groups/{id}/location-suggestions');
    expectSuccess(res, 'get-group-suggestions');
    sleep(0.2);
}

/**
 * S-GROUP-05: Search groups by name
 */
export function scenarioSearchGroups(headers) {
    const queries = ['bali', 'tokyo', 'trip', 'adventure'];
    const q = queries[Math.floor(Math.random() * queries.length)];
    const res = get(url(`/groups/search?q=${q}`), headers, 'GET /groups/search');
    expectSuccess(res, 'search-groups');
    sleep(0.3);
}

// ──────────────────────────────────────────────────────────────
// 3. LOCATION SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-LOC-01: Search locations (full-text)
 */
export function scenarioSearchLocations(headers) {
    const searches = [
        { q: 'cafe', type: 'POI', city: 'Ho Chi Minh City' },
        { q: 'hotel', country: 'VN' },
        { q: 'museum', type: 'POI' },
        { q: 'beach', city: 'Da Nang' },
    ];
    const params = searches[Math.floor(Math.random() * searches.length)];
    let queryStr = `/locations/search?page=0&size=20`;
    if (params.q) queryStr += `&q=${encodeURIComponent(params.q)}`;
    if (params.type) queryStr += `&type=${params.type}`;
    if (params.city) queryStr += `&city=${encodeURIComponent(params.city)}`;
    if (params.country) queryStr += `&country=${params.country}`;

    const res = get(url(queryStr), headers, 'GET /locations/search');
    expectSuccess(res, 'search-locations');
    sleep(0.4);
}

/**
 * S-LOC-02: Nearby locations (PostGIS)
 */
export function scenarioNearbyLocations(headers) {
    const city = randomCity();
    const res = get(
        url(`/locations/nearby?lat=${city.lat}&lng=${city.lng}&radius=5000&type=POI&limit=20`),
        headers,
        'GET /locations/nearby'
    );
    expectSuccess(res, 'nearby-locations');
    sleep(0.4);
}

/**
 * S-LOC-03: Location autocomplete
 */
export function scenarioLocationAutocomplete(headers) {
    const queries = ['highlands', 'pho', 'hotel', 'cafe', 'beach'];
    const q = queries[Math.floor(Math.random() * queries.length)];
    const res = get(url(`/locations/autocomplete?q=${q}`), headers, 'GET /locations/autocomplete');
    expectSuccess(res, 'location-autocomplete');
    sleep(0.3);
}

/**
 * S-LOC-04: Get administrative locations (provinces)
 */
export function scenarioGetAdministrativeLocations(headers) {
    const res = get(url('/locations/administrative?type=PROVINCE&country=VN'), headers, 'GET /locations/administrative');
    expectSuccess(res, 'get-administrative-locations');
    sleep(0.2);
}

// ──────────────────────────────────────────────────────────────
// 4. POST SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-POST-01: Browse post feed
 */
export function scenarioBrowseFeed(headers) {
    const res = get(url('/posts?page=0&size=20'), headers, 'GET /posts (feed)');
    expectSuccess(res, 'browse-feed');
    sleep(0.5);
    return extractData(res);
}

/**
 * S-POST-02: Search posts
 */
export function scenarioSearchPosts(headers) {
    const queries = ['Đà Nẵng', 'Hội An', 'travel', 'food', 'adventure'];
    const q = queries[Math.floor(Math.random() * queries.length)];
    const res = get(url(`/posts?q=${encodeURIComponent(q)}&page=0&size=10`), headers, 'GET /posts (search)');
    expectSuccess(res, 'search-posts');
    sleep(0.4);
}

/**
 * S-POST-03: Create a post + interact with it
 */
export function scenarioCreatePost(headers, itineraryId = null) {
    // Every post MUST have an itineraryId now. If not provided, the request will fail with 400.
    const payload = generatePostPayload(itineraryId);
    const res = post(url('/posts'), payload, headers, 'POST /posts');
    
    if (res.status === 400 && !itineraryId) {
        console.warn('[social] Post creation failed: missing itinerary_id. Check load-test.js logic.');
    }

    expectSuccess(res, 'create-post');
    const postId = extractId(res);

    if (postId) {
        sleep(0.3);
        // Like it
        const likeRes = post(url(`/posts/${postId}/likes`), {}, headers, 'POST /posts/{id}/likes');
        expectSuccess(likeRes, 'like-post');

        sleep(0.3);
        // Get comments
        const commentsRes = get(url(`/posts/${postId}/comments?page=0&size=10`), headers, 'GET /posts/{id}/comments');
        expectSuccess(commentsRes, 'get-post-comments');

        sleep(0.3);
        // Add comment
        const commentPayload = generateCommentPayload(postId);
        const commentRes = post(url(`/posts/${postId}/comments`), commentPayload, headers, 'POST /posts/{id}/comments');
        expectSuccess(commentRes, 'create-comment');
        const commentId = extractId(commentRes);

        if (commentId) {
            sleep(0.3);
            // Like comment
            const likeCommentRes = post(url(`/comments/${commentId}/likes`), {}, headers, 'POST /comments/{id}/likes');
            expectSuccess(likeCommentRes, 'like-comment');

            sleep(0.3);
            // Get replies
            const repliesRes = get(url(`/comments/${commentId}/replies?page=0&size=10`), headers, 'GET /comments/{id}/replies');
            expectSuccess(repliesRes, 'get-comment-replies');
        }

        sleep(0.3);
        // Unlike post  
        del(url(`/posts/${postId}/likes`), headers, 'DELETE /posts/{id}/likes');
    }

    sleep(0.5);
    return postId;
}

/**
 * S-POST-04: Save / unsave a post
 */
export function scenarioSavePost(headers, postId) {
    if (!postId) return;
    const saveRes = post(url(`/posts/${postId}/saves`), {}, headers, 'POST /posts/{id}/saves');
    expectSuccess(saveRes, 'save-post');
    sleep(0.3);
    // Get saved posts
    const savedRes = get(url('/posts/my-saves?page=0&size=10'), headers, 'GET /posts/my-saves');
    expectSuccess(savedRes, 'get-saved-posts');
    sleep(0.3);
}

// ──────────────────────────────────────────────────────────────
// 5. ITINERARY SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-ITIS-01: Create itinerary + add trip items + expenses
 */
export function scenarioCreateItinerary(headers, groupId) {
    if (!groupId) return null;

    const payload = generateItineraryPayload(groupId);
    const res = post(url('/itineraries'), payload, headers, 'POST /itineraries');
    expectSuccess(res, 'create-itinerary');
    const itineraryId = extractId(res);

    if (itineraryId) {
        sleep(0.3);
        // Get itinerary
        const getRes = get(url(`/itineraries/${itineraryId}`), headers, 'GET /itineraries/{id}');
        expectSuccess(getRes, 'get-itinerary');

        sleep(0.3);
        // Get trip items
        const itemsRes = get(url(`/itineraries/${itineraryId}/items`), headers, 'GET /itineraries/{id}/items');
        expectSuccess(itemsRes, 'get-trip-items');

        sleep(0.3);
        // Add expense
        const expPayload = generateExpensePayload();
        const expRes = post(url(`/itineraries/${itineraryId}/expenses`), expPayload, headers, 'POST /itineraries/{id}/expenses');
        expectSuccess(expRes, 'add-expense');
        const expenseId = extractId(expRes);

        if (expenseId) {
            sleep(0.3);
            // Get all expenses
            const allExpRes = get(url(`/itineraries/${itineraryId}/expenses`), headers, 'GET /itineraries/{id}/expenses');
            expectSuccess(allExpRes, 'get-expenses');

            sleep(0.3);
            // Remove expense
            del(url(`/itineraries/${itineraryId}/expenses/${expenseId}`), headers, 'DELETE /itineraries/{id}/expenses/{expId}');
        }

        sleep(0.3);
        // Favorite the itinerary
        const favRes = post(url(`/itineraries/${itineraryId}/favorites`), {}, headers, 'POST /itineraries/{id}/favorites');
        expectSuccess(favRes, 'favorite-itinerary');

        sleep(0.3);
        // Unfavorite
        del(url(`/itineraries/${itineraryId}/favorites`), headers, 'DELETE /itineraries/{id}/favorites');
    }

    sleep(0.5);
    return itineraryId;
}

// ──────────────────────────────────────────────────────────────
// 6. NOTIFICATION SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-NOTIF-01: Check notification inbox
 */
export function scenarioCheckNotifications(headers) {
    const unreadRes = get(url('/notifications/unread-count'), headers, 'GET /notifications/unread-count');
    expectSuccess(unreadRes, 'get-unread-count');
    sleep(0.2);

    const listRes = get(url('/notifications?page=0&size=20'), headers, 'GET /notifications');
    expectSuccess(listRes, 'get-notifications');
    sleep(0.3);
}

/**
 * S-NOTIF-02: Mark all notifications as read
 */
export function scenarioMarkAllNotificationsRead(headers) {
    const res = put(url('/notifications/mark-all-read'), {}, headers, 'PUT /notifications/mark-all-read');
    expectSuccess(res, 'mark-all-read');
    sleep(0.2);
}

// ──────────────────────────────────────────────────────────────
// 7. CONVERSATION / CHAT SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-CHAT-01: Browse conversations inbox
 */
export function scenarioGetConversations(headers) {
    const res = get(url('/conversations'), headers, 'GET /conversations');
    expectSuccess(res, 'get-conversations');
    sleep(0.3);
    return extractData(res);
}

/**
 * S-CHAT-02: Read messages from a conversation
 */
export function scenarioReadMessages(headers, conversationId) {
    if (!conversationId) return;

    // Mark as read
    put(url(`/conversations/${conversationId}/read`), {}, headers, 'PUT /conversations/{id}/read');
    sleep(0.2);

    // Load message history
    const histRes = get(url(`/conversations/${conversationId}/messages?limit=30`), headers, 'GET /conversations/{id}/messages');
    expectSuccess(histRes, 'get-messages');
    sleep(0.3);

    // Get pinned messages
    const pinnedRes = get(url(`/conversations/${conversationId}/pinned-messages`), headers, 'GET /conversations/{id}/pinned-messages');
    expectSuccess(pinnedRes, 'get-pinned-messages');
    sleep(0.2);
}

/**
 * S-CHAT-03: Send a message
 */
export function scenarioSendMessage(headers, conversationId) {
    if (!conversationId) return;
    const payload = generateMessagePayload();
    const res = post(url(`/conversations/${conversationId}/messages`), payload, headers, 'POST /conversations/{id}/messages');
    expectSuccess(res, 'send-message');
    sleep(0.3);
    return extractId(res);
}

/**
 * S-CHAT-04: Search messages globally
 */
export function scenarioSearchMessages(headers) {
    const queries = ['đi', 'hotel', 'lịch trình', 'book', 'trip'];
    const q = queries[Math.floor(Math.random() * queries.length)];
    const res = get(url(`/messages/search?q=${encodeURIComponent(q)}&page=0&size=10`), headers, 'GET /messages/search');
    expectSuccess(res, 'search-messages-global');
    sleep(0.3);
}

// ──────────────────────────────────────────────────────────────
// 8. TRAVEL NOTEBOOK SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-NB-01: Get notebook by itinerary ID
 */
export function scenarioGetNotebook(headers, itineraryId) {
    if (!itineraryId) return;
    const res = get(url(`/notebooks/${itineraryId}/itinerary`), headers, 'GET /notebooks/{itineraryId}/itinerary');
    // notebook might not exist yet — 404 is acceptable for general load test
    const ok = res.status === 200 || res.status === 404;
    if (!ok) {
        console.warn(`[notebook] unexpected status ${res.status}`);
    }
    sleep(0.3);
}

// ──────────────────────────────────────────────────────────────
// 9. MEDIA SCENARIOS
// ──────────────────────────────────────────────────────────────

/**
 * S-MEDIA-01: Get signed upload credentials
 */
export function scenarioGetUploadSignature(headers) {
    const res = get(url('/media/sign?folder=tripjoy/posts'), headers, 'GET /media/sign');
    expectSuccess(res, 'get-upload-signature');
    sleep(0.2);
}

export default {};
