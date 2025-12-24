# Hệ Thống Notification - Hướng Dẫn Chi Tiết

> **Hệ thống thông báo real-time cho TripJoy Application**  
> Tài liệu này giải thích CỰC KỲ CHI TIẾT về kiến trúc, thiết kế, và cách sử dụng notification system.

---

## 📚 Mục Lục

1. [Tổng Quan Kiến Trúc](#1-tổng-quan-kiến-trúc)
2. [Thiết Kế Database Schema](#2-thiết-kế-database-schema)
3. [Các Thành Phần Core](#3-các-thành-phần-core)
4. [Event-Driven Pattern](#4-event-driven-pattern)
5. [REST API Endpoints](#5-rest-api-endpoints)
6. [Tích Hợp Real-time](#6-tích-hợp-real-time)
7. [Hướng Dẫn Tích Hợp](#7-hướng-dẫn-tích-hợp)
8. [Hệ Thống ActivityLog](#8-hệ-thống-activitylog)
9. [Hướng Dẫn Testing](#9-hướng-dẫn-testing)
10. [Best Practices](#10-best-practices)

---

## 1. Tổng Quan Kiến Trúc

### 1.1 Vấn Đề Cần Giải Quyết

**Tình huống:** Ứng dụng social travel cần thông báo user về các tương tác (likes, comments, invites, v.v.)

**Thách thức:**
- Cần lưu trữ notification riêng cho từng user với trạng thái riêng (đã đọc/chưa đọc, đã lưu trữ)
- Phải gửi thông báo real-time qua Socket.IO
- Notification chỉ được gửi SAU KHI database transaction commit thành công
- Cần tránh memory leak khi có nhiều notifications
- Phải audit trail (ghi lại) toàn bộ hành động của users

**Giải pháp:** User-Centric Notification System + Event-Driven Architecture + ActivityLog

### 1.2 Kiến Trúc Tổng Thể

```
┌─────────────────┐         HTTP POST          ┌──────────────────┐
│  Client App     │──────────────────────────► │  PostController  │
│  (Like a post)  │                            │  likePost()      │
└─────────────────┘                            └────────┬─────────┘
                                                        │
                                                ┌───────▼─────────┐
                                                │  PostService    │
                                                │  - Lưu like     │
                                                │  - Publish evt  │
                                                └────────┬────────┘
                                                         │
                                                ┌────────▼─────────┐
                                                │  Transaction     │
                                                │  COMMIT          │
                                                └────────┬─────────┘
                                                         │
                              ┌──────────────────────────┴─────────────────┐
                              │                                            │
                      ┌───────▼────────────┐                   ┌──────────▼─────────┐
                      │NotificationEvent   │                   │  ActivityLog       │
                      │Listener            │                   │  (Tùy chọn)        │
                      │@AFTER_COMMIT       │                   └────────────────────┘
                      └───────┬────────────┘
                              │
                   ┌──────────┴──────────┐
                   │                     │
           ┌───────▼────────┐    ┌──────▼──────────┐
           │ Lưu vào DB     │    │ Socket.IO       │
           │  notification  │    │  Broadcast      │
           └────────────────┘    └──────┬──────────┘
                                        │
                                ┌───────▼──────────┐
                                │  Client Nhận     │
                                │  Thông báo       │
                                └──────────────────┘
```

### 1.3 Quyết Định Thiết Kế Quan Trọng

| Quyết Định | Lý Do | Phương Án Bị Từ Chối |
|-----------|-------|---------------------|
| **User-Centric** (1 notification = 1 người nhận) | Dễ scale, query đơn giản | Many-to-many (state phức tạp) |
| **Polymorphic Reference** (entityType + entityId) | Linh hoạt cho mọi entity | Foreign key đến mọi entity (quá nhiều columns) |
| **Hard Delete** (xóa thẳng, không soft delete) | Đơn giản, có Archive để lưu lịch sử | Soft delete (trùng lặp với Archive) |
| **Event-Driven** (publish event) | Tách biệt, an toàn transaction | Gọi trực tiếp (nguy cơ phantom notifications) |

---

## 2. Thiết Kế Database Schema

### 2.1 Notification Entity

**Thiết kế User-Centric:** Mỗi notification chỉ có 1 người nhận duy nhất

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    
    -- AI nhận (BẮT BUỘC)
    recipient_id UUID NOT NULL REFERENCES users(id),
    
    -- AI gây ra (TÙY CHỌN - null với system notifications)
    actor_id UUID REFERENCES users(id),
    
    -- HÀNH ĐỘNG GÌ xảy ra
    type VARCHAR(50) NOT NULL,  -- NotificationType enum
    
    -- LIÊN QUAN ĐẾN entity nào (polymorphic reference)
    entity_type VARCHAR(50),  -- "POST", "COMMENT", "GROUP"
    entity_id VARCHAR(255),   -- UUID dạng string
    
    -- NỘI DUNG
    title VARCHAR(255),
    message TEXT,
    metadata TEXT,  -- JSON string
    
    -- TRẠNG THÁI (của người nhận)
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    is_archived BOOLEAN DEFAULT false,
    
    -- ĐỘ ƯU TIÊN
    priority VARCHAR(20),  -- "HIGH", "NORMAL", "LOW"
    
    -- TIMESTAMPS (kế thừa từ BaseEntity)
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    
    -- INDEXES để tối ưu performance
    INDEX idx_recipient_unread (recipient_id, is_read, created_at),
    INDEX idx_recipient_created (recipient_id, created_at),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_actor (actor_id, created_at)
);
```

### 2.2 Tại Sao KHÔNG Dùng Soft Delete?

**Logic:**
- **Archive**: User muốn xem lại sau này → `isArchived = true`
- **Delete**: Xóa vĩnh viễn không cần nữa → `DELETE FROM notifications`

**So sánh:**
```java
// ❌ Soft Delete (phức tạp, trùng lặp)
private SoftDeleteInfo softDeleteInfo;
// Phải check softDeleteInfo.isDeleted = false ở MỌI query
// Trùng lặp chức năng với isArchived

// ✅ Hard Delete + Archive (đơn giản, rõ ràng)
private Boolean isArchived = false;
// Archive: giữ lại để xem sau
// Delete: xóa hẳn không cần nữa
```

### 2.3 Polymorphic Reference Pattern

**Tại sao KHÔNG dùng foreign keys cho từng entity?**

```java
// ❌ SAI: Phải thêm column mỗi lần có entity mới
@ManyToOne Post post;
@ManyToOne Comment comment;
@ManyToOne Group group;
@ManyToOne Itinerary itinerary;
// ... phải ALTER TABLE mỗi lần thêm entity type mới!

// ✅ ĐÚNG: Generic reference, linh hoạt
private String entityType;  // "POST"
private String entityId;    // "uuid-string"
```

**Khi cần fetch entity:**
```java
private Object fetchEntity(String entityType, String entityId) {
    return switch (entityType) {
        case "POST" -> postRepository.findById(UUID.fromString(entityId));
        case "COMMENT" -> commentRepository.findById(UUID.fromString(entityId));
        case "GROUP" -> groupRepository.findById(UUID.fromString(entityId));
        default -> null;
    };
}
```

**Câu hỏi:** Vậy ERD diagram vẽ thế nào?

**Trả lời:**
```mermaid
erDiagram
    users ||--o{ notifications : "recipient (FK thật)"
    users ||--o{ notifications : "actor (FK thật)"
    
    notifications {
        uuid recipient_id FK
        uuid actor_id FK
        varchar entity_type
        varchar entity_id
    }
    
    notifications -.-> posts : "Logical reference (KHÔNG có FK)"
    notifications -.-> comments : "Logical reference (KHÔNG có FK)"
```

- **Solid line (`||--o{`)**: Foreign key thật trong database
- **Dotted line (`-.->`)**: Logical reference, KHÔNG có constraint

### 2.4 Tối Ưu Hóa Indexes

**Tại sao cần các indexes này?**

```sql
-- Query: Lấy notification chưa đọc (THƯỜNG XUYÊN NHẤT)
-- SELECT * FROM notifications 
-- WHERE recipient_id = ? AND is_read = false 
-- ORDER BY created_at DESC;
INDEX idx_recipient_unread (recipient_id, is_read, created_at);
-- ↑ Composite index cover cả 3 columns → NHANH

-- Query: Lấy tất cả notifications phân trang
-- SELECT * FROM notifications 
-- WHERE recipient_id = ? 
-- ORDER BY created_at DESC;
INDEX idx_recipient_created (recipient_id, created_at);

-- Query: Tìm notifications theo entity (cho cleanup)
-- SELECT * FROM notifications 
-- WHERE entity_type = 'POST' AND entity_id = ?;
INDEX idx_entity (entity_type, entity_id);

-- Query: Tìm notifications theo actor (hiếm khi dùng)
-- SELECT * FROM notifications 
-- WHERE actor_id = ?;
INDEX idx_actor (actor_id, created_at);
```

---

## 3. Các Thành Phần Core

### 3.1 NotificationType Enum

**23 loại notification** bao quát mọi use cases:

```java
public enum NotificationType {
    // TƯƠNG TÁC VỚI POST (4 loại)
    POST_LIKED,          // Ai đó thích post của bạn
    POST_COMMENTED,      // Ai đó comment vào post của bạn
    POST_SAVED,          // Ai đó lưu post của bạn
    POST_SHARED,         // Ai đó chia sẻ post của bạn
    
    // TƯƠNG TÁC VỚI COMMENT (3 loại)
    COMMENT_LIKED,       // Ai đó thích comment của bạn
    COMMENT_REPLIED,     // Ai đó trả lời comment của bạn
    COMMENT_MENTIONED,   // Bạn được mention trong comment
    
    // HOẠT ĐỘNG GROUP (6 loại)
    GROUP_INVITE,        // Bạn được mời vào group
    GROUP_MEMBER_JOINED, // Ai đó tham gia group của bạn
    GROUP_MEMBER_LEFT,   // Ai đó rời khỏi group
    GROUP_ROLE_CHANGED,  // Vai trò của bạn trong group thay đổi
    GROUP_ITINERARY_CREATED, // Lịch trình mới trong group của bạn
    
    // TƯƠNG TÁC CHAT (3 loại)
    CHAT_MESSAGE,        // Tin nhắn mới (cho mentions)
    CHAT_MESSAGE_LIKED,  // Ai đó thích tin nhắn của bạn
    CHAT_MENTIONED,      // Bạn được mention trong chat
    
    // TƯƠNG TÁC ITINERARY (4 loại)
    ITINERARY_SHARED,    // Lịch trình được chia sẻ với bạn
    ITINERARY_LIKED,     // Ai đó thích lịch trình của bạn
    ITINERARY_UPDATED,   // Lịch trình bạn theo dõi được cập nhật
    ITINERARY_COLLABORATOR_ADDED, // Bạn được thêm vào làm cộng tác viên
    
    // HỆ THỐNG (3 loại)
    SYSTEM_ANNOUNCEMENT, // Thông báo toàn hệ thống
    ACCOUNT_VERIFICATION,// Trạng thái xác minh tài khoản
    CREDITS_AWARDED      // Điểm thưởng được trao
}
```

### 3.2 Notification Entity

```java
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_recipient_unread", 
           columnList = "recipient_id, is_read, created_at"),
    @Index(name = "idx_recipient_created", 
           columnList = "recipient_id, created_at"),
    @Index(name = "idx_entity", 
           columnList = "entity_type, entity_id"),
    @Index(name = "idx_actor", 
           columnList = "actor_id, created_at")
})
public class Notification extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;  // AI nhận
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;  // AI gây ra (null cho system)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    // Polymorphic reference
    private String entityType;
    private String entityId;
    
    // Nội dung
    private String title;
    private String message;
    private String metadata;  // JSON string
    
    // Trạng thái riêng của user
    @Builder.Default
    private Boolean isRead = false;
    
    private LocalDateTime readAt;
    
    @Builder.Default
    private Boolean isArchived = false;
    
    private String priority;
}
```

### 3.3 NotificationRepository

**Hơn 15 queries được tối ưu:**

```java
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    // QUERIES lấy dữ liệu
    Page<Notification> findByRecipient(UUID userId, Pageable pageable);
    Page<Notification> findUnreadByRecipient(UUID userId, Pageable pageable);
    Page<Notification> findArchivedByRecipient(UUID userId, Pageable pageable);
    
    // QUERIES đếm
    Long countUnreadByRecipient(UUID userId);
    Long countByRecipient(UUID userId);
    
    // QUERY kiểm tra quyền sở hữu (security)
    Optional<Notification> findByIdAndRecipient(UUID notificationId, UUID userId);
    
    // QUERIES cập nhật
    int markAsRead(UUID notificationId, UUID userId, LocalDateTime readAt);
    int markAllAsRead(UUID userId, LocalDateTime readAt);
    int updateArchived(UUID notificationId, UUID userId, boolean archived, LocalDateTime now);
    
    // QUERIES theo entity
    List<Notification> findByEntity(String entityType, String entityId);
    Page<Notification> findByActor(UUID actorId, Pageable pageable);
    
    // CLEANUP
    int archiveOldReadNotifications(LocalDateTime threshold, LocalDateTime now);
```

---

## 4. Event-Driven Pattern

### 4.1 NotificationEvent DTO

**Tách biệt business logic khỏi notification creation:**

```java
@Getter
@Setter
@Builder
public class NotificationEvent {
    private UUID recipientId;  // BẮT BUỘC
    private UUID actorId;      // Tùy chọn (null cho system)
    private NotificationType type;
    
    // Polymorphic reference
    private String entityType;
    private String entityId;
    
    // Nội dung
    private String title;
    private String message;
    private Map<String, Object> metadata;  // Sẽ được serialize thành JSON
    
    // Độ ưu tiên
    private String priority;
}
```

### 4.2 NotificationEventListener

**Pattern:** `@Async + @TransactionalEventListener(AFTER_COMMIT)`

```java
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SocketService socketService;
    private final ObjectMapper objectMapper;  // Serialize JSON
    
    // ═══════════════════════════════════════════════════════════
    // LISTENER CHỈ CHẠY SAU KHI TRANSACTION COMMIT THÀNH CÔNG
    // ═══════════════════════════════════════════════════════════
    @Async  // ← Chạy trên thread pool riêng
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // ↑ CHỈ chạy SAU KHI transaction commit
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            // 1. Validate & lấy recipient
            User recipient = userRepository.findById(event.getRecipientId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            // 2. Lấy actor (tùy chọn)
            User actor = null;
            if (event.getActorId() != null) {
                actor = userRepository.findById(event.getActorId()).orElse(null);
            }
            
            // 3. Serialize metadata Map → JSON string
            String metadataJson = null;
            if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
                metadataJson = objectMapper.writeValueAsString(event.getMetadata());
            }
            
            // 4. Tạo notification entity
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .actor(actor)
                    .type(event.getType())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .metadata(metadataJson)
                    .priority(event.getPriority())
                    .isRead(false)
                    .isArchived(false)
                    .build();
                    
            Notification saved = notificationRepository.save(notification);
            
            // 5. Broadcast qua Socket.IO
            socketService.sendNotification(event.getRecipientId(), saved);
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý notification event", e);
        }
    }
}
```

### 4.3 Tại Sao KHÔNG Dùng `@Transactional` Trên Listener?

**Quy tắc Spring:**
```
@TransactionalEventListener + @Transactional = ❌ CONFLICT
(Trừ khi dùng REQUIRES_NEW hoặc NOT_SUPPORTED)
```

**Pattern đúng:**
```java
// ❌ SAI
@Transactional
@TransactionalEventListener(phase = AFTER_COMMIT)

// ✅ ĐÚNG
@Async
@TransactionalEventListener(phase = AFTER_COMMIT)
```

**Giải thích:**
- `@TransactionalEventListener(AFTER_COMMIT)` đã đảm bảo parent transaction commit rồi
- Thêm `@Transactional` gây conflict
- `@Async` chạy trong thread pool riêng, không block request thread

**Tại sao cần AFTER_COMMIT?**

```
TRƯỚC (không dùng AFTER_COMMIT):
1. PostService.likePost() → Save like → Publish Event
2. Listener chạy NGAY → Broadcast notification
3. Transaction CHƯA commit
4. Nếu có lỗi → Rollback
5. → Client nhận "phantom notification" (notification ma không tồn tại!)

SAU (dùng AFTER_COMMIT):
1. PostService.likePost() → Save like → Publish Event
2. Transaction COMMIT thành công
3. Listener mới chạy → Broadcast notification
4. → An toàn vì data đã có trong DB!
```

---

## 5. REST API Endpoints

### 5.1 Endpoint Constants

```java
// Endpoint.java
public static final class Notification {
    public static final String BASE = API_PREFIX + "/notifications";
    public static final String ID = "/{notificationId}";
    public static final String UNREAD_COUNT = "/unread-count";
    public static final String MARK_READ = ID + "/read";
    public static final String MARK_ALL_READ = "/mark-all-read";
    public static final String ARCHIVE = ID + "/archive";
}
```

### 5.2 Tham Chiếu API

| Method | Endpoint | Mô Tả |
|--------|----------|-------|
| GET | `/api/v1/notifications` | Lấy danh sách notifications (phân trang) |
| GET | `/api/v1/notifications/unread-count` | Lấy số notification chưa đọc (badge) |
| GET | `/api/v1/notifications/{id}` | Lấy 1 notification cụ thể |
| PUT | `/api/v1/notifications/{id}/read` | Đánh dấu đã đọc |
| PUT | `/api/v1/notifications/mark-all-read` | Đánh dấu tất cả đã đọc |
| PUT | `/api/v1/notifications/{id}/archive` | Lưu trữ/bỏ lưu trữ |
| DELETE | `/api/v1/notifications/{id}` | Xóa vĩnh viễn (hard delete) |

### 5.3 Ví Dụ Request/Response

#### Lấy Danh Sách Notifications
```http
GET /api/v1/notifications?unreadOnly=true&page=0&size=20
Authorization: Bearer {JWT_TOKEN}

Response 200 OK:
{
  "data": {
    "content": [
      {
        "id": "uuid-123",
        "recipient": {
          "id": "user-1",
          "username": "john_doe"
        },
        "actor": {
          "id": "user-2",
          "username": "jane_doe"
        },
        "type": "POST_LIKED",
        "entityType": "POST",
        "entityId": "post-uuid",
        "message": "jane_doe thích bài viết của bạn",
        "isRead": false,
        "isArchived": false,
        "createdAt": "2025-12-17T10:30:00"
      }
    ],
    "totalElements": 42,
    "totalPages": 3
  }
}
```

#### Lấy Số Chưa Đọc (Badge)
```http
GET /api/v1/notifications/unread-count
Authorization: Bearer {JWT_TOKEN}

Response 200 OK:
{
  "data": 5
}
```

#### Đánh Dấu Đã Đọc
```http
PUT /api/v1/notifications/{id}/read
Authorization: Bearer {JWT_TOKEN}

Response 200 OK:
{
  "message": "Đã đánh dấu đọc"
}
```

---

## 6. Tích Hợp Real-time

### 6.1 Cập Nhật SocketService

```java
@Service
public class SocketService {
    
    private final SocketIOServer server;
    
    /**
     * Gửi notification đến user cụ thể
     * Sử dụng room user_{userId} đã có sẵn
     */
    public void sendNotification(UUID userId, Object notification) {
        try {
            String roomName = "user_" + userId;
            
            // Broadcast tới room riêng của user
            server.getRoomOperations(roomName)
                  .sendEvent("notification", notification);
                  
            log.info("Đã gửi notification đến user: {}", userId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi notification", e);
        }
    }
}
```

### 6.2 Tích Hợp Frontend

**Ví dụ React:**
```javascript
import io from 'socket.io-client';

// Kết nối Socket.IO
const socket = io('http://localhost:9092', {
  query: { 
    userId: currentUser.id,
    token: jwtToken 
  }
});

// Lắng nghe notifications
socket.on('notification', (notification) => {
  // Cập nhật badge count
  setUnreadCount(prev => prev + 1);
  
  // Hiện toast
  toast.info(notification.message);
  
  // Thêm vào danh sách
  setNotifications(prev => [notification, ...prev]);
  
  // Phát âm thanh
  playNotificationSound();
});

// Fetch dữ liệu ban đầu khi mount
useEffect(() => {
  fetchNotifications();
  fetchUnreadCount();
}, []);
```

---

## 7. Hướng Dẫn Tích Hợp

### 7.1 Cách Publish Notifications

**Ví dụ 1: PostService - Like Post**
```java
@Service
public class PostService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public void likePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId).orElseThrow(...);
        User user = userRepository.findById(userId).orElseThrow(...);
        
        // Business logic
        if (!post.getLikeUsers().contains(user)) {
            post.getLikeUsers().add(user);
            postRepository.save(post);
            
            // ✅ Publish notification event
            if (!post.getCreator().getId().equals(userId)) {  // Không tự thông báo cho mình
                eventPublisher.publishEvent(NotificationEvent.builder()
                    .recipientId(post.getCreator().getId())
                    .actorId(userId)
                    .type(NotificationType.POST_LIKED)
                    .entityType("POST")
                    .entityId(postId.toString())
                    .message(user.getUsername() + " thích bài viết của bạn")
                    .metadata(Map.of(
                        "postPreview", post.getContent().substring(0, 50),
                        "postId", postId.toString()
                    ))
                    .build());
            }
        }
    }
}
```

**Ví dụ 2: CommentService - Mention User**
```java
@Transactional
public CommentResponse createComment(CommentRequest request) {
    Comment comment = commentMapper.toEntity(request);
    Comment saved = commentRepository.save(comment);
    
    // Parse mentions từ nội dung (ví dụ: @username)
    List<UUID> mentionedUserIds = parseMentions(request.getContent());
    
    // ✅ Gửi notification cho mỗi người được mention
    mentionedUserIds.forEach(mentionedId -> {
        if (!mentionedId.equals(currentUserId)) {
            eventPublisher.publishEvent(NotificationEvent.builder()
                .recipientId(mentionedId)
                .actorId(currentUserId)
                .type(NotificationType.COMMENT_MENTIONED)
                .entityType("COMMENT")
                .entityId(saved.getId().toString())
                .message(currentUser.getUsername() + " nhắc đến bạn")
                .metadata(Map.of(
                    "commentText", request.getContent(),
                    "postId", request.getPostId().toString()
                ))
                .build());
        }
    });
    
    return commentMapper.toResponse(saved);
}
```

### 7.2 Best Practices

**NÊN:**
- ✅ Luôn check `!equals(currentUserId)` để tránh tự thông báo cho mình
- ✅ Dùng `@Transactional` trên service methods publish events
- ✅ Cung cấp context trong metadata (postPreview, commentText, v.v.)
- ✅ Set đúng type (POST_LIKED, COMMENT_REPLIED, v.v.)

**KHÔNG NÊN:**
- ❌ Đừng gọi `socketService` trực tiếp - dùng events
- ❌ Đừng publish events trước khi save vào DB
- ❌ Đừng quên set entityType và entityId
- ❌ Đừng dùng `@Transactional` trên event listener

---

## 8. Hệ Thống ActivityLog

### 8.1 Mục Đích

**ActivityLog** cung cấp audit trail (ghi lại) chi tiết cho TẤT CẢ hành động của users.

**Phạm vi:** Posts, Comments, Groups, Notifications, Itineraries, Chat, User actions

### 8.2 ActivityAction Enum

```java
public enum ActivityAction {
    // HÀNH ĐỘNG VỚI POST (8 actions)
    POST_CREATED, POST_UPDATED, POST_DELETED,
    POST_LIKED, POST_UNLIKED,
    POST_SAVED, POST_UNSAVED, POST_SHARED,
    
    // HÀNH ĐỘNG VỚI COMMENT (6 actions)
    COMMENT_CREATED, COMMENT_UPDATED, COMMENT_DELETED,
    COMMENT_LIKED, COMMENT_UNLIKED, COMMENT_REPLIED,
    
    // HÀNH ĐỘNG VỚI GROUP (8 actions)
    GROUP_CREATED, GROUP_UPDATED, GROUP_DELETED,
    GROUP_JOINED, GROUP_LEFT,
    GROUP_MEMBER_ADDED, GROUP_MEMBER_REMOVED, GROUP_ROLE_CHANGED,
    
    // HÀNH ĐỘNG VỚI NOTIFICATION (4 actions)
    NOTIFICATION_CREATED, NOTIFICATION_SENT,
    NOTIFICATION_READ, NOTIFICATION_DELETED,
    
    // ... Tổng 48 actions
}
```

### 8.3 ActivityLog Entity

```java
@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_user_action", 
           columnList = "user_id, action, created_at"),
    @Index(name = "idx_entity", 
           columnList = "entity_type, entity_id"),
    @Index(name = "idx_created_at", 
           columnList = "created_at")
})
public class ActivityLog extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // AI thực hiện
    
    @Enumerated(EnumType.STRING)
    private ActivityAction action;  // HÀNH ĐỘNG GÌ
    
    // Polymorphic reference
    private String entityType;  // LOẠI ENTITY GÌ
    private String entityId;    // ID CỦA ENTITY
    
    // Chi tiết
    private String metadata;  // JSON
    
    // ═══════════════════════════════════════════════════════════
    // THÔNG TIN REQUEST (cho security audit)
    // ═══════════════════════════════════════════════════════════
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;  // IP của request
    // ↑ Dùng để:
    // - Phát hiện hoạt động đáng ngờ (1 user login từ nhiều IP khác nhau)
    // - Geo-location analysis
    // - Security audit (ai truy cập từ đâu)
    // - Block IP độc hại
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;  // Browser/device info
    // ↑ Dùng để:
    // - Analytics: user dùng device gì (mobile vs desktop)
    // - Browser compatibility analysis
    // - Detect bot/crawler
    // - Security: phát hiện unusual user agents
}
```

### 8.4 Giải Thích 2 Fields Đặc Biệt

#### ipAddress - Địa chỉ IP của request

**Mục đích:**
```java
// 1. PHÁT HIỆN HOẠT ĐỘNG ĐÁNG NGỜ
User login từ Việt Nam lúc 9h sáng
→ 2h sau login từ Mỹ
→ ❌ Đáng ngờ! Tài khoản có thể bị hack

