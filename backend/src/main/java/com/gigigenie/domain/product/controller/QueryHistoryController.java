package com.gigigenie.domain.product.controller;

import com.gigigenie.domain.product.dto.HistoryRequest;
import com.gigigenie.domain.product.dto.QueryHistoryDTO;
import com.gigigenie.domain.product.service.QueryHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/history")
@RestController
public class QueryHistoryController {
    private final QueryHistoryService historyService;

    @Operation(summary = "이전 대화내용 불러오기")
    @GetMapping
    public ResponseEntity<List<QueryHistoryDTO>> getHistories(@RequestParam("memberId") Integer memberId, @RequestParam("productId") Integer productId) {
        List<QueryHistoryDTO> dtoList = historyService.getHistories(memberId, productId);
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "최근 채팅항목 조회")
    @GetMapping("/recent")
    public ResponseEntity<List<Integer>> recent(
            @Parameter(description = "회원ID(FK)", required = true)
            @RequestParam Integer memberId) {
        List<Integer> ids = historyService.recent(memberId);
        log.info("최근 항목 조회 결과: {}", ids);
        return ResponseEntity.ok(ids);
    }

    @Operation(summary = "대화내용 저장")
    @PostMapping("/save")
    public void save(@RequestBody HistoryRequest request) {
        historyService.save(request);
    }

}
