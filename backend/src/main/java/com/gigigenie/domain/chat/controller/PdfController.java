package com.gigigenie.domain.chat.controller;

import com.gigigenie.domain.chat.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;

    @Operation(
            summary = "PDF 파일 업로드 및 임베딩 처리",
            description = "PDF 업로드 → 텍스트 추출 → 임베딩 → vectorDB 저장 및 파일을 S3에 저장"
    )
    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(
            @Parameter(description = "업로드할 PDF 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "카테고리ID", required = true)
            @RequestParam("categoryId") Integer categoryId,
            @Parameter(description = "청크 크기 (기본값: 210)")
            @RequestParam(defaultValue = "210") int chunkSize,
            @Parameter(description = "청크 오버랩 (기본값: 50)")
            @RequestParam(defaultValue = "50") int chunkOverlap,
            @Parameter(description = "제품 이름", required = true)
            @RequestParam("name") String name,
            @Parameter(description = "업로드할 제품 이미지 (jpg, jpeg, png, webp 형식만 허용)")
            @RequestParam(value = "image", required = false) MultipartFile image,
            @Parameter(description = "업로드할 제품 이미지 (jpg, jpeg, png, webp 형식만 허용)", required = true)
            @RequestParam(value = "memberId") Integer memberId
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

        var result = pdfService.processPdf(file, categoryId, chunkSize, chunkOverlap, name, image, memberId);
        return ResponseEntity.ok(result);
    }
}
