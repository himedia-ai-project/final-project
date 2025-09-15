package com.gigigenie.domain.chat.controller;

import com.gigigenie.domain.chat.dto.ChatMessage;
import com.gigigenie.domain.chat.dto.ChatRequest;
import com.gigigenie.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "대화형 제품 설명서 질의응답")
    @PostMapping
    public ResponseEntity<List<ChatMessage>> chat(@RequestBody ChatRequest request,
        Authentication authentication) {
        List<ChatMessage> answer = chatService.processChat(request, authentication);
        return ResponseEntity.ok(answer);
    }

    @Operation(
        summary = "대화 종료",
        description = "skipSave=true 대화내역 저장 X, skipSave=false 대화내역 저장 O"
    )
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> endChat(
        @PathVariable Long productId, Authentication authentication,
        @RequestParam(defaultValue = "false") boolean skipSave
    ) {
        chatService.endChat(productId, authentication, skipSave);
        return ResponseEntity.ok("대화가 종료되었습니다.");
    }
}