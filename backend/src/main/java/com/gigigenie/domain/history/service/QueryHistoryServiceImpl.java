package com.gigigenie.domain.history.service;

import com.gigigenie.domain.history.dto.QueryHistoryDTO;
import com.gigigenie.domain.history.entity.QueryHistory;
import com.gigigenie.domain.history.repository.QueryHistoryRepository;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueryHistoryServiceImpl implements QueryHistoryService {

    private final QueryHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<QueryHistoryDTO> getHistories(Long productId, Authentication authentication) {
        Member member = findMember(authentication);
        Product product = findProduct(productId);
        List<QueryHistory> histories = historyRepository.findByMemberAndProduct(member, product);
        return histories.stream().map(this::entityToDTO).collect(Collectors.toList());
    }

    @Override
    public List<Long> recent(Authentication authentication) {
        Member member = findMember(authentication);
        List<QueryHistory> histories = historyRepository.findByMember(member);
        if (histories.isEmpty()) {
            return List.of();
        }

        return histories.stream()
            .map(history -> history.getProduct().getId())
            .toList();
    }

    private Member findMember(Authentication authentication) {
        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
        return memberRepository.findById(memberDTO.getId())
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }
}
