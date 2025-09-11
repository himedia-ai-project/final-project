package com.gigigenie.domain.product.service;

import com.gigigenie.domain.product.dto.HistoryRequest;
import com.gigigenie.domain.product.dto.QueryHistoryDTO;
import com.gigigenie.domain.product.entity.QueryHistory;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface QueryHistoryService {

    void save(HistoryRequest request, Authentication authentication);

    List<QueryHistoryDTO> getHistories(Long productId, Authentication authentication);

    void deleteByMemberAndProduct(Long productId, Authentication authentication);

    List<Long> recent(Authentication authentication);

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
