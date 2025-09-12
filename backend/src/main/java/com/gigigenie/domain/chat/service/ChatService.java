package com.gigigenie.domain.chat.service;

import com.gigigenie.domain.chat.dto.ChatMessage;
import com.gigigenie.domain.chat.dto.ChatRequest;
import com.gigigenie.domain.chat.repository.ChatStore;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.ProductRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final WebClient ragWebClient;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ChatStore chatStore;

    // 대화
    public String processChat(ChatRequest request, Authentication authentication) {
        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
        Member member = memberRepository.findById(memberDTO.getId())
            .orElseThrow(() -> new RuntimeException("Member not found"));
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Redis에서 히스토리 로드
        List<ChatMessage> stored = new ArrayList<>(
            chatStore.load(member.getMemberId(), product.getId()));
        final boolean hasSession = !stored.isEmpty();

        Map<String, Object> body = new HashMap<>();
        body.put("pdf_id", String.valueOf(product.getId()));
        body.put("question", request.getQuestion());
        if (hasSession) {
            body.put("history", stored);
        }

        // FastAPI /chat 호출
        String answer = ragWebClient.post()
            .uri("/chat")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(res -> log.info("응답: {}", res))
            .switchIfEmpty(Mono.error(new IllegalStateException("응답이 비어있음")))
            .block();

        if (!hasSession) { // 세션 X
            List<ChatMessage> newHistory = new ArrayList<>();
            newHistory.add(new ChatMessage("user", request.getQuestion()));
            newHistory.add(new ChatMessage("assistant", answer));
            chatStore.save(member.getMemberId(), product.getId(), newHistory);
        } else { // 세션 O
            List<ChatMessage> updated = new ArrayList<>(stored);
            updated.add(new ChatMessage("user", request.getQuestion()));
            updated.add(new ChatMessage("assistant", answer));
            chatStore.save(member.getMemberId(), product.getId(), updated);
        }

        return answer;
    }

}