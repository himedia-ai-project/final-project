package com.gigigenie.domain.chat.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ConversationSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private Integer memberId;
    private List<ChatMessage> messages;
    private Integer productId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    public ConversationSession(Integer memberId, Integer productId) {
        this.sessionId = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.productId = productId;
        this.messages = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void addMessage(ChatMessage message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
        this.lastUpdatedAt = LocalDateTime.now();
    }
}