// 2. GEO-LOCATION ANALYSIS
SELECT country, COUNT(*) as users
FROM activity_logs 
WHERE action = 'USER_LOGIN'
GROUP BY country;
// → Biết users từ quốc gia nào nhiều nhất

// 3. SECURITY AUDIT
Ai truy cập sensitive data từ IP nào?
→ Truy vết khi có vấn đề

// 4. BLOCK IP ĐỘC HẠI
Phát hiện IP spam → Block luôn
```

**Ví dụ thực tế:**
```java
ActivityLog log = ActivityLog.builder()
    .user(currentUser)
    .action(ActivityAction.POST_DELETED)
    .entityType("POST")
    .entityId(postId.toString())
    .ipAddress(request.getRemoteAddr())  // ← "192.168.1.100"
    .userAgent(request.getHeader("User-Agent"))
    .build();

// Sau này phát hiện post bị xóa không đúng:
// → Check log → IP: 192.168.1.100 → Truy vết!
```

#### userAgent - Thông tin browser/device

**Mục đích:**
```java
// 1. ANALYTICS - User dùng gì?
User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)
→ User dùng iPhone

User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
→ User dùng Windows Desktop

// 2. BROWSER COMPATIBILITY
Phát hiện bug chỉ xảy ra trên Safari
→ Check activity_logs WHERE user_agent LIKE '%Safari%'

