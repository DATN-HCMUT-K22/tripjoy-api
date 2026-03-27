package com.tripjoy.api.dto.request;

import java.util.UUID;

import lombok.Data;

@Data
public class SocketMessageRequest {
    // Nội dung tin nhắn
    private String messageContent;

    // Loại: TEXT, IMAGE, VIDEO...
    private String messageType;

    // ID người gửi (Client gửi lên hoặc lấy từ token)
    private UUID senderId;

    // Đích đến: Chỉ 1 trong 2 trường dưới có giá trị
    private UUID groupId; // Nếu chat nhóm
    private UUID receiverId; // Nếu chat 1-1
}
