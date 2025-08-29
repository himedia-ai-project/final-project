package com.gigigenie.domain.prompt.service;

import com.gigigenie.domain.prompt.entity.PromptTemplate;
import com.gigigenie.domain.prompt.repository.PromptRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PromptService {
    private final PromptRepository promptRepository;
    private final Map<String, PromptTemplate> promptCache = new ConcurrentHashMap<>();

    @Autowired
    public PromptService(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public String getPromptTemplate(String id) {
        PromptTemplate cached = promptCache.get(id);
        if (cached == null) {
            PromptTemplate template = promptRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("프롬프트 템플릿을 찾을 수 없습니다: " + id));
            promptCache.put(id, template);
            return template.getTemplate();
        }
        return cached.getTemplate();
    }

    @Scheduled(fixedRate = 3600000)
    public void refreshCache() {
        log.info("프롬프트 캐시 갱신 시작");
        List<PromptTemplate> activeTemplates = promptRepository.findByActiveTrue();

        promptCache.clear();

        for (PromptTemplate template : activeTemplates) {
            promptCache.put(template.getId(), template);
        }
        log.info("프롬프트 캐시 갱신 완료: {} 개 템플릿 로드됨", activeTemplates.size());
    }

    public List<PromptTemplate> getAllPromptTemplates() {
        return promptRepository.findAll();
    }

    public Optional<PromptTemplate> getPromptTemplateById(String id) {
        return promptRepository.findById(id);
    }

    public PromptTemplate savePromptTemplate(PromptTemplate template) {
        template.setLastUpdated(LocalDateTime.now());
        PromptTemplate saved = promptRepository.save(template);

        if (saved.isActive()) {
            promptCache.put(saved.getId(), saved);
        } else {
            promptCache.remove(saved.getId());
        }

        return saved;
    }

    public void deletePromptTemplate(String id) {
        promptRepository.deleteById(id);
        promptCache.remove(id);
    }
}