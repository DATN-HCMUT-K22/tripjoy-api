# Kiến Trúc Hệ Thống TripJoy

## 📋 Tổng Quan

TripJoy là một hệ thống quản lý và lập kế hoạch du lịch với kiến trúc phân tầng (Layered Architecture), kết hợp mô hình Client-Server và Event-Driven Architecture. Hệ thống được thiết kế để hỗ trợ cả ứng dụng web quản trị và ứng dụng di động cho người dùng cuối, với khả năng giao tiếp real-time và tích hợp AI.

![Kiến Trúc Hệ Thống TripJoy](./images/tripjoy-Architectural_Design.drawio.png)

## 🖥️ Kiến Trúc Tổng Thể

Hệ thống TripJoy bao gồm 3 thành phần chính:

### 1. **Client Layer** (Tầng Ứng Dụng Khách)

#### Admin Web Client
- **Framework**: Next.js (React Framework)
- **Công nghệ**: 
  - State Management: Redux Toolkit hoặc Context API
  - UI Libraries: Material-UI, Tailwind CSS
- **Chức năng**: 
  - Quản lý người dùng và nội dung
  - Dashboard thống kê
  - Quản lý quyền và vai trò
  - Xử lý báo cáo và feedback

#### End-User Mobile Client
- **Framework**: React Native với Ionic
- **Công nghệ**:
  - Socket.IO Client: Giao tiếp real-time
  - State Management: Redux/MobX
  - UI Libraries: React Native Paper, NativeBase
- **Chức năng**:
  - Lập kế hoạch chuyến đi
  - Chat và nhóm du lịch
  - Đăng bài và mạng xã hội du lịch
  - Tìm kiếm địa điểm và lộ trình

### 2. **Backend Server** (Máy Chủ Backend)

Backend được thiết kế theo kiến trúc phân tầng (Layered Architecture) với 4 tầng chính:

#### 🎯 Presentation Layer (Tầng Trình Diễn)

**REST Controllers** - Xử lý các HTTP Request/Response:

- **AuthController**: Xác thực và đăng ký
- **UserController**: Quản lý thông tin người dùng
- **GroupController**: Quản lý nhóm du lịch
- **ItineraryController**: Quản lý lịch trình
- **LocationController**: Quản lý địa điểm
- **ConversationController**: Quản lý hội thoại
- **MessageController**: Quản lý tin nhắn
- **PostController**: Quản lý bài đăng
- **FeedbackController**: Quản lý phản hồi
- **AdminController**: Các chức năng quản trị

**Security Filter Chain**:
- **JWT Authentication**: Xác thực bằng JSON Web Token
- **RBAC Authorization**: Phân quyền dựa trên vai trò người dùng
- **Rate Limiting**: Giới hạn số lượng request (Bucket4j)

#### 💼 Business Logic Layer (Tầng Nghiệp Vụ)

**Services with @Transactional** - Xử lý logic nghiệp vụ:

- **Business Audit**: Ghi nhận hoạt động của người dùng
- **Application Event Publisher**: Phát sự kiện trong hệ thống
  - Notification events
  - Activity log events
  - Cache invalidation events

**Các Service chính**:
- `AuthenticationService`: Logic xác thực
- `UserService`: Logic quản lý người dùng
- `GroupService`: Logic nhóm du lịch
- `ItineraryService`: Logic lịch trình
- `LocationService`: Logic địa điểm (với PostGIS)
- `ChatService`: Logic chat real-time
- `PostService`: Logic mạng xã hội
- `NotificationService`: Logic thông báo
- `ReportService`: Logic báo cáo

#### ⚡ Event-Driven Layer (Tầng Xử Lý Sự Kiện)

**@TransactionalEventListener** - Xử lý sự kiện bất đồng bộ:

- **Message Broker**: Phân phối sự kiện giữa các service
- **Event Listeners**: Lắng nghe và xử lý sự kiện
  - User registration events
  - Chat message events
  - Notification events
  - Activity logging events

**Lợi ích**:
- Giảm coupling giữa các service
- Xử lý bất đồng bộ cho các tác vụ không cần đồng bộ
- Dễ dàng mở rộng chức năng mới

#### 💾 Data Access Layer (Tầng Truy Cập Dữ Liệu)

**JPA Repositories**:
- Spring Data JPA với các repository cho từng entity
- Custom queries với JPQL và Native SQL
- Pagination và Sorting support

**Hibernate ORM**:
- Object-Relational Mapping
- Lazy Loading và Eager Loading
- Entity relationships (OneToMany, ManyToOne, ManyToMany)
- Hibernate Spatial cho dữ liệu địa lý

