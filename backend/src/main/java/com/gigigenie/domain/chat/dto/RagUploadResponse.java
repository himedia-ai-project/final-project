package com.gigigenie.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RagUploadResponse {

    private String message;

    @JsonProperty("pdf_id")
    private String pdfId;

    @JsonProperty("store_path")
    private String storePath;
}
