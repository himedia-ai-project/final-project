package com.gigigenie.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private String message;

    @JsonProperty("pdf_id")
    private Long pdfId;

    @JsonProperty("store_path")
    private String storePath;
}
