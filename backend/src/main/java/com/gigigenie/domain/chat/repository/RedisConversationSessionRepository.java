package com.gigigenie.domain.chat.repository;

import com.gigigenie.domain.chat.model.ConversationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisConversationSessionRepository implements ConversationSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "conversation:session:";
    private static final long SESSION_TTL_HOURS = 24; // 24시간 후 만료

    @Override
    public ConversationSession save(ConversationSession session) {
        String key = KEY_PREFIX + session.getSessionId();
        redisTemplate.opsForValue().set(key, session);
        redisTemplate.expire(key, SESSION_TTL_HOURS, TimeUnit.HOURS);
        return session;
    }

    @Override
    public Optional<ConversationSession> findById(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        Object session = redisTemplate.opsForValue().get(key);
        if (session != null) {
            // 세션 접근 시 TTL 갱신
            redisTemplate.expire(key, SESSION_TTL_HOURS, TimeUnit.HOURS);
            return Optional.of((ConversationSession) session);
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(String sessionId) {
        redisTemplate.delete(KEY_PREFIX + sessionId);
    }
}