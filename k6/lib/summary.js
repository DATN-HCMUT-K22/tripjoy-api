import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

/**
 * Custom summary reporter for TripJoy k6 tests.
 * Displays standard k6 summary + a beautiful table for scenario metrics.
 */
export function handleSummary(data) {
    const scenarioMetrics = [
        { name: 'auth', title: 'AUTHENTICATION' },
        { name: 'read', title: 'READ (GENERIC)' },
        { name: 'social', title: 'SOCIAL/POSTS' },
        { name: 'location', title: 'LOCATION/MAP' },
        { name: 'group', title: 'GROUPS' },
        { name: 'notification', title: 'NOTIFICATIONS' },
        { name: 'itinerary', title: 'ITINERARIES' },
        { name: 'write', title: 'WRITE OPERATIONS' },
        { name: 'search', title: 'SEARCH/FTS' },
    ];

    let customOutput = '\n  █ THRESHOLDS \n\n';
    
    // Standard thresholds output
    const thresholds = data.metrics.http_req_duration ? data.metrics.http_req_duration.thresholds : {};
    Object.keys(thresholds).forEach(key => {
        const success = thresholds[key].ok;
        const icon = success ? '✓' : '✗';
        customOutput += `    ${icon} ${key}\n`;
    });

    customOutput += '\n\n  █ SCENARIO BREAKDOWN \n\n';
    customOutput += '    SCENARIO             | AVG       | P(95)     | MAX       | COUNT\n';
    customOutput += '    ---------------------|-----------|-----------|-----------|-------\n';

    scenarioMetrics.forEach(m => {
        const key = `http_req_duration{scenario:${m.name}}`;
        const metric = data.metrics[key];
        
        if (metric) {
            const avg = (metric.values.avg / 1000).toFixed(2) + 's';
            const p95 = (metric.values['p(95)'] / 1000).toFixed(2) + 's';
            const max = (metric.values.max / 1000).toFixed(2) + 's';
            const count = metric.values.count;
            
            customOutput += `    ${m.title.padEnd(20)} | ${avg.padEnd(9)} | ${p95.padEnd(9)} | ${max.padEnd(9)} | ${count}\n`;
        } else {
            customOutput += `    ${m.title.padEnd(20)} | -         | -         | -         | 0\n`;
        }
    });

    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }) + customOutput,
    };
}
