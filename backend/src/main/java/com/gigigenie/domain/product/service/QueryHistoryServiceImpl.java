package com.gigigenie.domain.product.service;

import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.product.dto.HistoryRequest;
import com.gigigenie.domain.product.dto.QueryHistoryDTO;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.entity.QueryHistory;
import com.gigigenie.domain.product.repository.ProductRepository;
import com.gigigenie.domain.product.repository.QueryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueryHistoryServiceImpl implements QueryHistoryService {
    private final QueryHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Override
    public void save(HistoryRequest request) {
        Member member = findMember(request.getMemberId());
        Product product = findProduct(request.getProductId());

        List<QueryHistory> histories = new ArrayList<>();

        request.getHistory().forEach(history -> {
            QueryHistory queryHistory = QueryHistory.builder()
                    .product(product)
                    .member(member)
                    .queryText(history.get("queryText").toString())
                    .responseText(history.get("responseText").toString())
                    .queryTime(((Number) history.get("queryTime")).longValue())
                    .build();

            histories.add(queryHistory);
        });

        historyRepository.saveAll(histories);
    }

    @Override
    public List<QueryHistoryDTO> getHistories(Integer memberId, Integer productId) {
        Member member = findMember(memberId);
        Product product = findProduct(productId);
        List<QueryHistory> histories = historyRepository.findByMemberAndProduct(member, product);
        return histories.stream().map(this::entityToDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteByMemberAndProduct(Integer memberId, Integer productId) {
        Member member = findMember(memberId);
        Product product = findProduct(productId);
        historyRepository.deleteByMemberAndProduct(member, product);
    }

    @Override
    public List<Integer> recent(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        List<QueryHistory> histories = historyRepository.findByMember(member);
        if (histories.isEmpty()) {
            return List.of();
        }

        return histories.stream()
                .map(history -> history.getProduct().getId())
                .toList();
    }

    private Member findMember(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private Product findProduct(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
