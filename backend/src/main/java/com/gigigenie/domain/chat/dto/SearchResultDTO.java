package com.gigigenie.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private String content;
    private Map<String, Object> metadata;
    private Double score;
}