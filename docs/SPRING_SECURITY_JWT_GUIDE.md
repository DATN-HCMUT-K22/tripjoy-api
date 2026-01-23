# Spring Security với JWT - TripJoy API Documentation

> Tài liệu chi tiết về Spring Security và JWT Authentication Implementation

## 📚 Mục Lục

1. [JWT Basics](#1-jwt-basics)
2. [Spring Security Core Concepts](#2-spring-security-core-concepts)
3. [TripJoy Authentication Architecture](#3-tripjoy-authentication-architecture)
4. [Configuration](#4-configuration)
5. [JWT Implementation](#5-jwt-implementation)
6. [Authentication Flows](#6-authentication-flows)
7. [API Examples](#7-api-examples)
8. [Testing](#8-testing)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. JWT Basics

### 1.1. JWT Structure

JWT (JSON Web Token) có 3 phần, phân cách bởi dấu chấm (`.`):

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyLWlkIn0.signature
└─────HEADER─────┘ └──────PAYLOAD──────┘ └─SIGNATURE─┘
```

| Part | Content | Example |
|------|---------|---------|
| **Header** | Algorithm & token type | `{"alg":"HS512","typ":"JWT"}` |
| **Payload** | Claims (user data) | `{"sub":"user-id","exp":1706003600}` |
| **Signature** | Cryptographic signature | `HMACSHA512(header.payload, secret)` |

### 1.2. Access Token vs Refresh Token

```
┌────────────────────────────────────┐     ┌────────────────────────────────────┐
│        ACCESS TOKEN                │     │        REFRESH TOKEN               │
├────────────────────────────────────┤     ├────────────────────────────────────┤
│ Lifespan:  1 hour                  │     │ Lifespan:  4 days                  │
│ Purpose:   API requests            │     │ Purpose:   Get new access token    │
│ Contains:  Permissions, roles      │     │ Contains:  User ID only            │
│ Usage:     Every API call          │     │ Usage:     Only when refreshing    │
│ Risk:      High exposure           │     │ Risk:      Low exposure            │
└────────────────────────────────────┘     └────────────────────────────────────┘
```

**Why two tokens?**

✅ **Security:** Short-lived access token limits damage if stolen  
✅ **UX:** Long-lived refresh token prevents frequent re-login  
✅ **Control:** Can revoke refresh tokens to force logout

---

## 2. Spring Security Core Concepts

### 2.1. Authentication vs Authorization

```
┌─────────────────────── AUTHENTICATION ───────────────────────┐
│                                                              │
│  Question: "Who are you?"                                    │
│  Process:  Verify identity (username + password / JWT)       │
│  Result:   Authentication object                             │
│                                                              │
│  Example:                                                    │
│    Input:  Username "user1" + Password "pass123"             │
│    Output: Authentication { principal: "user1", ... }        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                               ↓
┌─────────────────────── AUTHORIZATION ────────────────────────┐
│                                                              │
│  Question: "What can you do?"                                │
│  Process:  Check permissions & roles                         │
│  Result:   Access granted or denied                          │
│                                                              │
│  Example:                                                    │
│    Check:  Does user have "ROLE_ADMIN"?                      │
│    Result: YES → Allow access  /  NO → 403 Forbidden         │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 2.2. Security Filter Chain

Request đi qua chuỗi filters trước khi đến controller:

```
HTTP Request → Filter 1 → Filter 2 → ... → Controller
```

**TripJoy Filter Chain:**

```
1. CorsFilter              → Check CORS headers
2. CsrfFilter              → Disabled for REST API
3. AuthorizationFilter     → Check if endpoint is public
4. OAuth2ResourceServer    → Validate JWT token
5. AuthorizationFilter     → Check roles/permissions
```

### 2.3. SecurityContext

Store authentication info cho current thread:

```java
// Get current user
Authentication auth = Secur ityContextHolder.getContext().getAuthentication();
String userId = auth.getName();  // User ID from JWT "sub" claim
Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
```

---

## 3. TripJoy Authentication Architecture

### 3.1. Components

```
┌──────────────── CLIENT ────────────────┐
│  Web / Mobile / Postman                │
└────────────────┬───────────────────────┘
                 │ HTTP
                 ↓
┌──────────────── CONTROLLER LAYER ──────────────────┐
│  AuthenticationController                          │
│  • POST /auth/login                                │
│  • POST /auth/register                             │
│  • POST /auth/refresh                              │
│  • POST /auth/logout                               │
└────────────────┬───────────────────────────────────┘
                 ↓
┌──────────────── SERVICE LAYER ─────────────────────┐
│  AuthenticationService                             │
│  • authenticate()  - Verify credentials            │
│  • refreshToken()  - Issue new tokens              │
│  • logout()        - Invalidate token              │
└────────────────┬───────────────────────────────────┘
                 ↓
┌──────────────── SECURITY LAYER ────────────────────┐
│  JwtUtils                                          │
│  • generateToken()        - Create access token    │
│  • generateRefreshToken() - Create refresh token   │
│  • verifyToken()          - Validate token         │
└────────────────┬───────────────────────────────────┘
                 ↓
┌──────────────── DATA LAYER ────────────────────────┐
│  UserRepository          InvalidatedTokenRepository│
│  • Find user by ID       • Check if token revoked  │
│  • Save user             • Save invalidated tokens │
└────────────────────────────────────────────────────┘
```

### 3.2. Database Tables

```sql
-- Store user accounts
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),  -- BCrypt hashed
    ...
);

-- Store invalidated tokens (logout, refresh rotation)
CREATE TABLE invalidated_token (
    id UUID PRIMARY KEY,        -- JWT ID (jti claim)
    expires_at TIMESTAMP
);
```

---

## 4. Configuration

### 4.1. Application Properties

**File:** `application-dev.yml`

```yaml
jwt:
  signer-key: ${JWT_SIGNER_KEY}           # Secret key
  expiration: ${JWT_EXPIRATION}            # 3600 (1 hour)
  refresh-expiration: ${JWT_REFRESH_EXPIRATION}  # 360000 (~4 days)
```

**File:** `.env`

```properties
JWT_SIGNER_KEY=your-256-bit-secret-key-here
JWT_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=360000
```

### 4.2. SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    // Endpoints không cần authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/refresh"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Configure endpoint access
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            
            // 2. Configure JWT processing
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(customJwtDecoder)
                    .jwtAuthenticationConverter(jwtConverter())
                )
            )
            
            // 3. Disable CSRF (stateless API)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 4. Configure CORS
            .cors(cors -> cors.configurationSource(corsSource()))
            
            // 5. Stateless sessions
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtConverter() {
        JwtGrantedAuthoritiesConverter authConverter = 
            new JwtGrantedAuthoritiesConverter();
        authConverter.setAuthorityPrefix("");  // No "SCOPE_" prefix
        
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authConverter);
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000");  // React
        config.addAllowedOrigin("http://localhost:5173");  // Vite
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**Key Points:**

1. **PUBLIC_ENDPOINTS:** No authentication needed
2. **oauth2ResourceServer:** Spring Security auto-validates JWT
3. **STATELESS:** No server-side sessions
4. **CORS:** Allow frontend requests

### 4.3. CustomJwtDecoder.java

```java
@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtUtils jwtUtils;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Validate token
            SignedJWT signedJWT = jwtUtils.verifyToken(token, false);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            
            // Convert to Spring Security Jwt
            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(signedJWT.getHeader().toJSONObject()))
                    .claims(c -> c.putAll(claims.getClaims()))
                    .build();
                    
        } catch (Exception e) {
            throw new JwtException("Invalid token", e);
        }
    }
}
```

---

## 5. JWT Implementation

### 5.1. JwtUtils.java

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    private final InvalidatedTokenRepository invalidatedTokenRepo;

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION;  // 3600 seconds (1 hour)

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_EXPIRATION;  // 360000 seconds (~4 days)

    /**
     * Generate Access Token
     */
    public String generateToken(User user) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())        // User ID
                .issuer("tripjoy")
                .issueTime(new Date())
                .expirationTime(new Date(
                    Instant.now().plus(EXPIRATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())     // Unique ID
                .claim("scope", buildScope(user))         // Permissions
                .claim("userId", user.getId().toString())
                .claim("type", "access")                  // Token type
                .build();

        return signToken(claims);
    }

    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(User user) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer("tripjoy")
                .issueTime(new Date())
                .expirationTime(new Date(
                    Instant.now().plus(REFRESH_EXPIRATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "refresh")                 // IMPORTANT!
                .build();

        return signToken(claims);
    }

    /**
     * Verify Token
     */
    public SignedJWT verifyToken(String token, boolean isRefresh) 
            throws JOSEException, ParseException {
        
        // 1. Parse token
        SignedJWT signedJWT = SignedJWT.parse(token);
        
        // 2. Verify signature
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        // 3. Check expiration
        Date expiresAt = isRefresh
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                    .toInstant().plus(REFRESH_EXPIRATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        
        if (expiresAt.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        // 4. Check if invalidated
        UUID tokenId = UUID.fromString(signedJWT.getJWTClaimsSet().getJWTID());
        if (invalidatedTokenRepo.existsById(tokenId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        return signedJWT;
    }

    /**
     * Build scope from user roles & permissions
     */
    private String buildScope(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                joiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(perm -> {
                        joiner.add(perm.getName());
                    });
                }
            });
        }
        return joiner.toString();
    }

    /**
     * Sign token with HMAC-SHA512
     */
    private String signToken(JWTClaimsSet claims) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
            JWSObject jwsObject = new JWSObject(header, new Payload(claims.toJSONObject()));
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Cannot sign token", e);
        }
    }
}
```

### 5.2. Token Claims Comparison

| Claim | Access Token | Refresh Token |
|-------|--------------|---------------|
| `sub` | User UUID | User UUID |
| `iss` | "tripjoy" | "tripjoy" |
| `iat` | Issue time | Issue time |
| `exp` | Now + 1 hour | Now + 4 days |
| `jti` | Random UUID | Random UUID |
| `scope` | "ROLE_USER GROUP_READ" | ❌ Not included |
| `userId` | User UUID (for Socket.IO) | ❌ Not included |
| `type` | `"access"` | `"refresh"` ✅ |

---

## 6. Authentication Flows

### 6.1. Login Flow

**Step 1:** Client sends credentials

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "Password123!"
}
```

**Step 2:** AuthenticationService validates

```java
public AuthenticationResponse authenticate(AuthenticationRequest request) {
    // 1. Find user
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    // 2. Verify password
    boolean valid = passwordEncoder.matches(request.getPassword(), user.getPassword());
    if (!valid) {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    // 3. Generate tokens
    String accessToken = jwtUtils.generateToken(user);
    String refreshToken = jwtUtils.generateRefreshToken(user);

    // 4. Return response
    return AuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(3600L)
            .isAuthenticated(true)
            .build();
}
```

**Step 3:** Response

```json
{
  "code": 1000,
  "data": {
    "isAuthenticated": true,
    "access_token": "eyJhbGci...",
    "refresh_token": "eyJhbGci...",
    "expires_in": 3600,
    "token_type": "Bearer"
  }
}
```

### 6.2. API Request Flow

**Step 1:** Client sends request with access token

```http
GET /api/v1/groups
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Step 2:** Spring Security Filter Chain processes

```
1. Extract "Bearer eyJh..." from Authorization header
2. Call CustomJwtDecoder.decode(token)
3. JwtUtils.verifyToken(token, false)
   ✓ Signature valid
   ✓ Not expired
   ✓ Not invalidated
4. Convert JWT to Authentication object
5. Store in SecurityContext
6. Check @PreAuthorize if present
7. Execute controller method
```

**Step 3:** Response

```json
{
  "code": 1000,
  "data": [
    { "id": "...", "name": "Group 1" },
    { "id": "...", "name": "Group 2" }
  ]
}
```

### 6.3. Refresh Token Flow

**Step 1:** Client sends refresh token

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "token": "eyJhbGci... (refresh token)"
}
```

**Step 2:** AuthenticationService processes

```java
public AuthenticationResponse refreshToken(RefreshRequest request) {
    // 1. Verify refresh token
    SignedJWT jwt = jwtUtils.verifyToken(request.getToken(), true);
    
    // 2. Check token type
    String type = jwt.getJWTClaimsSet().getStringClaim("type");
    if (!"refresh".equals(type)) {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    
    // 3. Invalidate old refresh token (ROTATION)
    UUID jti = UUID.fromString(jwt.getJWTClaimsSet().getJWTID());
    invalidatedTokenRepo.save(InvalidatedToken.builder()
            .id(jti)
            .expiresAt(jwt.getJWTClaimsSet().getExpirationTime())
            .build());
    
    // 4. Find user
    UUID userId = UUID.fromString(jwt.getJWTClaimsSet().getSubject());
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    
    // 5. Generate NEW tokens
    String newAccessToken = jwtUtils.generateToken(user);
    String newRefreshToken = jwtUtils.generateRefreshToken(user);
    
    // 6. Return new tokens
    return AuthenticationResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .expiresIn(3600L)
            .isAuthenticated(true)
            .build();
}
```

**Important:** Old refresh token is now INVALID and cannot be reused!

### 6.4. Logout Flow

```java
public void logout(LogoutRequest request) {
    try {
        // Verify and invalidate refresh token
        SignedJWT jwt = jwtUtils.verifyToken(request.getToken(), true);
        
        UUID jti = UUID.fromString(jwt.getJWTClaimsSet().getJWTID());
        invalidatedTokenRepo.save(InvalidatedToken.builder()
                .id(jti)
                .expiresAt(jwt.getJWTClaimsSet().getExpirationTime())
                .build());
                
    } catch (AppException e) {
        log.info("Token already invalidated");
    }
}
```

---

## 7. API Examples

### 7.1. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "Password123!"
  }'
```

**Response:**

```json
{
  "code": 1000,
  "data": {
    "access_token": "eyJhbGciOiJIUzUxMiJ9...",
    "refresh_token": "eyJhbGciOiJIUzUxMiJ9...",
    "expires_in": 3600,
    "token_type": "Bearer"
  }
}
```

### 7.2. API Call with Access Token

```bash
ACCESS_TOKEN="your_access_token_here"

curl -X GET http://localhost:8080/api/v1/groups \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 7.3. Refresh Token

```bash
REFRESH_TOKEN="your_refresh_token_here"

curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"token\": \"$REFRESH_TOKEN\"}"
```

### 7.4. Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"token\": \"$REFRESH_TOKEN\"}"
```

---

## 8. Testing

### 8.1. Test Scenarios

**✅ Valid Token:**

```bash
# Should return 200 OK
curl -X GET http://localhost:8080/api/v1/groups \
  -H "Authorization: Bearer $VALID_ACCESS_TOKEN"
```

**❌ Expired Token:**

```bash
# Should return 401 Unauthorized
curl -X GET http://localhost:8080/api/v1/groups \
  -H "Authorization: Bearer $EXPIRED_TOKEN"
```

**❌ Invalid Signature:**

```bash
# Should return 401 Unauthorized
curl -X GET http://localhost:8080/api/v1/groups \
  -H "Authorization: Bearer eyJhbGci...modified_signature"
```

**❌ Using Access Token to Refresh:**

```bash
# Should return 401 Unauthorized
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -d "{\"token\": \"$ACCESS_TOKEN\"}"
```

**❌ Reusing Refresh Token:**

```bash
# Use refresh token once
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -d "{\"token\": \"$REFRESH_TOKEN\"}"

# Try to use same token again - Should fail!
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -d "{\"token\": \"$REFRESH_TOKEN\"}"
```

### 8.2. Decode JWT (for debugging)

Visit [https://jwt.io](https://jwt.io) and paste your token to inspect claims.

---

## 9. Troubleshooting

### 9.1. Common Issues

**Issue:** All requests return 401

**Solution:**
- Check if `Authorization` header is present
- Verify token hasn't expired
- Check `SIGNER_KEY` matches between token generation and validation

**Issue:** Token always invalid

**Solution:**
```java
// Add debug logging
@Override
public SignedJWT verifyToken(String token, boolean isRefresh) {
    log.info("Verifying token: {}", token.substring(0, 20) + "...");
    log.info("Signer key: {}", SIGNER_KEY.substring(0, 10) + "...");
    // ... rest of validation
}
```

**Issue:** CORS errors

**Solution:**
```java
@Bean
public CorsConfigurationSource corsSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("http://localhost:3000");
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.setAllowCredentials(true);  // Important!
    // ...
}
```

### 9.2. Debug SQL

Check invalidated tokens:

```sql
-- View all invalidated tokens
SELECT * FROM invalidated_token;

-- Check specific token
SELECT * FROM invalidated_token WHERE id = 'your-jti-uuid';

-- Cleanup expired manually
DELETE FROM invalidated_token WHERE expires_at < NOW();
```

---

## 📝 Summary

✅ **Implemented:**
- Dual-token system (Access + Refresh)
- Spring Security with OAuth2 Resource Server
- Token rotation on refresh
- Token invalidation on logout
- Type-based token verification
- Role & permission-based authorization

🔒 **Security Features:**
- HMAC-SHA512 signing
- Short-lived access tokens (1h)
- One-time use refresh tokens
- Token type verification
- Stateless authentication
- CORS protection

---

**Author:** TripJoy Backend Team  
**Last Updated:** January 23, 2026  
**Version:** 1.0.0
