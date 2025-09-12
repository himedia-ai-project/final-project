package com.gigigenie.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String role; // "user" | "assistant"
    private String messages; // 메시지 내용
}