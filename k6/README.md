# TripJoy API - K6 Performance Test Suite v2.0

> Production-ready performance evaluation framework using Grafana k6.
> Full documentation: [../docs/K6_PERFORMANCE_TESTING_GUIDE.md](../docs/K6_PERFORMANCE_TESTING_GUIDE.md)

---

## Quick Start

### Prerequisites
- k6 ≥ 0.50.0 installed (`winget install k6` or [k6.io](https://k6.io))
- TripJoy API running at `http://localhost:8080`
- Test users created (see [docs guide](../docs/K6_PERFORMANCE_TESTING_GUIDE.md#2-cài-đặt-và-yêu-cầu))

### First run — Smoke Test (1 VU, 2 min)
```bash
cd k6/
npm run test:smoke
```

---

## Available Tests

| Command | Type | VUs | Duration | Purpose |
|---|---|---|---|---|
| `npm run test:smoke` | Smoke | 1 | 2m | Health check after deploy |
| `npm run test:load` | Load | 0→50 | 20m | Normal traffic simulation |
| `npm run test:stress` | Stress | 0→200 | 18m | Find breaking point |
| `npm run test:soak` | Soak | 30 | 30m | Memory leak detection |
| `npm run test:spike` | Spike | 5→200 | 7m | Burst traffic / launch day |
| `npm run test:ai` | AI | 3 | 10m | AI endpoint validation |

### With JSON report output
```bash
npm run test:smoke:report    # → reports/smoke-results.json
npm run test:load:report     # → reports/load-results.json
npm run test:all:report      # Run all + save reports
```

### Staging environment
```bash
npm run test:smoke:staging
npm run test:load:staging
```

---

## Structure

```
k6/
├── config/
│   ├── environments.js   # URL + credentials per environment
│   └── thresholds.js     # SLO definitions (smoke/load/stress/soak/spike)
├── lib/
│   ├── auth.js           # login, register, introspect, refresh, logout
│   ├── http.js           # tagged HTTP wrappers + custom metrics
│   ├── generators.js     # realistic fake data generators
│   └── scenarios.js      # domain-level reusable user journeys
├── scenarios/
│   ├── smoke-test.js
│   ├── load-test.js
│   ├── stress-test.js
│   ├── soak-test.js
│   ├── spike-test.js
│   └── ai-test.js
├── reports/              # JSON output (auto-created)
└── package.json
```

---

## Performance SLOs

| Test | p(95) target | Error rate limit | Checks |
|---|---|---|---|
| Smoke | < 500ms | < 0.1% | > 99% |
| Load | < 1500ms | < 1% | > 95% |
| Stress | < 3000ms | < 5% | > 90% |
| Soak | < 2000ms (no drift) | < 1% | > 95% |
| Spike | < 5000ms | < 10% | > 85% |

---

## API Coverage

**Fully tested:** Auth, Users, Groups, Locations, Posts, Comments, Itinerary, Expenses, Notifications, Conversations, Messages, Travel Notebooks, Media  
**Excluded (not implemented):** Feedback, Report, Admin moderation, OAuth2  

See [API Coverage Map](../docs/K6_PERFORMANCE_TESTING_GUIDE.md#4-api-coverage-map) for full table.
