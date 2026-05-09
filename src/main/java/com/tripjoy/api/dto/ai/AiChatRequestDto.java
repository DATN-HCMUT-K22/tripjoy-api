package com.tripjoy.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO gửi sang AI Service cho endpoint POST /chat.
 * Field {@code itinerary} là optional — nếu có, AI sẽ dùng context lịch trình.
 *
 * <p>Python model tương ứng: {@code ChatRequest}</p>
 * <pre>
 * @dataclass
 * class ChatRequest:
 *     conversation_id: str   ← String (không phải UUID)
 *     message: str
 *     itinerary: Optional[FinalItinerary] = None
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestDto {

    /** Tin nhắn của user */
    private String message;

    /**
     * ID của Conversation — AI dùng để tra lịch sử chat qua tool get_chat_message.
     * Kiểu String (Python nhận str, không phải UUID object).
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * Lịch trình hiện tại (optional) — context cho AI khi chat về chuyến đi.
     */
    private AiFinalItineraryDto itinerary;
}
