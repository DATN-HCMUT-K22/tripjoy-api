package com.tripjoy.api.dto.ai;

import lombok.*;

/**
 * DTO wrap response từ AI Service endpoint POST /chat.
 *
 * <p>AI service trả về plain String (không phải JSON object).
 * WebClient sẽ deserialize raw string vào field {@code message}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponseDto {

    /** Câu trả lời của AI chatbot */
    private String message;
}
