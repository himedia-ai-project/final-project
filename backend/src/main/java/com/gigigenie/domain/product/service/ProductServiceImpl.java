package com.gigigenie.domain.product.service;

import com.gigigenie.domain.product.dto.ProductResponse;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductResponse> list() {
        List<Product> products = productRepository.findAllWithCategory();
        return products.stream().map(product -> (
            ProductResponse.builder()
                .id(product.getId())
                .name(product.getModelName())
                .url(product.getModelImage() != null ? product.getModelImage()
                    : product.getCategory().getCategoryIcon())
                .build())).collect(Collectors.toList());
    }
}