### 3. **Infrastructure Layer** (Tầng Cơ Sở Hạ Tầng)

#### 🗄️ Database & Caching

**PostgreSQL 17 + PostGIS 3.5**:
- Database chính lưu trữ dữ liệu ứng dụng
- PostGIS extension cho spatial data (tọa độ, geometry)
- JSONB support cho dữ liệu linh hoạt
- Full-text search capabilities

**Redis 7.2 (Pub/Sub & Caching)**:
- **Caching**: Cache dữ liệu thường xuyên truy cập
  - User sessions
  - Location data
  - Popular posts
- **Pub/Sub**: Message broker cho Socket.IO distributed
- **Rate Limiting**: Lưu trữ bucket counters

#### 🔌 Real-time Communication

**Socket.IO Server**:
- WebSocket connections cho real-time chat
- Room-based communication (theo nhóm, cuộc hội thoại)
- Event emitters và listeners
- Redis adapter cho horizontal scaling

#### 📦 External Services

**Cloudinary CDN**:
- Media storage (ảnh, video)
- Image optimization và transformation
- CDN delivery cho tốc độ cao

**PyVector** (Nếu có):
- Vector database cho AI features
- Similarity search
- Recommendation system

### 4. **External Services & Integrations**

#### 🗺️ Mapbox API
- Geocoding: Chuyển đổi địa chỉ thành tọa độ
- Reverse Geocoding: Chuyển tọa độ thành địa chỉ
- Directions API: Tính toán lộ trình
- Map visualization

#### 📧 Email Service
- Transactional emails (đăng ký, xác nhận)
- Notification emails
- Newsletter (nếu có)

#### 🤖 AI Service (FastAPI)

**API Layer**:
- **ChatGPT API**: Tích hợp GPT cho chatbot tư vấn du lịch
- **Agent API**: AI agents cho các tác vụ tự động

**Business Logic Layer**:
- Trip recommendation
- Smart itinerary planning
- Natural language processing

**Infrastructure**:
- Vector databases (ChromaDB, FAISS)
- LLM integration (OpenAI, LocalAI)
- Caching layer

## 🔄 Luồng Dữ Liệu (Data Flow)

### 1. User Authentication Flow

```
Mobile Client 
  → HTTP REST API 
  → AuthController 
  → AuthenticationService 
  → UserRepository 
  → PostgreSQL
  ← JWT Token
```

### 2. Real-time Chat Flow

```
Mobile Client (Socket.IO)
  → Socket.IO Server
  → Message Broker (Redis Pub/Sub)
  → ChatMessageController
  → ChatService
  → MessageRepository
  → PostgreSQL
  
Event Publisher 
  → NotificationListener
  → Socket.IO emit to room
  → All connected clients in conversation
```

### 3. Location Search Flow

```
Mobile Client
  → LocationController (HTTP GET /api/locations/search)
  → LocationService
  → LocationRepository (PostGIS spatial query)
  → PostgreSQL + PostGIS
  → Redis Cache (store result)
  ← Location list with distance
```

### 4. AI-Powered Trip Planning Flow

```
Mobile Client
  → ItineraryController
  → ItineraryService
  → AI Service (HTTP call to FastAPI)
  → ChatGPT API
  ← AI-generated itinerary suggestions
  → Save to PostgreSQL
```

## 🎨 Design Patterns Sử Dụng

### 1. **Layered Architecture**
- Tách biệt concerns theo từng tầng
- Dễ bảo trì và mở rộng
- Tầng trên phụ thuộc vào tầng dưới

### 2. **Repository Pattern**
- Trừu tượng hóa data access
- Dễ dàng test và mock
- Tách biệt business logic khỏi data access

### 3. **Service Pattern**
- Encapsulate business logic
- Reusable và testable
- Transaction management

### 4. **Event-Driven Architecture**
- Loose coupling giữa components
- Asynchronous processing
- Scalable và resilient

### 5. **DTO Pattern**
- Tách biệt entity và API response
- Validation và transformation
- Security (không expose entity trực tiếp)

### 6. **Filter Chain Pattern**
- Security filters (JWT, CORS, Rate Limiting)
- Request/Response logging
- Error handling

## 🔐 Security Architecture

### Authentication
- **JWT (JSON Web Token)**: Stateless authentication
- **Refresh Token**: Renew access token
- **Password Hashing**: BCrypt algorithm

### Authorization
- **RBAC (Role-Based Access Control)**: Phân quyền theo vai trò
- **Method Security**: `@PreAuthorize` annotations
- **Resource-level permissions**: Kiểm tra ownership

