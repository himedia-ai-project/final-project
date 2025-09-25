package com.gigigenie.domain.product.service;

import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.notification.service.NotificationService;
import com.gigigenie.domain.product.dto.ProductResponse;
import com.gigigenie.domain.product.dto.UploadRequest;
import com.gigigenie.domain.product.dto.UploadResponse;
import com.gigigenie.domain.product.entity.Category;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.CategoryRepository;
import com.gigigenie.domain.product.repository.ProductRepository;
import com.gigigenie.util.files.CustomFileUtil;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final CustomFileUtil fileUtil;
    private final NotificationService notificationService;
    private final WebClient ragWebClient;

    @Override
    public List<ProductResponse> list() {
        List<Product> products = productRepository.findAllWithCategory();
        return products.stream().map(product -> (
            ProductResponse.builder()
                .productId(product.getId())
                .modelName(product.getModelName())
                .iconUrl(product.getModelImage() != null ? product.getModelImage()
                    : product.getCategory().getCategoryIcon())
                .build())).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String processPdf(MultipartFile file, Integer categoryId, String name,
        MultipartFile image, Authentication authentication) {
        findMember(authentication);

        // 중복 체크
        Optional<Product> existingProduct = productRepository.findByModelName(name);
        if (existingProduct.isPresent()) {
            return "이미 등록된 모델입니다. (모델명: " + existingProduct.get().getModelName() + ")";
        }

        // S3 업로드 (PDF)
        String fileKey = fileUtil.uploadS3File(file);
        String fileUrl = fileUtil.getS3Url(fileKey);
        log.info("PDF 파일 S3 업로드 완료: key={}, url={}", fileKey, fileUrl);

        String imageUrl = null;

        // S3 업로드 (이미지 선택)
        if (image != null && !image.isEmpty()) {
            String imageKey = fileUtil.uploadS3File(image);
            imageUrl = fileUtil.getS3Url(imageKey);
            log.info("이미지 S3 업로드 완료: key={}, url={}", imageKey, imageUrl);
        }

        // 카테고리 조회 & Product 저장
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Product product = Product.builder()
            .category(category)
            .modelName(name)
            .modelImage(imageUrl)
            .modelKey(fileKey)
            .createdAt(LocalDateTime.now())
            .build();

        productRepository.save(product);

        // FastAPI /upload 호출 (파일 URL 전달)
        UploadResponse uploadResponse = pdfUpload(product.getId(), fileUrl);
        log.info("RAG 인덱싱 성공: pdf_id={}, store_path={}", uploadResponse.getPdfId(),
            uploadResponse.getStorePath());

        // 알림
        notificationService.addNotification(name +
            " 제품이 성공적으로 등록되었습니다.", "제품 등록 완료", authentication);

        return uploadResponse.getMessage();
    }

    @Override
    public String downloadPdf(Long productId, Authentication authentication) {
        findMember(authentication);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        String key = product.getModelKey();
        return fileUtil.getS3Url(key);
    }

    private UploadResponse pdfUpload(Long productId, String fileUrl) {
        UploadRequest uploadRequest = new UploadRequest(productId, fileUrl);

        return ragWebClient.post()
            .uri("/upload")
            .bodyValue(uploadRequest)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, res ->
                res.bodyToMono(String.class)
                    .defaultIfEmpty("요청 본문 없음")
                    .flatMap(
                        msg -> Mono.error(new IllegalArgumentException("잘못된 요청: " + msg)))
            )
            .onStatus(HttpStatusCode::is5xxServerError, res ->
                res.bodyToMono(String.class)
                    .defaultIfEmpty("서버 오류 본문 없음")
                    .flatMap(
                        msg -> Mono.error(new IllegalStateException("서버 오류: " + msg)))
            )
            .bodyToMono(UploadResponse.class)
            .switchIfEmpty(Mono.error(new IllegalStateException("응답이 비어있음")))
            .block();
    }

    private void findMember(Authentication authentication) {
        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
        memberRepository.findById(memberDTO.getId())
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }
}