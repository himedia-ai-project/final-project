package com.gigigenie.domain.chat.service;

import com.gigigenie.domain.chat.client.SummaryClient;
import com.gigigenie.domain.chat.util.PdfTextExtractor;
import com.gigigenie.domain.notification.service.NotificationService;
import com.gigigenie.domain.product.entity.Category;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.CategoryRepository;
import com.gigigenie.domain.product.repository.ProductRepository;
import com.gigigenie.util.files.CustomFileUtil;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PdfService {

    private final PdfTextExtractor extractor;
    private final SummaryClient summaryClient;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CustomFileUtil fileUtil;
    private final NotificationService notificationService;

    @Transactional
    public Map<String, Object> processPdf(MultipartFile file, Integer categoryId, int chunkSize,
        int chunkOverlap, String name, MultipartFile image, Integer memberId) {
        Optional<Product> existingProduct = productRepository.findByModelName(name);
        if (existingProduct.isPresent()) {
            return Map.of(
                "status", "exists",
                "message", "이미 등록된 모델입니다.",
                "model_name", existingProduct.get().getModelName()
            );
        }

        String fileKey = fileUtil.uploadS3File(file);
        log.info("PDF 파일 S3 업로드 완료: {}", fileKey);

        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            String imageKey = fileUtil.uploadS3File(image);
            imageUrl = fileUtil.getS3Url(imageKey);
            log.info("이미지 S3 업로드 완료: {}, URL: {}", imageKey, imageUrl);
        }

        String text = extractor.extract(file);
        String summary = summaryClient.summarize(text);
        log.info("생성된 요약: {}", summary);

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = Product.builder()
            .category(category)
            .modelName(name)
            .modelImage(imageUrl)
            .createdAt(LocalDateTime.now())
            .featureSummary(summary)
            .build();

        productRepository.save(product);

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
            "summary", summary
        );
    }

}
