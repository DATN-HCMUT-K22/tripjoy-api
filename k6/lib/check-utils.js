// Common Check Utilities for k6 Tests

import { check } from 'k6';

/**
 * Check if response has 200 status
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if status is 200
 */
export function checkStatus200(response, name = 'request') {
    return check(response, {
        [`${name}: status is 200`]: (r) => r.status === 200
    });
}

/**
 * Check if response has 201 status (Created)
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if status is 201
 */
export function checkStatus201(response, name = 'request') {
    return check(response, {
        [`${name}: status is 201`]: (r) => r.status === 201
    });
}

/**
 * Check if response has 204 status (No Content)
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if status is 204
 */
export function checkStatus204(response, name = 'request') {
    return check(response, {
        [`${name}: status is 204`]: (r) => r.status === 204
    });
}

/**
 * Check if response body is valid JSON
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if body is valid JSON
 */
export function checkValidJson(response, name = 'request') {
    return check(response, {
        [`${name}: response is valid JSON`]: (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch (e) {
                return false;
            }
        }
    });
}

/**
 * Check if response has standard API response structure
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if has correct structure
 */
export function checkApiResponseStructure(response, name = 'request') {
    return check(response, {
        [`${name}: has valid API structure`]: (r) => {
            try {
                const body = JSON.parse(r.body);
                return body !== null && typeof body === 'object';
            } catch (e) {
                return false;
            }
        }
    });
}

/**
 * Check if response data field exists
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if data field exists
 */
export function checkHasData(response, name = 'request') {
    return check(response, {
        [`${name}: has data field`]: (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== undefined && body.data !== null;
            } catch (e) {
                return false;
            }
        }
    });
}

/**
 * Check if response data is an array
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if data is array
 */
export function checkDataIsArray(response, name = 'request') {
    return check(response, {
        [`${name}: data is array`]: (r) => {
            try {
                const body = JSON.parse(r.body);
                return Array.isArray(body.data);
            } catch (e) {
                return false;
            }
        }
    });
}

/**
 * Check if response data is an object
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if data is object
 */
export function checkDataIsObject(response, name = 'request') {
    return check(response, {
        [`${name}: data is object`]: (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== null && typeof body.data === 'object' && !Array.isArray(body.data);
            } catch (e) {
                return false;
            }
        }
    });
}

/**
 * Check if response has pagination structure (for Page responses)
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if has pagination
 */
export function checkHasPagination(response, name = 'request') {
    return check(response, {
        [`${name}: has pagination`]: (r) => {
            try {
                const body = JSON.parse(r.body);
                const data = body.data;
                return data &&
                    data.content !== undefined &&
                    data.totalElements !== undefined &&
                    data.totalPages !== undefined;
            } catch (e) {
                return false;
            }
        }
    });
}

/**
 * Comprehensive check for 200 OK with data
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if all checks pass
 */
export function checkSuccess(response, name = 'request') {
    return check(response, {
        [`${name}: status is 200`]: (r) => r.status === 200,
        [`${name}: valid JSON`]: (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch (e) {
                return false;
            }
        },
        [`${name}: has data`]: (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== undefined;
            } catch (e) {
                return false;
            }
        }
    });
}

/**
 * Check for 401 Unauthorized
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if 401
 */
export function checkUnauthorized(response, name = 'request') {
    return check(response, {
        [`${name}: status is 401`]: (r) => r.status === 401
    });
}

/**
 * Check for 403 Forbidden
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if 403
 */
export function checkForbidden(response, name = 'request') {
    return check(response, {
        [`${name}: status is 403`]: (r) => r.status === 403
    });
}

/**
 * Check for 404 Not Found
 * @param {Object} response - HTTP response
 * @param {string} name - Name for the check
 * @returns {boolean} True if 404
 */
export function checkNotFound(response, name = 'request') {
    return check(response, {
        [`${name}: status is 404`]: (r) => r.status === 404
    });
}
