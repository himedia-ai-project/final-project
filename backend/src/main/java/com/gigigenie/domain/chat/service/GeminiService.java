package com.gigigenie.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigigenie.domain.chat.dto.SearchResultDTO;
import com.gigigenie.domain.chat.model.ChatMessage;
import com.gigigenie.domain.prompt.service.PromptService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;
    private final PromptService promptService;
    private final String MODEL = "gemini-2.0-flash";

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public Mono<String> generateAnswerWithHistory(String query, List<SearchResultDTO> retrievedDocs, List<ChatMessage> messageHistory) {
        String context = retrievedDocs.stream()
                .map(SearchResultDTO::getContent)
                .collect(Collectors.joining("\n\n"));

        // 대화 이력 포맷팅
        String conversationHistory = "";
        if (messageHistory != null && !messageHistory.isEmpty()) {
            conversationHistory = messageHistory.stream()
                    .map(msg -> msg.getRole() + ": " + msg.getContent())
                    .collect(Collectors.joining("\n\n"));
        }

        String prompt;
        if (!conversationHistory.isEmpty()) {
            String promptTemplate = promptService.getPromptTemplate("gemini_answer_with_history");
            prompt = String.format(promptTemplate, conversationHistory, query, context);
        } else {
            String promptTemplate = promptService.getPromptTemplate("gemini_answer");
            prompt = String.format(promptTemplate, query, context);
        }

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();

        parts.put("text", prompt);
        contents.put("parts", List.of(parts));
        requestBody.put("contents", List.of(contents));

        String uri = UriComponentsBuilder.fromPath("/models/{model}:generateContent")
                .queryParam("key", geminiApiKey)
                .buildAndExpand(MODEL)
                .toUriString();

        return geminiWebClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        GeminiResponse response = objectMapper.readValue(responseBody, GeminiResponse.class);
                        return Mono.just(response.getCandidates().get(0).getContent().getParts().get(0).getText());
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse Gemini response", e);
                        return Mono.error(e);
                    }
                });
    }

    @Data
    public static class GeminiResponse {
        private List<Candidate> candidates;

        @Data
        public static class Candidate {
            private Content content;
        }

        @Data
        public static class Content {
            private List<Part> parts;
        }

        @Data
        public static class Part {
            private String text;
        }
    }
}