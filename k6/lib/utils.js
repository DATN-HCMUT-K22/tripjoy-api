// Common Utility Functions for k6 Tests

/**
 * Helper function to pick random item from array
 * @param {Array} arr - Array to pick from
 * @returns {any} Random item
 */
function randomItem(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

/**
 * Helper function to generate random integer between min and max (inclusive)
 * @param {number} min - Minimum value
 * @param {number} max - Maximum value
 * @returns {number} Random integer
 */
function randomIntBetween(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Helper function to generate random string
 * @param {number} length - Length of string
 * @returns {string} Random string
 */
function randomString(length) {
    const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
        result += chars[Math.floor(Math.random() * chars.length)];
    }
    return result;
}

/**
 * Generate a random username
 * @returns {string} Random username
 */
export function generateUsername() {
    const adjectives = ['swift', 'brave', 'clever', 'happy', 'lucky', 'mighty', 'wise', 'cool'];
    const nouns = ['traveler', 'explorer', 'wanderer', 'nomad', 'adventurer', 'voyager'];

    const adj = randomItem(adjectives);
    const noun = randomItem(nouns);
    const num = randomIntBetween(100, 9999);

    return `${adj}_${noun}_${num}`;
}

/**
 * Generate a random email
 * @param {string} username - Optional username to base email on
 * @returns {string} Random email
 */
export function generateEmail(username) {
    const domains = ['gmail.com', 'yahoo.com', 'outlook.com', 'test.com'];
    const user = username || `user_${randomString(8)}`;
    const domain = randomItem(domains);

    return `${user}@${domain}`;
}

/**
 * Generate random user data for registration
 * @returns {Object} User data object
 */
export function generateUserData() {
    const username = generateUsername();

    return {
        username: username,
        email: generateEmail(username),
        password: 'Test123!@#',
        fullName: `${username} Test`
    };
}

/**
 * Generate a random group name
 * @returns {string} Random group name
 */
export function generateGroupName() {
    const destinations = ['Bali', 'Tokyo', 'Paris', 'NYC', 'London', 'Rome', 'Seoul', 'Bangkok'];
    const years = ['2024', '2025'];
    const seasons = ['Spring', 'Summer', 'Fall', 'Winter'];

    const dest = randomItem(destinations);
    const season = randomItem(seasons);
    const year = randomItem(years);

    return `${dest} ${season} ${year} Trip`;
}

/**
 * Generate random location data
 * @returns {Object} Location data
 */
export function generateLocationData() {
    const cities = [
        { name: 'Ho Chi Minh', lat: 10.8231, lng: 106.6297 },
        { name: 'Hanoi', lat: 21.0285, lng: 105.8542 },
        { name: 'Da Nang', lat: 16.0544, lng: 108.2022 }
    ];

    const city = randomItem(cities);
    const placeTypes = ['Restaurant', 'Hotel', 'Beach', 'Museum', 'Park', 'Market'];
    const placeType = randomItem(placeTypes);

    return {
        name: `Amazing ${placeType} in ${city.name}`,
        address: `${randomIntBetween(1, 999)} Test St, ${city.name}`,
        city: city.name,
        latitude: city.lat + (Math.random() - 0.5) * 0.1,
        longitude: city.lng + (Math.random() - 0.5) * 0.1,
        category: placeType.toUpperCase(),
        description: `A wonderful ${placeType.toLowerCase()} for your trip`
    };
}

/**
 * Validate UUID format
 * @param {string} uuid - UUID string to validate
 * @returns {boolean} True if valid UUID
 */
export function isValidUUID(uuid) {
    if (!uuid || typeof uuid !== 'string') return false;

    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    return uuidRegex.test(uuid);
}

/**
 * Wait/sleep for specified milliseconds
 * @param {number} ms - Milliseconds to sleep
 */
export function sleep(ms) {
    const start = Date.now();
    while (Date.now() - start < ms) { }
}

/**
 * Extract data from API response
 * @param {Object} response - HTTP response
 * @returns {any} Data field from response or null
 */
export function extractData(response) {
    try {
        const body = JSON.parse(response.body);
        return body.data || null;
    } catch (e) {
        console.error('Failed to parse response body:', e);
        return null;
    }
}

/**
 * Extract error message from API response
 * @param {Object} response - HTTP response
 * @returns {string} Error message or empty string
 */
export function extractError(response) {
    try {
        const body = JSON.parse(response.body);
        return body.message || body.error || '';
    } catch (e) {
        return response.body || '';
    }
}

/**
 * Log test info (only in verbose mode)
 * @param {string} message - Message to log
 * @param {any} data - Optional data to log
 */
export function logInfo(message, data) {
    if (__ENV.VERBOSE === 'true') {
        if (data !== undefined) {
            console.log(`[INFO] ${message}:`, JSON.stringify(data));
        } else {
            console.log(`[INFO] ${message}`);
        }
    }
}

/**
 * Log error
 * @param {string} message - Error message
 * @param {any} error - Optional error data
 */
export function logError(message, error) {
    if (error !== undefined) {
        console.error(`[ERROR] ${message}:`, JSON.stringify(error));
    } else {
        console.error(`[ERROR] ${message}`);
    }
}
