import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

/**
 * TripJoy k6 — Professional Summary Reporter
 * Fix: Handling cases where duration metrics might be missing for some scenarios (like auth).
 * Feature: Automatic generation of a premium interactive HTML visualization dashboard.
 */
export function handleSummary(data) {
    const scenarioMetrics = [
        { name: 'auth', title: 'AUTHENTICATION' },
        { name: 'read', title: 'READ JOURNEY' },
        { name: 'manage', title: 'MANAGE JOURNEY' },
        { name: 'social', title: 'SOCIAL JOURNEY' },
        { name: 'chat', title: 'CHAT JOURNEY' },
    ];

    let customOutput = '\n\n';
    customOutput += '  TRIPJOY API PERFORMANCE EVALUATION REPORT\n';
    customOutput += '  ------------------------------------------------------------------------------------------\n\n';

    customOutput += '  [ SCENARIO BREAKDOWN ]\n';
    customOutput += '  SCENARIO              | REQS   | SUCCESS | AVG       | P(95)     | P(99)     | MAX      \n';
    customOutput += '  ----------------------|--------|---------|-----------|-----------|-----------|----------\n';

    const allMetricKeys = Object.keys(data.metrics);

    scenarioMetrics.forEach(m => {
        const namePattern = new RegExp(`scenario\\s*:\\s*${m.name}`);
        
        // Find metrics
        const durKey = allMetricKeys.find(k => k.includes('http_req_duration') && namePattern.test(k));
        const failKey = allMetricKeys.find(k => k.includes('http_req_failed') && namePattern.test(k));
        const reqKey = allMetricKeys.find(k => k.startsWith('http_reqs') && namePattern.test(k));
        
        const durMetric = durKey ? data.metrics[durKey] : null;
        const failMetric = failKey ? data.metrics[failKey] : null;
        const reqMetric = reqKey ? data.metrics[reqKey] : null;
        
        let count = 0;
        if (reqMetric && reqMetric.values) count = reqMetric.values.count || 0;
        
        const hasData = count > 0 || (durMetric && durMetric.values && durMetric.values.avg > 0);
        
        if (hasData) {
            const values = durMetric ? durMetric.values : {};
            const failValues = failMetric ? failMetric.values : null;
            const successRate = failValues ? ((1 - failValues.rate) * 100).toFixed(1) + '%' : '100.0%';
            
            const formatTime = (val) => {
                if (val === undefined || val === null || val === 0) return '-'.padStart(6).padEnd(10);
                if (val < 1000) return val.toFixed(0).toString().padStart(4) + ' ms  ';
                return (val / 1000).toFixed(2).toString().padStart(4) + ' s   ';
            };

            const avg = formatTime(values.avg);
            const p95 = formatTime(values['p(95)']);
            const p99 = formatTime(values['p(99)']);
            const max = formatTime(values.max);
            
            customOutput += `  ${m.title.padEnd(21)} | ${count.toString().padEnd(6)} | ${successRate.padStart(7)} | ${avg} | ${p95} | ${p99} | ${max}\n`;
        } else if (m.name !== 'auth') {
            customOutput += `  ${m.title.padEnd(21)} | 0      | 0.0%    | -         | -         | -         | -\n`;
        }
    });

    customOutput += '  ------------------------------------------------------------------------------------------\n\n';

    customOutput += '  [ SLA THRESHOLDS STATUS ]\n';
    const thresholdKeys = allMetricKeys.filter(k => 
        data.metrics[k].thresholds && !k.startsWith('http_reqs{')
    ).sort();
    
    thresholdKeys.forEach(key => {
        const metric = data.metrics[key];
        Object.keys(metric.thresholds).forEach(tKey => {
            const success = metric.thresholds[tKey].ok;
            const status = success ? 'PASS' : 'FAIL';
            let displayName = key;
            if (key.includes('{')) {
                displayName = key.split('{')[1].split('}')[0].replace('scenario:', '').trim().toUpperCase();
            } else {
                displayName = 'OVERALL';
            }
            customOutput += `    ${status.padEnd(8)} | ${displayName.padEnd(12)} | ${key} (${tKey})\n`;
        });
    });
    
    customOutput += '\n';

    return {
        'stdout': customOutput,
        'summary.html': htmlReport(data, { title: 'TripJoy API Performance Evaluation Report' }),
        'reports/summary.html': htmlReport(data, { title: 'TripJoy API Performance Evaluation Report' }),
    };
}
