package com.gigigenie.domain.history.service;

import com.gigigenie.domain.history.dto.QueryHistoryDTO;
import com.gigigenie.domain.history.entity.QueryHistory;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface QueryHistoryService {

    List<QueryHistoryDTO> getHistories(Long productId, Authentication authentication);

    List<Long> recent(Authentication authentication);

    default QueryHistoryDTO entityToDTO(QueryHistory queryHistory) {
        return QueryHistoryDTO.builder()
            .id(queryHistory.getId())
            .product(queryHistory.getProduct())
            .member(queryHistory.getMember())
            .role(queryHistory.getRole().toString())
            .messages(queryHistory.getMessages())
            .build();
    }
}