### API Security
- **Rate Limiting**: Chống DDoS và abuse
- **CORS**: Cross-Origin Resource Sharing
- **Input Validation**: Bean Validation (JSR-380)
- **SQL Injection Prevention**: Parameterized queries
- **XSS Prevention**: Output encoding

## 📊 Caching Strategy

### 1. **Application-level Caching**
- `@Cacheable`: Cache method results
- `@CacheEvict`: Xóa cache khi update
- `@CachePut`: Update cache

### 2. **Cache Levels**
- **L1 - Method Cache**: Short-lived, frequent access
- **L2 - Entity Cache**: Hibernate second-level cache
- **L3 - Distributed Cache**: Redis cho multiple instances

### 3. **Cache Invalidation**
- **Time-based**: TTL (Time To Live)
- **Event-based**: Xóa khi có update
- **Manual**: Admin tools để clear cache

## 📈 Scalability & Performance

### Horizontal Scaling
- **Stateless Backend**: Có thể scale multiple instances
- **Redis Pub/Sub**: Socket.IO distributed với Redis adapter
- **Load Balancer**: Nginx hoặc AWS ALB

### Database Optimization
- **Indexing**: B-tree, GiST indexes cho PostGIS
- **Connection Pooling**: HikariCP
- **Query Optimization**: EXPLAIN ANALYZE
- **Read Replicas**: Cho read-heavy workloads

### Caching & CDN
- **Redis Cache**: Giảm database load
- **Cloudinary CDN**: Fast media delivery
- **HTTP Caching**: ETag, Cache-Control headers

## 🛡️ Fault Tolerance & Resilience

### Error Handling
- **Global Exception Handler**: `@ControllerAdvice`
- **Custom Exceptions**: Business-specific errors
- **Graceful Degradation**: Fallback mechanisms

### Monitoring & Logging
- **Application Logs**: SLF4J + Logback
- **Access Logs**: HTTP request/response logging
- **Performance Metrics**: Spring Actuator
- **Error Tracking**: Sentry hoặc similar tools

### Health Checks
- **Database Health**: PostgreSQL connection check
- **Redis Health**: Redis ping
- **Socket.IO Health**: Connection status
- **External API Health**: Dependency checks

## 🚀 Deployment Architecture

### Development
```
Docker Compose:
  - PostgreSQL + PostGIS
  - Redis
  - Application (Spring Boot)
  - Socket.IO Server
```

### Production
```
Cloud Infrastructure:
  - Application Servers (Auto-scaling group)
  - Load Balancer (Nginx/AWS ALB)
  - Database (RDS PostgreSQL with PostGIS)
  - Cache (ElastiCache Redis)
  - Storage (S3 hoặc Cloudinary)
  - AI Service (Separate container/service)
```

## 📚 Technology Stack Summary

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Client** | React Native, Next.js | Web & Mobile apps |
| **API Gateway** | Spring MVC, REST | HTTP API endpoints |
| **Business Logic** | Spring Boot, Java 21 | Core application logic |
| **Real-time** | Socket.IO, Netty | WebSocket communication |
| **Data Access** | Spring Data JPA, Hibernate | ORM & database operations |
| **Database** | PostgreSQL 17 + PostGIS | Relational & spatial data |
| **Caching** | Redis 7.2, Redisson | Distributed cache & pub/sub |
| **Security** | Spring Security, JWT | Authentication & authorization |
| **External APIs** | Mapbox, Cloudinary, ChatGPT | Maps, Media, AI |
| **AI/ML** | FastAPI, Python | AI-powered features |
| **DevOps** | Docker, Docker Compose | Containerization |

## 🔮 Future Enhancements

### Microservices Architecture
- Tách thành các service nhỏ (User Service, Chat Service, Location Service)
- API Gateway (Spring Cloud Gateway)
- Service Discovery (Eureka, Consul)
- Circuit Breaker (Resilience4j)

### Advanced Features
- **GraphQL API**: Thay thế hoặc bổ sung cho REST
- **Message Queue**: RabbitMQ/Kafka cho event streaming
- **Elasticsearch**: Full-text search nâng cao
- **Kubernetes**: Container orchestration
- **CI/CD Pipeline**: Automated testing và deployment

### AI/ML Integration
- Personalized recommendations
- Smart itinerary optimization
- Image recognition cho địa điểm
- Sentiment analysis cho reviews

---

**Tài liệu này mô tả kiến trúc hệ thống TripJoy tại thời điểm hiện tại. Kiến trúc có thể được cập nhật và mở rộng theo nhu cầu phát triển của dự án.**
