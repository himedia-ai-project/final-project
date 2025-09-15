package com.gigigenie.domain.history.controller;

import com.gigigenie.domain.history.dto.QueryHistoryDTO;
import com.gigigenie.domain.history.service.QueryHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/history")
@RestController
public class QueryHistoryController {

    private final QueryHistoryService historyService;

    @Operation(summary = "이전 대화내용 불러오기")
    @GetMapping
    public ResponseEntity<List<QueryHistoryDTO>> getHistories(
        @RequestParam Long productId, Authentication authentication) {
        List<QueryHistoryDTO> dtoList = historyService.getHistories(productId, authentication);
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "최근 채팅항목 조회")
    @GetMapping("/recent")
    public ResponseEntity<List<Long>> recent(Authentication authentication) {
        List<Long> ids = historyService.recent(authentication);
        log.info("최근 항목 조회 결과: {}", ids);
        return ResponseEntity.ok(ids);
    }
}
