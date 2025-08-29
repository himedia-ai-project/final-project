package com.gigigenie.domain.prompt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    @Id
    private String id;  // 예: "summary", "gemini_answer" 등

    @Column(columnDefinition = "TEXT")
    private String template;

    private LocalDateTime lastUpdated;

    private String description;

    private boolean active;
}