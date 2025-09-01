package com.gigigenie.domain.chat.service;

import com.gigigenie.domain.chat.dto.RagUploadResponse;
import com.gigigenie.domain.notification.service.NotificationService;
import com.gigigenie.domain.product.entity.Category;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.CategoryRepository;
import com.gigigenie.domain.product.repository.ProductRepository;
import com.gigigenie.util.files.CustomFileUtil;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PdfService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CustomFileUtil fileUtil;
    private final NotificationService notificationService;
    private final WebClient ragWebClient;

    @Transactional
    public Map<String, Object> processPdf(MultipartFile file, Integer categoryId, String name,
        MultipartFile image, Integer memberId) {

        // 중복 체크
        Optional<Product> existingProduct = productRepository.findByModelName(name);
        if (existingProduct.isPresent()) {
            return Map.of(
                "status", "exists",
                "message", "이미 등록된 모델입니다.",
                "model_name", existingProduct.get().getModelName()
            );
        }

        // S3 업로드 (PDF)
        String fileKey = fileUtil.uploadS3File(file);
        log.info("PDF 파일 S3 업로드 완료: {}", fileKey);

        String imageUrl = null;

        // S3 업로드 (이미지 선택)
        if (image != null && !image.isEmpty()) {
            String imageKey = fileUtil.uploadS3File(image);
            imageUrl = fileUtil.getS3Url(imageKey);
            log.info("이미지 S3 업로드 완료: {}, URL: {}", imageKey, imageUrl);
        }

        // FastAPI /upload 호출 (파일 그대로 전달)
        RagUploadResponse ragResp = null;
        try {
            ragResp = callRagUpload(file);
            log.info("RAG 인덱싱 성공: pdf_id={}, store_path={}",
                ragResp.getPdfId(), ragResp.getStorePath());
        } catch (Exception e) {
            log.error("RAG 인덱싱 실패: {}", e.getMessage(), e);
        }

        // 카테고리 조회 & Product 저장
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = Product.builder()
            .pdfId(Objects.requireNonNull(ragResp).getPdfId())
            .category(category)
            .modelName(name)
            .modelImage(imageUrl)
            .createdAt(LocalDateTime.now())
            .build();

        productRepository.save(product);

        // 알림
        if (memberId != null) {
            notificationService.addNotification(
                memberId,
                name + " 제품이 성공적으로 등록되었습니다.",
                "제품 등록 완료"
            );
        }

        return Map.of(
            "status", "success",
            "product_id", product.getId(),
            "message", ragResp.getMessage()
        );
    }

    private RagUploadResponse callRagUpload(MultipartFile file) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", file.getResource());

        return ragWebClient.post()
            .uri("/upload")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body.build()))
            .retrieve()
            .bodyToMono(RagUploadResponse.class)
            .block();
    }
}
