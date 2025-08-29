package com.gigigenie.domain.product.service;

import com.gigigenie.domain.product.dto.HistoryRequest;
import com.gigigenie.domain.product.dto.QueryHistoryDTO;
import com.gigigenie.domain.product.entity.QueryHistory;

import java.util.List;

public interface QueryHistoryService {
    void save(HistoryRequest request);

    List<QueryHistoryDTO> getHistories(Integer memberId, Integer productId);

    void deleteByMemberAndProduct(Integer memberId, Integer productId);

    List<Integer> recent(Integer memberId);

    default QueryHistoryDTO entityToDTO(QueryHistory queryHistory) {
        return QueryHistoryDTO.builder()
                .id(queryHistory.getId())
                .product(queryHistory.getProduct())
                .queryText(queryHistory.getQueryText())
                .responseText(queryHistory.getResponseText())
                .queryTime(queryHistory.getQueryTime())
                .member(queryHistory.getMember())
                .build();
    }

}
