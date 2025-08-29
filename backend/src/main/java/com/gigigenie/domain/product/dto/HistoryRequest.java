package com.gigigenie.domain.product.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class HistoryRequest {
    private Integer memberId;
    private Integer productId;
    private List<Map<String, Object>> history;

}
