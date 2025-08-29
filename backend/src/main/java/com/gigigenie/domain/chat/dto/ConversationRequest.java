package com.gigigenie.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {
    private String query;

    @JsonProperty("productId")
    private Integer productId;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("memberId")
    private Integer memberId;

    @JsonProperty("top_k")
    private int top_k = 3;

    private boolean newSession = false;
}
