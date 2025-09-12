package com.gigigenie.domain.chat.service;

import com.gigigenie.domain.chat.dto.ChatMessage;
import com.gigigenie.domain.chat.dto.ChatRequest;
import com.gigigenie.domain.chat.dto.FastApiRequest;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.ProductRepository;
import java.util.List;
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
    private final RedisChatService redisChatService;

    // 대화
    public List<ChatMessage> processChat(ChatRequest request, Authentication authentication) {
        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
        Member member = memberRepository.findById(memberDTO.getId())
            .orElseThrow(() -> new RuntimeException("Member not found"));
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        //변수
        Integer memberId = member.getMemberId();
        Long productId = product.getId();
        String question = request.getQuestion();

        //유저 메세지
        ChatMessage chatMessage = new ChatMessage("user",question);
        //유저 메세지 추가 + 이전 대화내용 불러오기
        List<ChatMessage> history = redisChatService.addMessage(memberId, productId, chatMessage);
        // request 요청
        FastApiRequest fastApiRequest = new FastApiRequest(productId,question,history);

        // FastAPI /chat 호출  타입수정 필요해 옵입니다 response 수정
        // webClient 예외처리부분도 추가해주세요
        String botResponse = ragWebClient.post()
            .uri("/chat")
            .bodyValue(fastApiRequest)
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(res -> log.info("응답: {}", res))
            .switchIfEmpty(Mono.error(new IllegalStateException("응답이 비어있음")))
            .block();

        ChatMessage botMessage = new ChatMessage("bot", botResponse);
        history.add(botMessage);
        redisChatService.save(memberId, productId, history);


        return history;
    }

}