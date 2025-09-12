package com.gigigenie.domain.chat.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigigenie.domain.chat.dto.ChatMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatStore {

    private final StringRedisTemplate redis;
    private static final ObjectMapper om = new ObjectMapper();
    private static final Duration TTL = Duration.ofDays(7);

    private String key(Integer memberId, Long productId) {
        return "chat:%d:%s".formatted(memberId, productId);
    }

    public List<ChatMessage> load(Integer memberId, Long productId) {
        String v = redis.opsForValue().get(key(memberId, productId));
        if (v == null) {
            return new ArrayList<>();
        }
        try {
            return Arrays.asList(om.readValue(v, ChatMessage[].class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void save(Integer memberId, Long productId, List<ChatMessage> history) {
        try {
            String s = om.writeValueAsString(history);
            redis.opsForValue().set(key(memberId, productId), s, TTL);
        } catch (Exception ignore) {
        }
    }
}
