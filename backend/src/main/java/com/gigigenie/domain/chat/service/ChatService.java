package com.gigigenie.domain.chat.service;

import com.gigigenie.domain.chat.dto.ChatRequest;
import com.gigigenie.domain.chat.dto.ChatResponse;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.product.service.QueryHistoryService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final WebClient ragWebClient;
    private final QueryHistoryService queryHistoryService;
    private final MemberRepository memberRepository;

    // 대화
    public String processChat(ChatRequest request, Authentication authentication) {
        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
        Member member = memberRepository.findById(memberDTO.getId())
            .orElseThrow(() -> new RuntimeException("Member not found"));

        ChatResponse response = ragWebClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/chat")
                .queryParam("user_id", String.valueOf(member.getMemberId()))
                .queryParam("pdf_id", request.getProductId())
                .queryParam("question", request.getQuestion())
                .build())
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .block();

        log.info("Chat response: {}", response);

        return Objects.requireNonNull(response).getAnswer();
    }

}