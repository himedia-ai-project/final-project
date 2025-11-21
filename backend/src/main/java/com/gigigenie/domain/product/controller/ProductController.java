package com.gigigenie.domain.product.controller;

import com.gigigenie.domain.product.dto.ProductResponse;
import com.gigigenie.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api/product")
@RestController
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "제품 전체 조회")
    @GetMapping("/list")
    public ResponseEntity<List<ProductResponse>> list() {
        List<ProductResponse> list = productService.list();
        return ResponseEntity.ok(list);
    }

    @Operation(
        summary = "PDF 파일 업로드 및 임베딩 처리",
        description = "PDF 업로드 → 텍스트 추출 → 임베딩 → vectorDB 저장 및 파일을 S3에 저장"
    )
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(
        @Parameter(description = "업로드할 PDF 파일", required = true)
        @RequestParam MultipartFile file,
        @Parameter(description = "카테고리ID", required = true)
        @RequestParam Integer categoryId,
        @Parameter(description = "제품 이름", required = true)
        @RequestParam String name,
        @Parameter(description = "업로드할 제품 이미지 (jpg, jpeg, png, webp 형식만 허용)")
        @RequestParam(required = false) MultipartFile image,
        Authentication authentication
    ) {
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("PDF 파일만 지원합니다.");
        }

        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();
            String fileName = image.getOriginalFilename();

            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("이미지 파일만 지원합니다.");
            }

            boolean isValidExtension = false;
            if (fileName != null) {
                String extension = fileName.toLowerCase();
                isValidExtension = extension.endsWith(".jpg") ||
                    extension.endsWith(".jpeg") ||
                    extension.endsWith(".png") ||
                    extension.endsWith(".webp");
            }

            if (!isValidExtension) {
                return ResponseEntity.badRequest().body("jpg, jpeg, png, webp 형식의 이미지만 지원합니다.");
            }
        }

        String result = productService.processPdf(file, categoryId, name, image, authentication);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "제품 사용 설명서 다운로드(PDF)")
    @GetMapping("/{productId}/download")
    public ResponseEntity<Void> downloadPdf(@PathVariable Long productId,
        Authentication authentication) {
        String url = productService.downloadPdf(productId, authentication);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(url))
            .build();
    }
}
