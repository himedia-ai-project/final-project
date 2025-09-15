package com.gigigenie.domain.chat.service;

import com.gigigenie.domain.chat.dto.ChatMessage;
import com.gigigenie.domain.chat.dto.ChatRequest;
import com.gigigenie.domain.chat.dto.FastApiRequest;
import com.gigigenie.domain.chat.enums.ChatRole;
import com.gigigenie.domain.history.entity.QueryHistory;
import com.gigigenie.domain.history.repository.QueryHistoryRepository;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.ProductRepository;
import java.util.ArrayList;
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
    private final QueryHistoryRepository queryHistoryRepository;
    private final RedisChatService redisChatService;

    // 대화
    public List<ChatMessage> processChat(ChatRequest request, Authentication authentication) {
        Member member = findMember(authentication);
        Product product = findProduct(request.getProductId());

        //변수
        Integer memberId = member.getMemberId();
        Long productId = product.getId();
        String question = request.getQuestion();

        //유저 메세지
        ChatMessage chatMessage = new ChatMessage("user", question);
        //유저 메세지 추가 + 이전 대화내용 불러오기
        List<ChatMessage> history = redisChatService.addMessage(memberId, productId, chatMessage);
        // request 요청
        FastApiRequest fastApiRequest = new FastApiRequest(productId, question, history);

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

    // 대화 종료
    public void endChat(Long productId, Authentication authentication, boolean skipSave) {
        // 1) 식별자 조회
        Member member = findMember(authentication);
        Product product = findProduct(productId);

        // 2) 대화 내역 불러오기
        List<ChatMessage> history = redisChatService.load(member.getMemberId(), product.getId());

        // 3) 기존 DB 저장 기록 삭제
        queryHistoryRepository.deleteByMemberAndProduct(member, product);

        // 4) 저장 옵션이 켜져 있고 대화 내역이 있으면 db에 저장
        if (!skipSave && !history.isEmpty()) {
            List<QueryHistory> histories = new ArrayList<>(history.size());

            for (ChatMessage message : history) {
                QueryHistory qh = QueryHistory.builder()
                    .product(product)
                    .member(member)
                    .role(convertRole(message.getRole()))
                    .messages(message.getMessages())
                    .build();
                histories.add(qh);
            }
            queryHistoryRepository.saveAll(histories);
        }

        redisChatService.delete(member.getMemberId(), product.getId());
    }

    // 문자열 role("user", "bot")을 ChatRole Enum 값으로 변환
    private ChatRole convertRole(String role) {
        if ("user".equals(role)) {
            return ChatRole.user;
        }

        if ("bot".equals(role)) {
            return ChatRole.bot;
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }

    private Member findMember(Authentication authentication) {
        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
        return memberRepository.findById(memberDTO.getId())
            .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}