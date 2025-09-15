package com.gigigenie.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigigenie.domain.chat.dto.ChatMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisChatService {

    private final StringRedisTemplate redis;
    private final ObjectMapper om = new ObjectMapper();
    //TTL수정
    private static final Duration TTL = Duration.ofMinutes(30);

    private String key(Integer memberId, Long productId) {
        return "chat:%d:%s".formatted(memberId, productId);
    }

    // 대화 불러오기
    public List<ChatMessage> load(Integer memberId, Long productId) {
        String redisKey = redis.opsForValue().get(key(memberId, productId));
        if (redisKey == null) {
            return new ArrayList<>();
        }
        try {
            ChatMessage[] history = om.readValue(redisKey, ChatMessage[].class);
            if (history == null) {
                return new ArrayList<>();
            }

            return new ArrayList<>(Arrays.asList(history));
        } catch (Exception e) {
            log.error("Redis load 실패 (memberId={}, productId={}): {}", memberId, productId,
                e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // 대화 저장
    public void save(Integer memberId, Long productId, List<ChatMessage> history) {
        try {
            String s = om.writeValueAsString(history);
            redis.opsForValue().set(key(memberId, productId), s, TTL);
        } catch (Exception e) {
            log.error("Redis save 실패 (memberId={}, productId={}): {}", memberId, productId,
                e.getMessage(), e);
        }
    }

    // 유저,에이전트 메시지 추가 = 전체 이력 반환 (FastAPI 호출용)
    public List<ChatMessage> addMessage(Integer memberId, Long productId, ChatMessage newMessage) {
        List<ChatMessage> history = load(memberId, productId); // 이전 대화 불러오기
        history.add(newMessage); // 새 메시지 추가
        save(memberId, productId, history); // Redis 갱신
        return history;
    }

    // 대화 삭제
    public void delete(Integer memberId, Long productId) {
        try {
            String redisKey = key(memberId, productId);
            Boolean result = redis.delete(redisKey);
            if (result) {
                log.info("delete 성공 (memberId={}, productId={})", memberId, productId);
            } else {
                log.warn("delete 대상 없음 (memberId={}, productId={})", memberId, productId);
            }
        } catch (Exception e) {
            log.error("Redis delete 실패 (memberId={}, productId={}): {}",
                memberId, productId, e.getMessage(), e);
        }
    }
}