// 3. DETECT BOT/CRAWLER
User-Agent: curl/7.68.0
→ ❌ Đây là bot, không phải người thật

// 4. SECURITY - Phát hiện unusual patterns
User thường dùng Chrome, đột nhiên có request từ curl
→ ❌ Đáng ngờ!
```

**Ví dụ reports:**
```sql
-- Thống kê platforms
SELECT 
    CASE 
        WHEN user_agent LIKE '%iPhone%' THEN 'iOS'
        WHEN user_agent LIKE '%Android%' THEN 'Android'
        WHEN user_agent LIKE '%Windows%' THEN 'Windows'
        WHEN user_agent LIKE '%Mac%' THEN 'Mac'
        ELSE 'Other'
    END as platform,
    COUNT(*) as count
FROM activity_logs
WHERE action = 'USER_LOGIN'
GROUP BY platform;

-- Kết quả:
-- iOS: 4,523 users
-- Android: 3,891 users
-- Windows: 1,234 users
-- Mac: 567 users
```

**Real-world use case:**
```java
// Security Alert System
@Scheduled(cron = "0 */5 * * * *")  // Mỗi 5 phút
public void detectSuspiciousActivity() {
    List<ActivityLog> logs = activityLogRepository.findRecentLogins();
    
    for (User user : getUniqueUsers(logs)) {
        List<ActivityLog> userLogs = getLogsForUser(user.getId());
        
        // Check IP changes
        Set<String> ips = userLogs.stream()
            .map(ActivityLog::getIpAddress)
            .collect(Collectors.toSet());
            
        if (ips.size() > 3) {  // Login từ >3 IPs khác nhau trong 5 phút
            alertSecurityTeam("Suspicious activity: User " + user.getUsername() 
                            + " logged in from " + ips.size() + " different IPs");
        }
        
        // Check user agent changes
        Set<String> userAgents = userLogs.stream()
            .map(ActivityLog::getUserAgent)
            .collect(Collectors.toSet());
            
        if (userAgents.size() > 2) {  // Đổi device liên tục
            flagForReview(user.getId(), "Multiple device switches");
        }
    }
}
```

### 8.5 Use Cases

```java
// Lịch sử hoạt động của user
Page<ActivityLog> logs = activityLogRepository.findByUserId(userId, pageable);

