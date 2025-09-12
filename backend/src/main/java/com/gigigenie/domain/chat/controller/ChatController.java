package com.gigigenie.domain.chat.controller;

import com.gigigenie.domain.chat.dto.ChatRequest;
import com.gigigenie.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "대화형 제품 설명서 질의응답")
    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequest request,
        Authentication authentication) {
        String answer = chatService.processChat(request, authentication);
        return ResponseEntity.ok(answer);
    }
}