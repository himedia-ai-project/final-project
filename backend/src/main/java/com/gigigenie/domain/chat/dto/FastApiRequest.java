package com.gigigenie.domain.chat.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastApiRequest {

    private Long productId;
    private String question;
    private List<ChatMessage> messages;
}