// Audit trail cho entity cụ thể
List<ActivityLog> audit = activityLogRepository.findByEntity("POST", "uuid-123");
// → Ai đã tạo, sửa, xóa post này?

// Admin dashboard
Page<ActivityLog> recent = activityLogRepository.findRecentActivities(pageable);

// Cleanup logs cũ (monthly job)
int deleted = activityLogRepository.deleteOldLogs(
    LocalDateTime.now().minusMonths(6)
);
```

---

## 9. Hướng Dẫn Testing

### 9.1 Socket.IO Test Client

**File:** `socketio-test-client.html` (trong thư mục artifacts)

**Tính năng:**
- Trạng thái kết nối real-time
- Hiển thị notification trực tiếp
- Thống kê (tổng nhận, chưa đọc, thời gian session)
- Điều khiển kết nối

**Cách dùng:**
```
1. Mở socketio-test-client.html trong browser
2. Nhập Server URL: http://localhost:9092
3. Nhập User ID: {uuid-của-bạn}
4. Click Connect
5. Trigger notifications từ Postman
6. Xem cập nhật real-time!
```

### 9.2 API Testing với Postman

**Thiết lập collection:**
```json
{
  "auth": {
    "type": "bearer",
    "bearer": "{{jwt_token}}"
  },
  "tests": [
    "GET /notifications - Danh sách tất cả",
    "GET /notifications?unreadOnly=true - Chỉ chưa đọc",
    "GET /notifications/unread-count - Số badge",
    "PUT /notifications/{id}/read - Đánh dấu đọc",
    "PUT /notifications/mark-all-read - Đánh dấu tất cả",
    "DELETE /notifications/{id} - Xóa vĩnh viễn"
  ]
}
```

### 9.3 Kịch Bản Integration Test

**Kịch bản 1: User A thích post của User B**
```
1. POST /posts/{id}/likes từ User A
2. Verify notification được tạo trong DB
3. Verify Socket.IO broadcast đến User B
4. Verify unread count tăng lên
```

**Kịch bản 2: User C mention User D trong comment**
```
1. POST /comments với "@user_d..." từ User C
2. Verify NotificationType.COMMENT_MENTIONED được tạo
3. Verify User D nhận thông báo real-time
4. GET /notifications từ User D → thấy mention
```

---

## 10. Best Practices

### 10.1 Tối Ưu Hóa Performance

**1. Dùng Composite Indexes**
```sql
-- Query thường xuyên nhất: notifications chưa đọc
INDEX idx_recipient_unread (recipient_id, is_read, created_at);
```

**2. Phân Trang (Pagination)**
```java
Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
Page<Notification> notifications = service.getNotifications(userId, pageable);
```

**3. Lazy Loading**
```java
@ManyToOne(fetch = FetchType.LAZY)  // ← ĐỪNG eager load User
private User recipient;
```

### 10.2 Bảo Mật

**1. Validate Quyền Sở Hữu**
```java
// LUÔN LUÔN validate notification thuộc về current user
@Override
public void markAsRead(UUID notificationId, UUID userId) {
    Notification notification = notificationRepository
        .findByIdAndRecipient(notificationId, userId)  // ← Kiểm tra security
        .orElseThrow(() -> new AppException(ErrorCode.NOTIF_NOT_FOUND));
    
    // Giờ mới an toàn để update
}
```

**2. JWT Authentication trong Socket.IO**
```java
config.setAuthorizationListener(data -> {
    String token = data.getSingleUrlParam("token");
    jwtUtils.verifyToken(token, false);
    return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
});
```

### 10.3 Xử Lý Lỗi

**1. Socket.IO Failures Gracefully**
```java
try {
    socketService.sendNotification(userId, notification);
} catch (Exception e) {
    log.error("Lỗi broadcast notification", e);
    // ĐỪNG throw - notification đã lưu trong DB rồi
}
```

**2. Event Listener Failures**
```java
@TransactionalEventListener(phase = AFTER_COMMIT)
public void handleNotificationEvent(NotificationEvent event) {
    try {
        // Xử lý notification
    } catch (Exception e) {
        log.error("CRITICAL: Lỗi xử lý notification event", e);
        // Log nhưng đừng propagate - không thể rollback nữa
    }
}
```

### 10.4 Chiến Lược Cleanup

**1. Archive Notifications Cũ Đã Đọc**
```java
@Scheduled(cron = "0 0 2 * * *")  // 2h sáng hàng ngày
public void archiveOldNotifications() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(30);
    int archived = notificationRepository
        .archiveOldReadNotifications(threshold, LocalDateTime.now());
    log.info("Đã archive {} notifications cũ", archived);
}
```

**2. Xóa ActivityLogs Cũ**
```java
@Scheduled(cron = "0 0 3 1 * *")  // 3h sáng, ngày 1 hàng tháng
public void deleteOldLogs() {
    LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
    int deleted = activityLogRepository.deleteOldLogs(threshold);
    log.info("Đã xóa {} activity logs cũ", deleted);
}
```

---

## Tổng Kết: Files Đã Tạo

### Core Implementation (15 files)
1. ✅ `NotificationType.java` - 23 loại notification
2. ✅ `Notification.java` - Entity user-centric
3. ✅ `NotificationRepository.java` - 15+ queries tối ưu
4. ✅ `NotificationEvent.java` - Event DTO
5. ✅ `NotificationEventListener.java` - Async event handler
6. ✅ `NotificationResponse.java` - API DTO
7. ✅ `NotificationMapper.java` - MapStruct mapper
8. ✅ `INotificationService.java` - Service interface
9. ✅ `NotificationService.java` - Business logic
10. ✅ `NotificationController.java` - REST API (7 endpoints)
11. ✅ `ErrorCode.java` - NOTIF_NOT_FOUND error
12. ✅ `SocketService.java` - sendNotification() method
13. ✅ `Endpoint.java` - Notification endpoint constants

### ActivityLog Addition (3 files)
14. ✅ `ActivityAction.java` - 48 action types
15. ✅ `ActivityLog.java` - Audit trail entity với IP và User-Agent
16. ✅ `ActivityLogRepository.java` - Audit queries

### Testing & Documentation
17. ✅ `socketio-test-client.html` - Công cụ test real-time
18. ✅ `NOTIFICATION_SYSTEM_GUIDE.md` - Tài liệu này

---

## Phụ Lục: ERD Diagram

```mermaid
erDiagram
    users ||--o{ notifications : "recipient (1-nhiều)"
    users ||--o{ notifications : "actor (1-nhiều)"
    users ||--o{ activity_logs : "user (1-nhiều)"
    
    notifications {
        uuid id PK
        uuid recipient_id FK "AI nhận"
        uuid actor_id FK "AI gây ra"
        varchar type "NotificationType enum"
        varchar entity_type "Polymorphic POST COMMENT GROUP"
        varchar entity_id "Polymorphic UUID string"
        varchar title
        text message
        text metadata "JSON string"
        boolean is_read
        timestamp read_at
        boolean is_archived
        varchar priority
    }
    
    activity_logs {
        uuid id PK
        uuid user_id FK "AI thực hiện"
        varchar action "ActivityAction enum"
        varchar entity_type "Polymorphic"
        varchar entity_id "Polymorphic"
        text metadata "JSON string"
        varchar ip_address "IP của request - Security audit"
        text user_agent "Browser device info - Analytics"
    }
    
    posts {
        uuid id PK
        text content
    }
    
    comments {
        uuid id PK
        text content
    }
    
    groups {
        uuid id PK
        varchar name
    }
    
    notifications -.-> posts : "Logical reference (KHÔNG có FK)"
    notifications -.-> comments : "Logical reference (KHÔNG có FK)"
    notifications -.-> groups : "Logical reference (KHÔNG có FK)"
    activity_logs -.-> posts : "Logical reference (KHÔNG có FK)"
    activity_logs -.-> comments : "Logical reference (KHÔNG có FK)"
```

**Chú thích:**
- **Solid line (`||--o{`)**: Foreign key thực trong database
- **Dotted line (`-.->`)**: Logical reference, KHÔNG có constraint trong DB

---

**Phiên bản tài liệu:** 1.0  
**Cập nhật lần cuối:** 2025-12-17  
**Tác giả:** TripJoy Development Team

**Chúc code vui vẻ! 🚀**
