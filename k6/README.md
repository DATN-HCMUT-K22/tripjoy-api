# TripJoy API - k6 Load Testing

Stress testing suite for TripJoy REST API using Grafana k6.

## 🚀 Quick Start

### Prerequisites
- k6 installed (`win get install k6` or download from [k6.io](https://k6.io/))
- TripJoy API running (default: `http://localhost:8080`)

### Run Smoke Test (1 minute)
```bash
cd d:/devspace/datn/tripjoy-project/tripjoy-api
k6 run k6/scenarios/smoke-test.js
```

### Run Auth Flow Test
```bash
k6 run k6/tests/auth-test.js
```

## 📁 Project Structure

```
k6/
├── config/
│   ├── dev.js          # Environment configuration
│   └── thresholds.js   # Performance thresholds
├── lib/
│   ├── auth.js         # Authentication helpers
│   ├── utils.js        # Common utilities
│   └── check-utils.js  # Response validation
├── tests/
│   ├── auth-test.js    # Auth endpoints (5)
│   ├── user-test.js    # User management (5)
│   ├── admin-test.js   # Roles + Permissions (6)
│   ├── location-test.js # Location APIs (7)
│   ├── group-test.js   # Groups + Suggestions (14)
│   ├── conversation-test.js # Chat (4)
│   ├── message-test.js # Message actions (4)
│   └── notification-test.js # Notifications (7)
└── scenarios/
    ├── smoke-test.js   # Quick validation
    ├── load-test.js    # Expected traffic
    └── stress-test.js  # Peak traffic
```

## 🧪 Test Scenarios

### Smoke Test
Quick validation that the system is working.
- **VUs**: 2
- **Duration**: 1 minute
- **Endpoints**: Critical paths only (auth, user, groups, notifications)
- **Purpose**: CI/CD health check

```bash
k6 run k6/scenarios/smoke-test.js
```

### Load Test
Test with expected normal traffic.
- **VUs**: Ramp 0 → 20 → 0
- **Duration**: 7 minutes
- **Endpoints**: All implemented APIs
- **Purpose**: Validate performance under normal load

```bash
k6 run k6/scenarios/load-test.js
```

### Stress Test
Find the breaking point.
- **VUs**: Ramp 0 → 100 → 0
- **Duration**: 10 minutes
- **Purpose**: Identify maximum capacity

```bash
k6 run k6/scenarios/stress-test.js
```

## 🔧 Configuration

### Environment Variables
```bash
# Change base URL
k6 run --env BASE_URL=http://api.tripjoy.com k6/scenarios/smoke-test.js

# Enable verbose logging
k6 run --env VERBOSE=true k6/tests/auth-test.js
```

### Default Test Users
Configured in `k6/config/dev.js`:
- **Admin**: `admin` / `admin123`
- **User 1**: `testuser1` / `Test123!`
- **User 2**: `testuser2` / `Test123!`

## 📊 Performance Thresholds

### Success Criteria
- **p95 response time**: < 1000ms
- **p99 response time**: < 2000ms
- **Error rate**: < 1%
- **Max response time**: < 5000ms

## 🎯 Implemented Endpoints (51 total)

### Core (16 endpoints)
- ✅ Auth: 5 endpoints
- ✅ User: 5 endpoints
- ✅ Roles: 3 endpoints
- ✅ Permissions: 3 endpoints

### Business Logic (24 endpoints)
- ✅ Location: 7 endpoints
- ✅ Group: 11 endpoints
- ✅ Location Suggestions: 3 endpoints
- ✅ Conversation: 3 endpoints (partial)

### Social & Notifications (11 endpoints)
- ✅ Message Actions: 4 endpoints
- ✅ Notifications: 7 endpoints

## 📖 Reading Results

### Key Metrics
- `http_req_duration`: Response time (avg, p95, p99, max)
- `http_req_failed`: Error rate
- `http_reqs`: Requests per second
- `vus`: Virtual users

### Example Output
```
✓ login: status 200
✓ login: has token

checks.........................: 100.00% ✓ 450   ✗ 0
http_req_duration..............: avg=234ms p95=456ms p99=678ms max=1.2s
http_req_failed................: 0.00%   ✓ 0     ✗ 450
http_reqs......................: 450     7.5/s
```

## 🛠️ Best Practices

1. **Start Small**: Run smoke test first before load/stress tests
2. **Isolated Environment**: Test against a dedicated test server
3. **Data Cleanup**: Tests create data - clean up periodically
4. **Monitor Server**: Watch server CPU/memory during tests
5. **Gradual Ramp**: Use ramp-up/down to avoid shocking the system

## 🐛 Troubleshooting

### "Connection refused"
- Check if API is running on `localhost:8080`
- Set correct `BASE_URL` environment variable

### "Login failed"
- Verify test users exist in database
- Check credentials in `k6/config/dev.js`

### High error rates
- Server may be overloaded
- Reduce VUs or check server logs

## 🔗 Resources

- [k6 Documentation](https://k6.io/docs/)
- [k6 Examples](https://k6.io/docs/examples/)
- [TripJoy API Documentation](../docs/)

## 📝 Notes

- Test data is generated with random usernames/emails
- Each VU (virtual user) simulates one concurrent user
- Tests are stateless - each iteration is independent
- Default users should exist in DB before running tests
