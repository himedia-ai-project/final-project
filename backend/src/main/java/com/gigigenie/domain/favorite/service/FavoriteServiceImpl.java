package com.gigigenie.domain.favorite.service;

import com.gigigenie.domain.favorite.entity.Favorite;
import com.gigigenie.domain.favorite.repository.FavoriteRepository;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.entity.Member;
import com.gigigenie.domain.member.repository.MemberRepository;
import com.gigigenie.domain.product.entity.Product;
import com.gigigenie.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Long> list(Authentication authentication) {
        Member member = findMember(authentication);
        List<Favorite> favorites = favoriteRepository.findByMember(member);
        if (favorites.isEmpty()) {
            return List.of();
        }
        
        return favorites.stream()
            .map(favorite -> favorite.getProduct().getId())
            .toList();
    }

    @Override
    public void addFavorite(Long productId, Authentication authentication) {
        Member member = findMember(authentication);
        Product product = findProduct(productId);

        List<Favorite> existingFavorites = favoriteRepository.findByMember(member);
        boolean alreadyExists = existingFavorites.stream()
            .anyMatch(fav -> fav.getProduct().getId().equals(product.getId()));

        if (alreadyExists) {
            log.info("이미 즐겨찾기에 존재함: memberId={}, productId={}", member.getMemberId(),
                product.getId());
            return;
        }

        favoriteRepository.save(new Favorite(product, member));
    }

    @Override
    public void deleteFavorite(Long productId, Authentication authentication) {
        Member member = findMember(authentication);
        Product product = findProduct(productId);
        favoriteRepository.deleteByProductAndMember(product, member);
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
