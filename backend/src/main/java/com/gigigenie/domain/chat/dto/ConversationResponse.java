package com.gigigenie.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String query;
    private String answer;
    private List<SearchResultDTO> documents;

    @JsonProperty("sessionId")
    private String sessionId;
}