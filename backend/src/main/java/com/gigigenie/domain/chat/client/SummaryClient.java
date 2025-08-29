package com.gigigenie.domain.chat.client;

import com.gigigenie.domain.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SummaryClient {

    private final WebClient webClient;
    private final String CHAT_API_URL = "https://api.openai.com/v1/chat/completions";
    private final PromptService promptService;

    public SummaryClient(
            @Value("${openai.api.key}") String apiKey,
            PromptService promptService) {
        this.webClient = WebClient.builder()
                .baseUrl(CHAT_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.promptService = promptService;
    }

    public Mono<String> summarizeAsync(String text) {
        String limitedText = text.length() > 3000 ? text.substring(0, 3000) : text;
        String promptTemplate = promptService.getPromptTemplate("summary");
        String prompt = String.format(promptTemplate, limitedText);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.3);

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response != null && response.containsKey("choices")) {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        if (!choices.isEmpty()) {
                            Map<String, Object> choice = choices.get(0);
                            Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                            return (String) messageObj.get("content");
                        }
                    }
                    return "요약 생성 실패";
                })
                .onErrorReturn("요약 생성 중 오류 발생");
    }

    public String summarize(String text) {
        return summarizeAsync(text).block();
    }
}