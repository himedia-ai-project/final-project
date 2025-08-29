package com.gigigenie.domain.prompt.repository;

import com.gigigenie.domain.prompt.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptRepository extends JpaRepository<PromptTemplate, String> {
    List<PromptTemplate> findByActiveTrue();
}