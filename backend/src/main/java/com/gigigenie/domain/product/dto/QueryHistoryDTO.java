package com.gigigenie.domain.product.dto;

import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.product.entity.Product;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class QueryHistoryDTO {
    private Integer id;
    private Product product;
    private String queryText;
    private String responseText;
    private Long queryTime;
    private Member member;
    private String sessionId;
}
