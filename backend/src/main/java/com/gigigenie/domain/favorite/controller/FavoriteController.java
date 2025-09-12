package com.gigigenie.domain.favorite.controller;

import com.gigigenie.domain.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/favorite")
@RestController
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "회원별 즐겨찾기 전체 조회")
    @GetMapping("/member")
    public ResponseEntity<List<Long>> list(Authentication authentication) {
        List<Long> ids = favoriteService.list(authentication);
        log.info("즐겨찾기 목록 조회 결과: {}", ids);
        return ResponseEntity.ok(ids);
    }

    @Operation(summary = "즐겨찾기 추가")
    @PostMapping("/add")
    public void addFavorite(@RequestParam Long productId, Authentication authentication) {
        log.info("즐겨찾기 추가 요청: {}", productId);
        favoriteService.addFavorite(productId, authentication);
    }

    @Operation(summary = "즐겨찾기 삭제")
    @DeleteMapping("/delete")
    public void deleteFavorite(@RequestParam Long productId, Authentication authentication) {
        log.info("즐겨찾기 삭제 요청: {}", productId);
        favoriteService.deleteFavorite(productId, authentication);
    }
}
