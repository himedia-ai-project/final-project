package com.gigigenie.domain.chat.repository;

import com.gigigenie.domain.chat.model.ConversationSession;

import java.util.Optional;

public interface ConversationSessionRepository {
    ConversationSession save(ConversationSession session);
    Optional<ConversationSession> findById(String sessionId);
    void deleteById(String sessionId);
}