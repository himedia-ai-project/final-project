package com.gigigenie.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@Data
public class WebClientConfig {
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1}")
    private String openaiApiUrl;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        log.info("Gemini API URL: {}", geminiApiUrl);
        return builder
                .baseUrl(geminiApiUrl)
                .build();
    }

    @Bean
    public WebClient openaiWebClient(WebClient.Builder builder) {
        log.info("OpenAI API URL: {}", openaiApiUrl);
        return builder
                .baseUrl(openaiApiUrl)
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}