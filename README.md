# TripJoy API

A RESTful API backend for the TripJoy travel planning application, built with Spring Boot 3 and Java 21. This API provides comprehensive trip management features including user authentication, group collaboration, itinerary planning, location management, real-time chat, and social networking capabilities for travelers.

## 🎯 Features

### Core Modules
- **Authentication & Authorization**: Secure JWT-based authentication with role-based access control (RBAC)
- **User Management**: Complete user profile management with avatar support via Cloudinary
- **Group Collaboration**: Create and manage travel groups with role-based permissions
- **Trip Planning**: Comprehensive itinerary management with timeline-based planning
- **Location Services**: PostGIS-powered location search and geospatial queries
- **Real-time Chat**: Socket.IO-based messaging with conversation management and pinned messages
- **Social Features**: Posts, comments, feedback, and travel notebooks
- **Admin Dashboard**: Comprehensive admin system with report management, moderation actions, and analytics
- **Notification System**: Real-time notifications with activity logging

### Technical Highlights
- **Spatial Data**: PostGIS integration for advanced location-based features
- **Real-time Communication**: Socket.IO with Redis Pub/Sub for scalable WebSocket connections
- **Distributed Caching**: Redis-based caching with Redisson for improved performance
- **Rate Limiting**: Bucket4j for API rate limiting and abuse prevention
- **API Documentation**: OpenAPI 3.0 (Swagger) with comprehensive endpoint documentation
- **Code Quality**: Spotless Maven plugin for consistent code formatting

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot 3.3.9** with Java 21
- **Spring Data JPA** with Hibernate 6
- **Spring Security** with OAuth2 Resource Server
- **Spring Validation** for request validation

### Database & Spatial
- **PostgreSQL 17** with **PostGIS 3.5** for spatial data
- **Hibernate Spatial** for geospatial entity mapping
- **JTS (Java Topology Suite)** for geometric operations

### Real-time & Caching
- **Socket.IO** (Netty SocketIO 2.0.6) for real-time bidirectional communication
- **Redis 7.2** for caching and session management
- **Redisson 3.24.3** for distributed features and Socket.IO store

### Utilities & Libraries
- **Lombok** for reducing boilerplate code
- **MapStruct 1.5.5** for DTO mapping
- **Passay 1.6.2** for password validation
- **Bucket4j** for rate limiting
- **dotenv-java** for environment configuration
- **SpringDoc OpenAPI** for API documentation

### Development Tools
- **Maven** for dependency management
- **Spotless** for code formatting
- **Docker Compose** for containerized development environment

## 🏗️ System Architecture

TripJoy follows a **Layered Architecture** pattern combined with **Event-Driven Architecture** for scalable and maintainable design. The system consists of multiple layers, each with specific responsibilities.

![TripJoy System Architecture](docs/images/tripjoy-Architectural_Design.drawio.png)

The architecture consists of four main layers:

1. **Client Layer**: Admin web application (Next.js) and end-user mobile application (React Native/Ionic) communicating via HTTP REST API and Socket.IO WebSocket connections.

2. **Backend Server**: 
   - **Presentation Layer**: REST controllers with security filter chain (JWT authentication, RBAC authorization, rate limiting)
   - **Business Logic Layer**: Services with transactional support, business audit, and application event publisher
   - **Event-Driven Layer**: Transactional event listeners with message broker for asynchronous processing
   - **Data Access Layer**: JPA repositories with Hibernate ORM for database operations

3. **Infrastructure Layer**: 
   - PostgreSQL 17 + PostGIS 3.5 for spatial database operations
   - Redis 7.2 for distributed caching and pub/sub messaging
   - Socket.IO server for real-time WebSocket communication
   - Cloudinary CDN for media storage and delivery

4. **External Services**: Mapbox API for maps and geolocation, Email service for notifications, and AI Service (FastAPI) with ChatGPT integration for intelligent trip planning.

### Key Components

- **REST API**: Stateless HTTP endpoints for all CRUD operations
- **Socket.IO Server**: WebSocket connections for real-time chat and notifications
- **Redis Cache**: Distributed caching layer with event-driven invalidation
- **PostGIS**: Spatial database for location-based queries and geofencing
- **Event Bus**: Asynchronous event processing for notifications and activity logs
- **Security Layer**: JWT authentication with RBAC authorization

### External Integrations

