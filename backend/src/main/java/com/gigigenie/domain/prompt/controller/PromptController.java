package com.gigigenie.domain.prompt.controller;

import com.gigigenie.domain.prompt.entity.PromptTemplate;
import com.gigigenie.domain.prompt.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {
    private final PromptService promptService;

    @Operation(summary = "전체 프롬프트 조회")
    @GetMapping
    public List<PromptTemplate> getAllPrompts() {
        return promptService.getAllPromptTemplates();
    }

    @Operation(summary = "프롬프트 단일 조회")
    @GetMapping("/{id}")
    public ResponseEntity<PromptTemplate> getPrompt(@PathVariable String id) {
        return promptService.getPromptTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "프롬프트 생성")
    @PostMapping
    public PromptTemplate createPrompt(@RequestBody PromptTemplate prompt) {
        return promptService.savePromptTemplate(prompt);
    }

    @Operation(summary = "프롬프트 수정")
    @PutMapping("/{id}")
    public ResponseEntity<PromptTemplate> updatePrompt(
            @Parameter(description = "프롬프트 템플릿 ID (예: summary, gemini_answer)", example = "summary")
            @PathVariable String id,
            @RequestBody PromptTemplate prompt) {
        if (promptService.getPromptTemplateById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        prompt.setId(id);
        return ResponseEntity.ok(promptService.savePromptTemplate(prompt));
    }

    @Operation(summary = "프롬프트 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(
            @Parameter(description = "프롬프트 템플릿 ID (예: summary, gemini_answer)", example = "summary")
            @PathVariable String id) {
        if (!promptService.getPromptTemplateById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        promptService.deletePromptTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshCache() {
        promptService.refreshCache();
        return ResponseEntity.ok().build();
    }
}