package com.gigigenie.domain.product.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ProductResponse {
    private Integer id;
    private String name;
    private String icon;
    private String url;
}