- **Mapbox API**: Geocoding, routing, and map visualization
- **Cloudinary CDN**: Media upload, optimization, and delivery
- **AI Service (FastAPI)**: ChatGPT integration for trip planning assistance
- **Email Service**: Transactional and notification emails

### Design Patterns

- **Layered Architecture**: Separation of concerns by layers
- **Repository Pattern**: Data access abstraction
- **Service Pattern**: Business logic encapsulation
- **DTO Pattern**: API request/response transformation
- **Event-Driven**: Loose coupling via application events
- **Filter Chain**: Security and logging filters


## 📋 Prerequisites

Before running this project, ensure you have:

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker & Docker Compose** ([Download](https://www.docker.com/products/docker-desktop))
- **Git** for version control

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd tripjoy-api
```

### 2. Environment Configuration

Copy the example environment file and configure your settings:

```bash
cp .env.example .env
```

Edit `.env` and update the following critical values:

```bash
# Security - IMPORTANT: Generate your own keys!
JWT_SIGNER_KEY=generate_your_own_jwt_signer_key_here  # Run: openssl rand -hex 32

# Database
DB_PASSWORD=your_secure_password_here

# Redis
REDIS_PASSWORD=your_redis_password_here
```

### 3. Start Infrastructure Services

Start PostgreSQL (with PostGIS) and Redis using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL 17 + PostGIS 3.5** on port `5432`
- **Redis 7.2** on port `6379`

Verify services are running:

```bash
docker-compose ps
```

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

**Development mode:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Or using Maven profiles:**

```bash
# Development
mvn spring-boot:run -Pdev

# Test
mvn spring-boot:run -Ptest

# Production
mvn spring-boot:run -Pprod
```

The API will start on `http://localhost:8080` by default.

### 6. Verify Installation

Check the health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

## 📚 API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

The raw OpenAPI specification is available at:

```
http://localhost:8080/v3/api-docs
```

Or view the pre-generated specification: `api-doc.json`

## 🏗️ Project Structure

```
tripjoy-api/
├── src/main/java/com/tripjoy/api/
│   ├── TripjoyApiApplication.java    # Main application entry point
│   ├── configuration/                # Spring configurations (Security, Redis, Socket.IO, etc.)
│   ├── constant/                     # Application constants and enums
│   ├── controller/                   # REST API controllers (16 modules)
│   │   ├── AuthenticationController.java
│   │   ├── UserController.java
│   │   ├── GroupController.java
│   │   ├── ItineraryController.java
│   │   ├── LocationController.java
│   │   ├── ChatMessageController.java
│   │   ├── PostController.java
│   │   ├── CommentController.java
│   │   ├── FeedbackController.java
│   │   ├── TravelNotebookController.java
│   │   ├── ReportController.java
│   │   ├── RoleController.java
│   │   ├── PermissionController.java
│   │   ├── AdminController.java
│   │   ├── ConversationController.java
│   │   └── SuggestLocationController.java
│   ├── dto/                          # Data Transfer Objects (requests/responses)
│   ├── entity/                       # JPA entities (28 domain models)
│   ├── enums/                        # Enumerations
│   ├── exception/                    # Custom exceptions and global error handling
│   ├── listener/                     # Event listeners
│   ├── mapper/                       # MapStruct mappers for DTO conversions
│   ├── repository/                   # Spring Data JPA repositories
│   ├── service/                      # Business logic services (34 services)
│   ├── utils/                        # Utility classes
│   └── validator/                    # Custom validators
├── src/main/resources/
│   └── application.yml               # Application configuration
├── docker/                           # Docker configuration files
│   ├── postgis/                      # PostgreSQL + PostGIS initialization
│   └── redis/                        # Redis configuration
├── docs/                             # Documentation
├── scripts/                          # Utility scripts
├── .env.example                      # Example environment configuration
├── docker-compose.yml                # Docker Compose configuration
├── pom.xml                           # Maven dependencies
└── README.md                         # This file
```

## 🔒 Security

### Authentication Flow

1. **Login/Register**: Client sends credentials to `/api/auth/login` or `/api/auth/register`
2. **Token Generation**: Server validates credentials and returns JWT access token + refresh token
3. **API Access**: Client includes access token in `Authorization: Bearer <token>` header
4. **Token Refresh**: Use refresh token to obtain new access token when expired

### Authorization

The API implements role-based access control (RBAC) with the following roles:

- **ADMIN**: Full system access
- **USER**: Standard user permissions
- **MODERATOR**: Content moderation capabilities

Permissions are enforced at the controller level using `@PreAuthorize` annotations.

### Password Security

- Passwords are hashed using BCrypt
- Password validation enforced by Passay library
- Requirements: minimum length, uppercase, lowercase, digits, special characters

## 🌐 Socket.IO Real-time Features

The application includes a Socket.IO server for real-time features:

- **Real-time Chat**: Send and receive messages instantly
- **Typing Indicators**: Show when users are typing
- **Online Status**: Track user presence
- **Message Reactions**: Real-time reaction updates
- **Notifications**: Push notifications to connected clients

**Socket.IO Server**: Runs on port `8085` by default

**Documentation**: See `docs/SOCKET_IO_README_VI.md` for detailed implementation guide

## 🗺️ Location & Spatial Features

Leveraging PostGIS for advanced geospatial capabilities:

- **Location Search**: Full-text search with geographic data
- **Proximity Queries**: Find nearby locations
- **Route Planning**: Distance calculations and route optimization
- **Geofencing**: Define and query geographic boundaries

**Documentation**: See `docs/LOCATION_MAP_API_GUIDE.md` for API usage

## 🧪 Testing

Run unit and integration tests:

```bash
mvn test
```

Run with specific profile:

```bash
mvn test -Ptest
```

## 🔧 Code Formatting

This project uses Spotless for consistent code formatting.

**Check formatting:**

```bash
mvn spotless:check
```

**Apply formatting:**

```bash
mvn spotless:apply
```

## 📦 Building for Production

Create a production-ready JAR:

```bash
mvn clean package -Pprod -DskipTests
```

The JAR file will be created in `target/tripjoy-api-0.0.1-SNAPSHOT.jar`

Run the production build:

```bash
java -jar target/tripjoy-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 🐳 Docker Deployment

Build Docker image:

```bash
docker build -t tripjoy-api:latest .
```

Run with Docker:

```bash
docker run -p 8080:8080 --env-file .env tripjoy-api:latest
```

## 📊 Monitoring & Health Checks

Spring Boot Actuator endpoints (if enabled):

- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

## 🔍 Common Issues & Troubleshooting

### Database Connection Refused

**Problem**: `Connection to localhost:5432 refused`

**Solution**: Ensure PostgreSQL container is running:
```bash
docker-compose up -d postgres
docker-compose ps
```

### Redis Connection Error

**Problem**: `Unable to connect to Redis`

**Solution**: Check Redis container and password:
```bash
docker-compose up -d redis
docker-compose logs redis
```

### PostGIS Extension Not Found

**Problem**: `ERROR: type "geometry" does not exist`

**Solution**: Ensure PostGIS is initialized:
```bash
docker-compose exec postgres psql -U postgres -d tripjoy -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

### Socket.IO Connection Issues

**Problem**: WebSocket connection fails

**Solution**: 
1. Verify Socket.IO port (default 8085) is not blocked
2. Check `SOCKET_HOST` and `SOCKET_PORT` in `.env`
3. Review logs for Socket.IO startup messages

### JWT Token Errors

**Problem**: `Invalid signature` or token validation errors

**Solution**: Ensure `JWT_SIGNER_KEY` is properly set and consistent across restarts

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Run code formatting (`mvn spotless:apply`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## 📄 License

This project is part of a graduation thesis (DATN - Đồ Án Tốt Nghiệp) at HCMUT-K22.

## 📞 Support

For detailed documentation on specific features:

- **System Architecture** (Vietnamese): See `docs/ARCHITECTURE_VI.md`
- **Socket.IO Implementation**: See `docs/SOCKET_IO_README_VI.md`
- **Location API Guide**: See `docs/LOCATION_MAP_API_GUIDE.md`
- **Admin & Moderation API**: See `docs/report_api_integration_guide.md` and `docs/modules/ADMIN_BUSINESS_DASHBOARD_API.md`
- **Technical Report**: See `docs/TECHNICAL_REPORT_SOCKET_IO.md`

## 🏆 Acknowledgments

- Spring Boot team for the excellent framework
- PostGIS for powerful spatial capabilities
- Socket.IO for real-time communication
- Redis for caching and pub/sub features
- All open-source contributors whose libraries made this project possible

---

**Built with ❤️ for travelers by travelers**
