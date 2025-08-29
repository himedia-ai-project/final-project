package com.gigigenie.domain.chat.service;

import com.gigigenie.domain.chat.dto.ConversationRequest;
import com.gigigenie.domain.chat.dto.ConversationResponse;
import com.gigigenie.domain.chat.model.ChatMessage;
import com.gigigenie.domain.chat.model.ConversationSession;
import com.gigigenie.domain.chat.repository.ConversationSessionRepository;
import com.gigigenie.domain.product.dto.HistoryRequest;
import com.gigigenie.domain.product.dto.QueryHistoryDTO;
import com.gigigenie.domain.product.service.QueryHistoryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationSessionRepository sessionRepository;
    private final QueryHistoryService queryHistoryService;

    /**
     * 대화 처리: 세션 관리 + 질의응답
     */
    public Mono<ConversationResponse> processConversation(ConversationRequest requestDto) {
        Mono<ConversationSession> sessionMono;

        if (requestDto.isNewSession() || requestDto.getSessionId() == null) {
            ConversationSession newSession = new ConversationSession(requestDto.getMemberId(),
                requestDto.getProductId());

            if (requestDto.getMemberId() != null) {
                List<QueryHistoryDTO> histories = queryHistoryService.getHistories(
                    requestDto.getMemberId(), requestDto.getProductId());
                if (!histories.isEmpty()) {
                    for (QueryHistoryDTO history : histories) {
                        newSession.addMessage(new ChatMessage("user", history.getQueryText()));
                        newSession.addMessage(
                            new ChatMessage("assistant", history.getResponseText()));
                    }
                }
            }

            sessionMono = Mono.just(sessionRepository.save(newSession));
        } else {
            sessionMono = Mono.justOrEmpty(sessionRepository.findById(requestDto.getSessionId()))
                .switchIfEmpty(Mono.defer(() -> {
                    ConversationSession newSession = new ConversationSession(
                        requestDto.getMemberId(), requestDto.getProductId());
                    return Mono.just(sessionRepository.save(newSession));
                }));
        }

        return null;
    }

    /**
     * 세션 종료 및 대화 내역 저장 로그인 사용자의 대화만 히스토리에 저장
     */
    public void saveSessionToHistory(String sessionId) {
        Optional<ConversationSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            ConversationSession session = sessionOpt.get();

            if (session.getMemberId() != null) {
                queryHistoryService.deleteByMemberAndProduct(session.getMemberId(),
                    session.getProductId());

                List<Map<String, Object>> historyList = new ArrayList<>();

                for (int i = 0; i < session.getMessages().size() - 1; i += 2) {
                    if (i + 1 < session.getMessages().size()) {
                        ChatMessage userMsg = session.getMessages().get(i);
                        ChatMessage assistantMsg = session.getMessages().get(i + 1);

                        if ("user".equals(userMsg.getRole()) && "assistant".equals(
                            assistantMsg.getRole())) {
                            if (!"initial_connection".equals(userMsg.getContent())) {
                                Map<String, Object> entry = new HashMap<>();
                                entry.put("queryText", userMsg.getContent());
                                entry.put("responseText", assistantMsg.getContent());
                                entry.put("queryTime", System.currentTimeMillis());
                                historyList.add(entry);
                            }
                        }
                    }
                }

                HistoryRequest request = new HistoryRequest();
                request.setMemberId(session.getMemberId());
                request.setProductId(session.getProductId());
                request.setHistory(historyList);

                queryHistoryService.save(request);
            }
        }
    }

    /**
     * 히스토리 저장하고 세션 삭제
     */
    public void deleteSession(String sessionId) {
        saveSessionToHistory(sessionId);
        sessionRepository.deleteById(sessionId);
    }

    /**
     * 히스토리 저장하지 않고 세션만 삭제
     */
    public void deleteSessionWithoutSaving(String sessionId) {
        Optional<ConversationSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            ConversationSession session = sessionOpt.get();
            queryHistoryService.deleteByMemberAndProduct(session.getMemberId(),
                session.getProductId());
        }
        sessionRepository.deleteById(sessionId);
    }
}