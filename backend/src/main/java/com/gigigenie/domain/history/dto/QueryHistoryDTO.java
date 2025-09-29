package com.gigigenie.domain.history.dto;

import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class QueryHistoryDTO {

    private Integer id;
    private Product product;
    private Member member;
    private String role;
    private String messages;
}
