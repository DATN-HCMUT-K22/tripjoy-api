# Socket.IO & Event-Driven Architecture - Hướng Dẫn Chi Tiết

> **Triết lý học tập:** "Fake it till you make it" - Code trước, hiểu sau!  
> Tài liệu này giải thích CỰC KỲ CHI TIẾT mọi khái niệm để bạn tự tin trình bày với senior.

---

## 📚 Mục Lục

1. [Tổng Quan Kiến Trúc](#1-tổng-quan-kiến-trúc)
2. [Các Thành Phần Core](#2-các-thành-phần-core)
3. [Event-Driven Pattern Giải Thích](#3-event-driven-pattern-giải-thích)
4. [WebSocket vs HTTP](#4-websocket-vs-http)
5. [Luồng Hoạt Động Chi Tiết](#5-luồng-hoạt-động-chi-tiết)
6. [Configuration Chi Tiết](#6-configuration-chi-tiết)
7. [Best Practices](#7-best-practices)
8. [Testing & Debugging](#8-testing--debugging)

---

## 1. Tổng Quan Kiến Trúc

### 1.1 Vấn Đề Cần Giải Quyết

**Tình huống:** Ứng dụng chat cần gửi tin nhắn real-time.

**Thách thức:**
- HTTP thông thường không thể "đẩy" data từ server xuống client
- Client phải liên tục polling (hỏi server mỗi giây) → lãng phí bandwidth
- Cần đảm bảo tin nhắn được lưu DB trước khi broadcast
- Cần tránh spam và DoS attacks

**Giải pháp:** Socket.IO + Event-Driven Architecture

### 1.2 Kiến Trúc Tổng Thể

```
┌─────────────┐                    ┌──────────────────┐
│ Client A    │◄──WebSocket────────│  SocketIOServer  │
│ (Browser)   │                    │   (Port 8085)    │
└─────────────┘                    └────────┬─────────┘
                                            │
┌─────────────┐                    ┌────────▼─────────┐
│ Client B    │◄──WebSocket────────│  SocketService   │
│ (Mobile)    │                    │  (Event Handler) │
└─────────────┘                    └────────┬─────────┘
                                            │
┌─────────────┐      HTTP POST     ┌────────▼─────────┐
│ Client A    │───────────────────►│ ChatMessageService│
│ Send Msg    │                    │ (Business Logic)  │
└─────────────┘                    └────────┬─────────┘
                                            │
                                    ┌───────▼──────────┐
                                    │ Save to Database │
                                    │   (PostgreSQL)   │
                                    └───────┬──────────┘
                                            │
                                    ┌───────▼──────────┐
                                    │ Publish Event    │
                                    │ MessageSentEvent │
                                    └───────┬──────────┘
                                            │
                                    ┌───────▼──────────┐
                                    │Transaction COMMIT│
                                    └───────┬──────────┘
                                            │
                                    ┌───────▼────────────┐
                                    │MessageEventListener│
                                    │(AFTER_COMMIT)      │
                                    └───────┬────────────┘
                                            │
                                    ┌───────▼────────────┐
                                    │SocketService       │
                                    │sendNewMessage()    │
                                    └───────┬────────────┘
                                            │
                    ┌───────────────────────┴──────────────────────┐
                    ▼                                              ▼
            ┌───────────────┐                            ┌───────────────┐
            │  Client A     │                            │  Client B     │
            │  Nhận message │                            │  Nhận message │
            └───────────────┘                            └───────────────┘
```

### 1.3 Các Lớp Trong Kiến Trúc

| Lớp | Vai Trò | Ví Dụ |
|-----|---------|-------|
| **Presentation** | Giao diện người dùng | WebSocket Client, HTTP Client |
| **API Gateway** | Tiếp nhận request | `SocketIOServer`, REST Controllers |
| **Service** | Business logic | `ChatMessageService`, `SocketService` |
| **Event** | Xử lý bất đồng bộ | `MessageEventListener` |
| **Repository** | Truy xuất database | `ChatMessageRepository` |
| **Infrastructure** | Hạ tầng | Redis, PostgreSQL |

---

## 2. Các Thành Phần Core

### 2.1 SocketIOConfig

**Mục đích:** Cấu hình server Socket.IO

**Vị trí:** `src/main/java/com/tripjoy/api/configuration/socketio/SocketIOConfig.java`

#### Giải Thích Từng Dòng Code

```java
@Configuration  // ← Spring sẽ load class này khi khởi động
@RequiredArgsConstructor  // ← Lombok tự tạo constructor với các field final
@Slf4j  // ← Lombok tự tạo logger để ghi log
public class SocketIOConfig {

    // Inject dependencies qua constructor
    private final RedissonClient redissonClient;  // ← Kết nối Redis (để scale)
    private final JwtUtils jwtUtils;  // ← Để verify JWT token

    @Value("${socket-server.host}")  // ← Đọc từ application.yaml
    private String host;  // ← Ví dụ: "localhost"

    @Value("${socket-server.port}")  // ← Đọc từ application.yaml
    private Integer port;  // ← Ví dụ: 8085

    @Bean  // ← Tạo bean để Spring quản lý
    public SocketIOServer socketIOServer(ObjectMapper objectMapper) {
        // Tạo configuration object
        com.corundumstudio.socketio.Configuration config = 
            new com.corundumstudio.socketio.Configuration();
        
        // Cấu hình địa chỉ server
        config.setHostname(host);  // localhost
        config.setPort(port);      // 8085
        
        // Cấu hình giới hạn kích thước
        config.setMaxFramePayloadLength(1024 * 1024);  // 1MB = tối đa 1MB mỗi message
        config.setMaxHttpContentLength(1024 * 1024);   // 1MB = tối đa 1MB cho HTTP upgrade
        
        // Cấu hình ping/pong (kiểm tra kết nối còn sống không)
        config.setPingTimeout(60000);   // 60 giây - nếu không nhận pong → ngắt kết nối
        config.setPingInterval(25000);  // 25 giây - gửi ping mỗi 25 giây
        
        // Cấu hình Jackson để serialize LocalDateTime
        com.corundumstudio.socketio.protocol.JacksonJsonSupport jsonSupport = 
            new com.corundumstudio.socketio.protocol.JacksonJsonSupport(
                new com.fasterxml.jackson.module.paramnames.ParameterNamesModule(),
                new JavaTimeModule()  // ← Hỗ trợ LocalDateTime, LocalDate, etc.
            );
        config.setJsonSupport(jsonSupport);
        
        // Cấu hình Redis store (để nhiều server cùng dùng)
        config.setStoreFactory(new RedissonStoreFactory(redissonClient));
        
        // Cấu hình JWT authentication
        config.setAuthorizationListener(this::authorizeConnection);
        
        // Cấu hình error handler
        config.setExceptionListener(new SocketExceptionHandler());
        
        // Tạo server từ config
        SocketIOServer server = new SocketIOServer(config);
        log.info("Socket.IO server configured on {}:{}", host, port);
        return server;
    }

    // Method xác thực JWT token
    private AuthorizationResult authorizeConnection(
            com.corundumstudio.socketio.HandshakeData data) {
        try {
            // Lấy token từ URL parameter
            String token = data.getSingleUrlParam("token");
            
            if (token == null || token.trim().isEmpty()) {
                log.warn("Connection without token from {}", 
                    data.getAddress().getHostString());
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            // Verify token bằng JwtUtils
            jwtUtils.verifyToken(token, false);
            return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;

        } catch (Exception e) {
            log.error("Authorization failed: {}", e.getMessage());
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }
    }
}
```

#### Câu Hỏi Senior Có Thể Hỏi & Trả Lời

**Q1: Tại sao dùng Redis store?**
> **Trả lời:** Redis store cho phép horizontal scaling. Khi có nhiều server (load balancer), tất cả server đều share chung session store trên Redis. Client kết nối vào server A, sau đó reconnect vào server B vẫn giữ được session.

**Q2: PingTimeout và PingInterval khác nhau thế nào?**
> **Trả lời:**
> - `PingInterval (25s)`: Server GỬI ping message mỗi 25 giây để kiểm tra client còn sống không
> - `PingTimeout (60s)`: Nếu sau 60 giây mà không nhận được pong từ client → coi như client đã mất kết nối → ngắt connection
> 
> Ví dụ: Server gửi ping lúc 10:00:00, nếu đến 10:01:00 vẫn chưa nhận pong → disconnect

**Q3: Tại sao cần JavaTimeModule?**
> **Trả lời:** Mặc định Jackson không biết cách serialize `LocalDateTime` (Java 8 time API). `JavaTimeModule` dạy Jackson cách convert LocalDateTime thành JSON string (ISO-8601 format).
>
> Không có module này → Error: `InvalidDefinitionException: Java 8 date/time type not supported`

---

### 2.2 SocketService

**Mục đích:** Xử lý các event từ WebSocket clients

**Các Annotation Quan Trọng:**

```java
@OnConnect    // ← Được gọi khi client kết nối
@OnDisconnect // ← Được gọi khi client ngắt kết nối
@OnEvent("event_name")  // ← Được gọi khi client emit event có tên "event_name"
```

#### Code Chi Tiết

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class SocketService implements ISocketService {

    private final SocketIOServer server;
    private final SocketRateLimiter rateLimiter;  // ← Chống spam

    // ═══════════════════════════════════════════════════════
    // KHI CLIENT KẾT NỐI
    // ═══════════════════════════════════════════════════════
    @OnConnect
    public void onConnect(SocketIOClient client) {
        // Lấy userId từ URL parameter
        // Ví dụ: ws://localhost:8085?token=xxx&userId=123
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        String sessionId = client.getSessionId().toString();

        if (userId != null && !userId.trim().isEmpty()) {
            // Join vào room riêng của user
            // Room name: user_123
            String userRoom = "user_" + userId;
            client.joinRoom(userRoom);
            
            log.info("User connected: userId={}, sessionId={}", userId, sessionId);
        } else {
            log.warn("Client connected without userId");
        }
    }

    // ═══════════════════════════════════════════════════════
    // KHI CLIENT NGẮT KẾT NỐI
    // ═══════════════════════════════════════════════════════
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        
        if (userId != null) {
            // Cleanup rate limiter để giải phóng bộ nhớ
            rateLimiter.cleanup(userId);
        }
        
        log.info("Client disconnected: userId={}", userId);
    }

    // ═══════════════════════════════════════════════════════
    // KHI CLIENT JOIN VÀO CONVERSATION
    // ═══════════════════════════════════════════════════════
    @OnEvent("join_conversation")
    public void onJoinConversation(SocketIOClient client, String conversationId) {
        try {
            // Room name: conversation_abc-def-ghi
            String roomName = "conversation_" + conversationId;
            client.joinRoom(roomName);

            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            log.info("Client joined conversation: userId={}, conversationId={}", 
                userId, conversationId);
                
        } catch (Exception e) {
            log.error("Error joining conversation {}: {}", 
                conversationId, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // KHI CLIENT GỬI TYPING INDICATOR
    // ═══════════════════════════════════════════════════════
    @OnEvent("typing")
    public void onTyping(SocketIOClient client, String conversationId) {
        try {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            
            // RATE LIMITING: Chỉ cho phép 1 typing event mỗi giây
            if (!rateLimiter.allowTyping(userId)) {
                return;  // Bỏ qua nếu spam quá nhanh
            }
            
            String roomName = "conversation_" + conversationId;

            // Broadcast tới TẤT CẢ clients trong room TRỪ người gửi
            server.getRoomOperations(roomName)
                    .getClients()
                    .stream()
                    .filter(c -> !c.getSessionId().equals(client.getSessionId()))
                    .forEach(c -> c.sendEvent("user_typing", userId));

        } catch (Exception e) {
            log.error("Error broadcasting typing: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // BROADCAST MESSAGE TỚI CONVERSATION ROOM
    // ═══════════════════════════════════════════════════════
    public void sendNewMessage(UUID conversationId, ChatMessageResponse messageResponse) {
        try {
            String roomName = "conversation_" + conversationId;
            
            // Gửi event "receive_message" tới TẤT CẢ clients trong room
            server.getRoomOperations(roomName)
                  .sendEvent("receive_message", messageResponse);
            
            log.info("Message broadcasted: messageId={}, conversationId={}", 
                messageResponse.getId(), conversationId);
                
        } catch (Exception e) {
            log.error("Failed to broadcast message: {}", e.getMessage());
        }
    }
}
```

#### Giải Thích Khái Niệm "Room"

**Room là gì?**
- Room giống như "phòng chat" trên Discord/Telegram
- Mỗi client có thể join nhiều rooms
- Khi broadcast tới room, CHỈ clients trong room đó nhận được

**Ví dụ thực tế:**
```
User A (sessionId: aaa) join room: user_123, conversation_abc
User B (sessionId: bbb) join room: user_456, conversation_abc  
User C (sessionId: ccc) join room: user_789

Khi broadcast tới room "conversation_abc":
→ User A nhận được ✅
→ User B nhận được ✅
→ User C KHÔNG nhận được ❌ (không trong room này)
```

---

### 2.3 ChatMessageService

**Mục đích:** Xử lý business logic gửi tin nhắn

#### Code Chi Tiết

```java
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final ApplicationEventPublisher eventPublisher;  // ← Để publish event

    @Transactional  // ← Tất cả DB operations trong 1 transaction
    public ChatMessageResponse sendMessage(
            UUID conversationId, 
            UUID senderId, 
            ChatMessageRequest request) {
        
        // ─────────────────────────────────────────────────────
        // BƯỚC 1: Validate user tồn tại
        // ─────────────────────────────────────────────────────
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // ─────────────────────────────────────────────────────
        // BƯỚC 2: Validate conversation tồn tại
        // ─────────────────────────────────────────────────────
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // ─────────────────────────────────────────────────────
        // BƯỚC 3: Tạo message entity
        // ─────────────────────────────────────────────────────
        ChatMessage message = chatMessageMapper.toEntity(request);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsDeleted(false);

        // ─────────────────────────────────────────────────────
        // BƯỚC 4: Lưu message vào database
        // ─────────────────────────────────────────────────────
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // ─────────────────────────────────────────────────────
        // BƯỚC 5: Update conversation's last message timestamp
        // ─────────────────────────────────────────────────────
        conversation.setLastMessageTimestamp(LocalDateTime.now());
        conversationRepository.save(conversation);

        // ─────────────────────────────────────────────────────
        // BƯỚC 6: Convert entity → DTO response
        // ─────────────────────────────────────────────────────
        ChatMessageResponse response = chatMessageMapper.toResponse(savedMessage);

        // ─────────────────────────────────────────────────────
        // BƯỚC 7: PUBLISH EVENT (QUAN TRỌNG!)
        // ─────────────────────────────────────────────────────
        MessageSentEvent event = MessageSentEvent.builder()
                .conversationId(conversationId)
                .messageResponse(response)
                .build();
        
        // Publish event, nhưng CHƯA broadcast ngay!
        // Broadcast sẽ xảy ra SAU KHI transaction commit
        eventPublisher.publishEvent(event);

        // ─────────────────────────────────────────────────────
        // BƯỚC 8: Return response cho HTTP client
        // ─────────────────────────────────────────────────────
        return response;
    }
    // Khi method kết thúc → Transaction COMMIT
    // → MessageEventListener được trigger
}
```

#### Tại Sao KHÔNG Gọi Trực Tiếp `socketService.sendNewMessage()`?

**CÁCH SAI (Trước đây):**
```java
@Transactional
public ChatMessageResponse sendMessage(...) {
    // Save to DB
    ChatMessage saved = repository.save(message);
    
    // ❌ SAI: Broadcast NGAY
    socketService.sendNewMessage(conversationId, response);
    
    return response;
}
```

**VẤN ĐỀ:**
1. Transaction chưa commit
2. Data chưa thực sự được lưu vào DB
3. Nếu có lỗi sau đó → transaction rollback
4. Nhưng message đã broadcast rồi!
5. → Clients nhận "phantom messages" (tin nhắn ma không tồn tại)

**CÁCH ĐÚNG (Hiện tại):**
```java
@Transactional
public ChatMessageResponse sendMessage(...) {
    // Save to DB
    ChatMessage saved = repository.save(message);
    
    // ✅ ĐÚNG: Publish event
    eventPublisher.publishEvent(new MessageSentEvent(...));
    
    return response;
}
// → Transaction COMMIT
// → Listener được trigger
// → MỚI broadcast
```

---

### 2.4 MessageEventListener

**Mục đích:** Lắng nghe event và broadcast SAU KHI transaction commit

#### Code Chi Tiết

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {

    private final SocketService socketService;

    // ═══════════════════════════════════════════════════════
    // ANNOTATION QUAN TRỌNG NHẤT!
    // ═══════════════════════════════════════════════════════
    @Async  // ← Chạy trên thread pool riêng, không block request thread
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // ↑ CHỈ CHẠY SAU KHI TRANSACTION COMMIT THÀNH CÔNG
    public void handleMessageSent(MessageSentEvent event) {
        try {
            // Bây giờ an toàn để broadcast vì data đã có trong DB!
            socketService.sendNewMessage(
                event.getConversationId(), 
                event.getMessageResponse()
            );
            
        } catch (Exception e) {
            log.error("Failed to broadcast message: {}", e.getMessage());
        }
    }
}
```

#### Giải Thích `TransactionPhase`

Spring cung cấp 4 phases:

| Phase | Khi Nào Chạy | Use Case |
|-------|--------------|----------|
| `BEFORE_COMMIT` | Trước khi commit | Prepare data, validate |
| `AFTER_COMMIT` | Sau khi commit thành công | **Broadcast, send email** ✅ |
| `AFTER_ROLLBACK` | Sau khi rollback | Cleanup, notify về failure |
| `AFTER_COMPLETION` | Sau khi kết thúc (commit hoặc rollback) | Close resources |

**Tại sao dùng `AFTER_COMMIT`?**
- Đảm bảo data ĐÃ LƯU thành công vào DB
- Nếu transaction fail → listener KHÔNG chạy
- Nếu chạy ở `BEFORE_COMMIT` → vẫn có thể rollback sau đó

---

## 3. Event-Driven Pattern Giải Thích

### 3.1 Event-Driven Là Gì?

**Định nghĩa:** Thay vì gọi method trực tiếp, các component **phát ra events** (sự kiện) và component khác **lắng nghe** events đó.

**Ví dụ đời thực:**
- **Traditional:** Bạn gọi điện trực tiếp cho bạn: "Tôi vừa gửi tin nhắn"
- **Event-Driven:** Bạn post lên Facebook: "Tôi vừa gửi tin nhắn", ai quan tâm thì tự theo dõi

### 3.2 So Sánh Code

**TRƯỚC (Tight Coupling):**
```java
public class ChatMessageService {
    private final SocketService socketService;  // ← Phụ thuộc trực tiếp
    private final EmailService emailService;    // ← Phụ thuộc trực tiếp
    private final NotificationService notificationService;  // ← Thêm service mới phải sửa code
    
    public void sendMessage() {
        // Save message
        save(message);
        
        // ❌ Gọi trực tiếp nhiều services
        socketService.broadcast(message);
        emailService.sendEmail(message);
        notificationService.push(message);
        
        // Muốn thêm feature mới? Phải sửa method này!
    }
}
```

**SAU (Event-Driven):**
```java
public class ChatMessageService {
    private final ApplicationEventPublisher eventPublisher;  // ← CHỈ cần 1 dependency
    
    public void sendMessage() {
        // Save message
        save(message);
        
        // ✅ Chỉ cần publish event
        eventPublisher.publishEvent(new MessageSentEvent(message));
        
        // Thêm feature mới? Tạo listener mới, KHÔNG cần sửa code này!
    }
}

// Listener 1: Broadcast qua Socket.IO
@EventListener
public class MessageBroadcastListener {
    public void handleMessageSent(MessageSentEvent event) {
        socketService.broadcast(event.getMessage());
    }
}

// Listener 2: Send email
@EventListener
public class EmailListener {
    public void handleMessageSent(MessageSentEvent event) {
        emailService.send(event.getMessage());
    }
}

// Listener 3: Push notification (thêm mới dễ dàng!)
@EventListener
public class PushListener {
    public void handleMessageSent(MessageSentEvent event) {
        notificationService.push(event.getMessage());
    }
}
```

### 3.3 Lợi Ích Event-Driven

1. **Loose Coupling:** Service không biết ai đang listen
2. **Easy to Extend:** Thêm listener mà không sửa code cũ
3. **Transaction Safety:** Có thể chỉ định khi nào listener chạy
4. **Async Processing:** Listener có thể chạy background

---

## 4. WebSocket vs HTTP

### 4.1 Bảng So Sánh

| Tiêu Chí | HTTP | WebSocket |
|----------|------|-----------|
| **Kết nối** | Request → Response → Đóng | Kết nối liên tục |
| **Hướng** | Client → Server (1 chiều) | Client ↔ Server (2 chiều) |
| **Overhead** | Header lớn mỗi request (~1KB) | Header nhỏ (~2 bytes) |
| **Latency** | Cao (TCP handshake mỗi lần) | Thấp (reuse connection) |
| **Real-time** | Phải polling | Push trực tiếp |
| **Use Case** | REST API, Load page | Chat, Game, Live update |

### 4.2 HTTP Polling (Cách Cũ)

```javascript
// Client phải liên tục hỏi server
setInterval(() => {
    fetch('/api/messages/new')
        .then(res => res.json())
        .then(messages => {
            if (messages.length > 0) {
                displayMessages(messages);
            }
        });
}, 1000);  // Hỏi mỗi 1 giây

// ❌ VẤN ĐỀ:
// - 60 requests/phút (lãng phí bandwidth)
// - Delay tối thiểu 1 giây
// - Server bị stress với nhiều clients
```

### 4.3 WebSocket (Cách Mới)

```javascript
// Client kết nối 1 lần
const socket = io('ws://localhost:8085', {
    query: {
        token: 'JWT_TOKEN',
        userId: 'USER_ID'
    }
});

// Lắng nghe event từ server
socket.on('receive_message', (message) => {
    displayMessage(message);  // Hiển thị NGAY KHI CÓ
});

// ✅ LỢI ÍCH:
// - Không delay
// - Server chủ động push
// - Tiết kiệm bandwidth
```

---

## 5. Luồng Hoạt Động Chi Tiết

### 5.1 Kịch Bản: User A Gửi Message Cho User B

#### Phase 1: Kết Nối (Connection)

**Bước 1:** User A mở app, WebSocket client kết nối
```
Client → ws://localhost:8085?token=JWT_A&userId=USER_A_ID
```

**Bước 2:** Server nhận connection
```java
// SocketIOConfig.authorizeConnection() được gọi
authorizeConnection(handshakeData) {
    token = handshakeData.getParam("token");  // JWT_A
    jwtUtils.verifyToken(token);  // Kiểm tra token hợp lệ
    return SUCCESSFUL_AUTHORIZATION;
}
```

**Bước 3:** Sau khi authorize thành công
```java
// SocketService.onConnect() được gọi
onConnect(client) {
    userId = client.getParam("userId");  // USER_A_ID
    client.joinRoom("user_" + userId);   // Join room: user_USER_A_ID
    log.info("User connected: {}", userId);
}
```

**Kết quả:** User A đã kết nối và join vào room riêng

#### Phase 2: Join Conversation

**Bước 1:** User A vào màn hình chat với conversation `CONV_123`

**Bước 2:** Client emit event
```javascript
socket.emit('join_conversation', 'CONV_123');
```

**Bước 3:** Server nhận event
```java
// SocketService.onJoinConversation() được gọi
onJoinConversation(client, conversationId) {
    // conversationId = "CONV_123"
    roomName = "conversation_CONV_123";
    client.joinRoom(roomName);
    log.info("Client joined conversation: {}", conversationId);
}
```

**Bước 4:** User B cũng join vào conversation tương tự

**Kết quả:** Cả User A và B đều trong room `conversation_CONV_123`

#### Phase 3: Gửi Message

**Bước 1:** User A gõ "Hello" và nhấn send

**Bước 2:** Client gửi HTTP POST request
```
POST /conversations/CONV_123/messages
Body: {
  "messageContent": "Hello",
  "messageType": "TEXT"
}
```

**Bước 3:** Server nhận request, gọi `ChatMessageService.sendMessage()`
```java
sendMessage(conversationId, senderId, request) {
    // ① Validate user exists
    User sender = userRepository.findById(senderId);
    
    // ② Validate conversation exists
    Conversation conv = conversationRepository.findById(conversationId);
    
    // ③ Create message entity
    ChatMessage message = new ChatMessage();
    message.setContent("Hello");
    message.setSender(sender);
    message.setConversation(conv);
    message.setCreatedAt(now());
    
    // ④ Save to database
    ChatMessage saved = chatMessageRepository.save(message);
    // INSERT INTO chat_message VALUES (...)
    
    // ⑤ Update conversation timestamp
    conv.setLastMessageTimestamp(now());
    conversationRepository.save(conv);
    // UPDATE conversation SET last_message_timestamp = ...
    
    // ⑥ Convert to DTO
    ChatMessageResponse response = mapper.toResponse(saved);
    
    // ⑦ Publish event (CHƯA broadcast!)
    eventPublisher.publishEvent(
        new MessageSentEvent(conversationId, response)
    );
    
    return response;  // Trả về cho HTTP client
}
// → Transaction COMMIT tại đây
```

**Bước 4:** Transaction commit thànhcông
```
PostgreSQL: COMMIT;
```

**Bước 5:** MessageEventListener được trigger
```java
@TransactionalEventListener(phase = AFTER_COMMIT)
handleMessageSent(MessageSentEvent event) {
    // Bây giờ an toàn để broadcast!
    socketService.sendNewMessage(
        event.getConversationId(),  // CONV_123
        event.getMessageResponse()   // ChatMessageResponse
    );
}
```

**Bước 6:** SocketService broadcast message
```java
sendNewMessage(conversationId, messageResponse) {
    roomName = "conversation_CONV_123";
    
    // Lấy tất cả clients trong room
    server.getRoomOperations(roomName)
          .sendEvent("receive_message", messageResponse);
    
    // Gửi event "receive_message" tới:
    // - User A ✅
    // - User B ✅
}
```

**Bước 7:** Cả User A và User B nhận WebSocket event
```javascript
// Trên client
socket.on('receive_message', (message) => {
    console.log('New message:', message);
    // {
    //   id: "MSG_ID",
    //   messageContent: "Hello",
    //   senderId: "USER_A_ID",
    //   createdAt: "2025-12-09T10:00:00"
    // }
    
    displayMessage(message);  // Hiển thị tin nhắn
});
```

**Timeline Tổng Thể:**
```
10:00:00.000 - User A click "Send"
10:00:00.010 - HTTP POST request tới server
10:00:00.020 - ChatMessageService.sendMessage() bắt đầu
10:00:00.050 - Save message vào DB
10:00:00.060 - Update conversation
10:00:00.070 - Publish MessageSentEvent
10:00:00.080 - Return HTTP response (User A thấy tick xanh)
10:00:00.090 - Transaction COMMIT
10:00:00.100 - MessageEventListener.handleMessageSent() được gọi
10:00:00.110 - SocketService.sendNewMessage() broadcast
10:00:00.120 - User B nhận WebSocket event (hiển thị message)
```

---

## 6. Configuration Chi Tiết

### 6.1 application.yaml

```yaml
# Socket.IO Server Configuration
socket-server:
  host: localhost      # Địa chỉ server (localhost cho dev, 0.0.0.0 cho production)
  port: 8085          # Port riêng cho WebSocket (tách biệt với HTTP port 8080)
```

**Giải thích:**
- `host: localhost` → Chỉ accept connections từ localhost (dev)
- `host: 0.0.0.0` → Accept từ mọi IP (production)
- `port: 8085` → Tách biệt với HTTP server (port 8080)

**Tại sao tách port?**
- WebSocket dùng protocol khác HTTP
- Dễ dàng config load balancer riêng
- Tránh conflict với HTTP requests

### 6.2 Async Configuration (Bắt Buộc!)

Vì dùng `@Async` trong `MessageEventListener`, cần config thread pool:

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Số thread chạy liên tục
        executor.setCorePoolSize(5);
        
        // Số thread tối đa khi busy
        executor.setMaxPoolSize(10);
        
        // Queue size (task chờ xử lý)
        executor.setQueueCapacity(100);
        
        // Prefix cho thread name (dễ debug)
        executor.setThreadNamePrefix("async-");
        
        executor.initialize();
        return executor;
    }
}
```

**Giải thích:**
- `CorePoolSize(5)`: Luôn có 5 threads chờ sẵn
- `MaxPoolSize(10)`: Khi quá tải, tạo thêm tối đa 10 threads
- `QueueCapacity(100)`: Nếu 10 threads đều busy, queue tối đa 100 tasks

**Ví dụ thực tế:**
```
Có 3 messages đồng thời:
Thread-1: Broadcast message #1
Thread-2: Broadcast message #2
Thread-3: Broadcast message #3

Có 12 messages đồng thời:
Thread-1 đến Thread-10: Xử lý 10 messages
Queue: 2 messages còn lại chờ
```

### 6.3 Transaction Configuration

Spring Boot tự động config, nhưng nếu cần customize:

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}
```

### 6.4 Redis Configuration

Redisson config cho Socket.IO session store:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}  # Từ environment variable
```

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://localhost:6379")
              .setPassword(redisPassword);
        
        return Redisson.create(config);
    }
}
```

---

## 7. Best Practices Đã Implement

### 7.1 Transaction-Safe Broadcasting ✅

**Vấn đề:** Broadcast trước khi commit → phantom messages

**Giải pháp:**
```java
// ❌ SAI
@Transactional
public void sendMessage() {
    save(message);
    socketService.broadcast(message);  // Trước commit!
}

// ✅ ĐÚNG
@Transactional
public void sendMessage() {
    save(message);
    eventPublisher.publish(event);  // Còn trong transaction
}

@TransactionalEventListener(phase = AFTER_COMMIT)
public void handleEvent(MessageSentEvent event) {
    socketService.broadcast(event);  // SAU commit!
}
```

### 7.2 Rate Limiting ✅

**Vấn đề:** User spam events → server overload

**Giải pháp: Bucket4j Token Bucket Algorithm**

```java
@Component
public class SocketRateLimiter {
    
    // Map lưu bucket cho mỗi user
    private final Map<String, Bucket> messageBuckets = new ConcurrentHashMap<>();
    
    // Giới hạn: 10 messages/second
    private static final int MESSAGE_LIMIT = 10;
    
    public boolean allowMessage(String userId) {
        // Lấy hoặc tạo bucket cho user
        Bucket bucket = messageBuckets.computeIfAbsent(
            userId, 
            k -> createMessageBucket()
        );
        
        // Try consume 1 token
        // True = còn token → cho phép
        // False = hết token → reject
        return bucket.tryConsume(1);
    }
    
    private Bucket createMessageBucket() {
        // Bandwidth = 10 tokens, refill 10 tokens mỗi 1 giây
        Bandwidth limit = Bandwidth.classic(
            MESSAGE_LIMIT, 
            Refill.intervally(MESSAGE_LIMIT, Duration.ofSeconds(1))
        );
        
        return Bucket.builder().addLimit(limit).build();
    }
}
```

**Token Bucket Algorithm Giải Thích:**

```
Lúc đầu: Bucket có 10 tokens
[🪙🪙🪙🪙🪙🪙🪙🪙🪙🪙]

User gửi message #1: Consume 1 token
[🪙🪙🪙🪙🪙🪙🪙🪙🪙_]

User gửi 9 messages nữa NGAY LẬP TỨC:
[__________] ← Hết tokens!

User gửi message #11: REJECTED ❌

Sau 1 giây: Refill 10 tokens
[🪙🪙🪙🪙🪙🪙🪙🪙🪙🪙] ← Lại có thể gửi
```

**Integration vào SocketService:**
```java
@OnEvent("typing")
public void onTyping(SocketIOClient client, String conversationId) {
    String userId = getUserId(client);
    
    // Kiểm tra rate limit
    if (!rateLimiter.allowTyping(userId)) {
        log.warn("Rate limit exceeded: userId={}", userId);
        return;  // Bỏ qua event này
    }
    
    // Xử lý bình thường
    broadcastTyping(userId, conversationId);
}
```

### 7.3 Room-Based Broadcasting ✅

**Vấn đề:** Broadcast tới ALL clients → lãng phí bandwidth

**Giải pháp:** Chỉ broadcast tới clients trong room

```java
// ❌ SAI: Broadcast tới ALL
server.getBroadcastOperations()
      .sendEvent("receive_message", message);
// → 10,000 clients nhận, nhưng chỉ 2 clients cần!

// ✅ ĐÚNG: Broadcast tới room
server.getRoomOperations("conversation_123")
      .sendEvent("receive_message", message);
// → Chỉ clients trong room nhận
```

### 7.4 JWT Authentication ✅

**Flow:**
1. Client login → Nhận JWT token
2. Client connect WebSocket với token trong URL
3. Server verify token trước khi accept connection

```java
private AuthorizationResult authorizeConnection(HandshakeData data) {
    String token = data.getSingleUrlParam("token");
    
    try {
        // Verify token signature
        SignedJWT jwt = jwtUtils.verifyToken(token, false);
        
        // Check expiration
        if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date())) {
            return FAILED_AUTHORIZATION;
        }
        
        return SUCCESSFUL_AUTHORIZATION;
        
    } catch (Exception e) {
        return FAILED_AUTHORIZATION;
    }
}
```

### 7.5 Centralized Error Handling ✅

```java
public class SocketExceptionHandler extends DefaultExceptionListener {

    @Override
    public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        
        // Log với context
        log.error("Socket event error: userId={}, args={}, error={}", 
            userId, args, e.getMessage(), e);

        // Notify client về error
        client.sendEvent("error", new ErrorResponse(
            "EVENT_ERROR",
            e.getMessage()
        ));
    }

    @Override
    public void onConnectException(Exception e, SocketIOClient client) {
        log.error("Connection failed: {}", e.getMessage());
        
        // Có thể send error message trước khi disconnect
        if (client != null) {
            client.sendEvent("error", new ErrorResponse(
                "CONNECTION_FAILED",
                "Authentication failed"
            ));
        }
    }
}
```

**Error Response Format:**
```json
{
  "type": "EVENT_ERROR",
  "message": "Failed to process typing event",
  "timestamp": 1702123456789
}
```

---

## 8. Testing & Debugging

### 8.1 Test WebSocket Connection (Postman)

**Bước 1:** Mở Postman → New → WebSocket Request

**Bước 2:** URL
```
ws://localhost:8085?token=YOUR_JWT_TOKEN&userId=YOUR_USER_ID
```

**Bước 3:** Click "Connect"

**Expected Response:**
```
✓ Connected to ws://localhost:8085
```

**Server Log:**
```
INFO: Socket.IO server configured on localhost:8085
INFO: Socket.IO server started successfully  
INFO: User connected: userId=abc-def, sessionId=xyz-123
```

### 8.2 Test Join Conversation

**Trong Postman WebSocket:**

**Message Tab → Compose:**
```json
{
  "event": "join_conversation",
  "args": ["6d5fd425-cfbf-4e56-8522-0923007c468a"]
}
```

**Click "Send"**

**Expected Server Log:**
```
INFO: Client joined conversation: userId=abc-def, conversationId=6d5fd425-cfbf-4e56-8522-0923007c468a
```

### 8.3 Test Send Message End-to-End

**Terminal 1: Connect Client A**
```bash
# Postman: ws://localhost:8085?token=TOKEN_A&userId=USER_A
# Emit: join_conversation → CONV_123
```

**Terminal 2: Connect Client B**
```bash
# Postman: ws://localhost:8085?token=TOKEN_B&userId=USER_B  
# Emit: join_conversation → CONV_123
```

**Terminal 3: Send HTTP POST**
```bash
curl -X POST http://localhost:8080/conversations/CONV_123/messages \
  -H "Authorization: Bearer TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{
    "messageContent": "Hello from A!",
    "messageType": "TEXT"
  }'
```

**Expected:**
- Terminal 1 (Client A): Nhận event `receive_message`
- Terminal 2 (Client B): Nhận event `receive_message`
- Server log:
```
INFO: Message saved: messageId=...
INFO: MessageSentEvent published
INFO: Transaction committed
INFO: Message broadcasted: messageId=..., conversationId=CONV_123
```

### 8.4 Test Rate Limiting

**Script:** Gửi 20 typing events liên tục

```javascript
// Trong Postman hoặc browser console
for (let i = 0; i < 20; i++) {
    socket.emit('typing', 'CONV_123');
}
```

**Expected:**
- Chỉ 1-2 events đầu được xử lý
- Server log:
```
WARN: Rate limit exceeded: userId=abc-def
```

### 8.5 Debug Common Issues

#### Issue 1: Client không nhận message

**Checklist:**
1. ✅ Client đã connect?
2. ✅ Client đã join conversation room?
3. ✅ Client đang listen event `receive_message`?
4. ✅ Server có log "Message broadcasted"?

**Debug:**
```java
// Thêm log vào SocketService.sendNewMessage()
public void sendNewMessage(UUID conversationId, ChatMessageResponse message) {
    String roomName = "conversation_" + conversationId;
    
    Collection<SocketIOClient> clients = server.getRoomOperations(roomName).getClients();
    
    log.info("Broadcasting to room: {}, clients count: {}", 
        roomName, clients.size());
    
    if (clients.isEmpty()) {
        log.warn("No clients in room: {}", roomName);
    }
    
    server.getRoomOperations(roomName).sendEvent("receive_message", message);
}
```

#### Issue 2: Transaction rollback nhưng vẫn broadcast

**Nguyên nhân:** Listener chạy ở phase sai

**Fix:** Đảm bảo dùng `AFTER_COMMIT`
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```

#### Issue 3: LocalDateTime serialization error

**Error:**
```
InvalidDefinitionException: Java 8 date/time type not supported
```

**Fix:** Kiểm tra `SocketIOConfig` đã thêm `JavaTimeModule`:
```java
com.corundumstudio.socketio.protocol.JacksonJsonSupport jsonSupport = 
    new com.corundumstudio.socketio.protocol.JacksonJsonSupport(
        new ParameterNamesModule(),
        new JavaTimeModule()  // ← Đảm bảo có dòng này!
    );
```

---

## 9. Câu Hỏi Senior Có Thể Hỏi & Cách Trả Lời

### Q1: Tại sao dùng Event-Driven thay vì gọi trực tiếp?

**Trả lời:**
> Event-Driven giúp **loose coupling** và **transaction safety**.
>
> Nếu gọi trực tiếp `socketService.broadcast()` trong `@Transactional` method, message sẽ broadcast TRƯỚC KHI transaction commit. Nếu có lỗi sau đó, transaction rollback nhưng message đã broadcast rồi → clients nhận "phantom messages".
>
> Với Event-Driven, tôi dùng `@TransactionalEventListener(phase = AFTER_COMMIT)` để đảm bảo chỉ broadcast SAU KHI data đã lưu thành công vào database.

### Q2: Redis store có thể scale horizontal không?

**Trả lời:**
> Có. Redis làm shared session store giữa các server instances.
>
> Ví dụ: Client kết nối vào Server A, session được lưu trên Redis. Khi client reconnect, load balancer route tới Server B, Server B vẫn lấy được session từ Redis.
>
> Điều này cho phép chạy nhiều server instances phía sau load balancer mà không cần sticky sessions.

### Q3: Token Bucket algorithm hoạt động như thế nào?

**Trả lời:**
> Token Bucket giống như một "xô chứa coin".
>
> - Ban đầu có 10 coins
> - Mỗi request tiêu tốn 1 coin
> - Coins được refill theo thời gian (10 coins/giây)
> - Nếu hết coins → reject request
>
> Điều này smooth hơn so với "hard limit" vì cho phép burst traffic ngắn (vd: gửi 10 messages cùng lúc OK, nhưng không thể spam liên tục).

### Q4: Tại sao tách Socket.IO server ra port riêng?

**Trả lời:**
> Có 3 lý do chính:
>
> 1. **Protocol khác nhau**: WebSocket cần upgrade từ HTTP, để riêng port dễ config
> 2. **Load balancing**: Có thể config load balancer riêng cho WebSocket (sticky sessions, longer timeout)
> 3. **Resource isolation**: WebSocket connections long-lived, tách ra tránh ảnh hưởng REST API

### Q5: Nếu MessageEventListener fail thì sao?

**Trả lời:**
> Hiện tại listener có `try-catch`, nếu fail chỉ log error. Message vẫn đã lưu DB thành công (vì transaction đã commit).
>
> Để handle tốt hơn, có thể:
> 1. Retry mechanism với `@Retryable`
> 2. Dead letter queue (lưu failed messages vào queue riêng)
> 3. Alert/monitoring khi fail
>
> Trong production, tôi sẽ implement retry với exponential backoff:
> ```java
> @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
> ```

---

## 10. Tóm Tắt Key Points Để Nhớ

### Architecture
- ✅ Event-Driven với Spring `ApplicationEventPublisher`
- ✅ Transaction-safe broadcasting với `@TransactionalEventListener(AFTER_COMMIT)`
- ✅ Room-based messaging cho efficiency
- ✅ Redis-backed store cho horizontal scaling

### Security
- ✅ JWT authentication on connection
- ✅ Rate limiting với Bucket4j (10 msg/sec, 1 typing/sec)
- ✅ Centralized exception handling

### Performance
- ✅ Async processing với `@Async`
- ✅ Thread pool configuration
- ✅ WebSocket long-lived connections (tiết kiệm overhead vs HTTP)

### Testing
- ✅ Postman WebSocket client
- ✅ End-to-end test với multiple clients
- ✅ Rate limiting verification

---

## 11. Tài Liệu Tham Khảo

- **Spring Events:** https://spring.io/guides/gs/spring-boot/
- **Netty-SocketIO:** https://github.com/mrniko/netty-socketio
- **Bucket4j:** https://bucket4j.com/
- **WebSocket Protocol:** https://datatracker.ietf.org/doc/html/rfc6455

---