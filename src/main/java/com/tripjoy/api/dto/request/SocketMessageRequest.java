package com.tripjoy.api.dto.request;

import lombok.Data;

@Data
public class SocketMessageRequest {
    // Nội dung tin nhắn
    private String messageContent;

    // Loại: TEXT, IMAGE, VIDEO...
    private String messageType;

    // ID người gửi (Client gửi lên hoặc lấy từ token)
    private String senderId;

    // Đích đến: Chỉ 1 trong 2 trường dưới có giá trị
    private String groupId;     // Nếu chat nhóm
    private String receiverId;  // Nếu chat 1-1
}