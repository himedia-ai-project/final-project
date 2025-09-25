package com.gigigenie.domain.product.service;

import com.gigigenie.domain.product.dto.ProductResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    List<ProductResponse> list();

    String processPdf(MultipartFile file, Integer categoryId, String name,
        MultipartFile image, Authentication authentication);

    String downloadPdf(Long productId, Authentication authentication);
}
