package com.gigigenie.domain.chat.controller;

import com.gigigenie.domain.chat.dto.ConversationRequest;
import com.gigigenie.domain.chat.dto.ConversationResponse;
import com.gigigenie.domain.chat.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "대화형 제품 설명서 질의응답")
    @PostMapping
    public Mono<ConversationResponse> conversation(@RequestBody ConversationRequest requestDto) {
        log.info("대화 처리 중 - 세션: {}, 제품: {}, 쿼리: {}",
                requestDto.getSessionId(), requestDto.getProductId(), requestDto.getQuery());
        return conversationService.processConversation(requestDto);
    }

    @Operation(
            summary = "대화 세션 종료",
            description = "skipSave=true 대화내역 저장 X, skipSave=false 대화내역 저장 O"
    )
    @DeleteMapping("/{sessionId}")
    public Mono<Void> endConversation(
            @PathVariable String sessionId,
            @RequestParam(required = false, defaultValue = "false") boolean skipSave
    ) {
        log.info("대화 세션 종료: {}, 저장 건너뛰기: {}", sessionId, skipSave);
        if (skipSave) {
            conversationService.deleteSessionWithoutSaving(sessionId);
        } else {
            conversationService.deleteSession(sessionId);
        }
        return Mono.empty();
    }
}