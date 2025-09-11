package com.gigigenie.domain.favorite.service;

import java.util.List;
import org.springframework.security.core.Authentication;

public interface FavoriteService {

    List<Long> list(Authentication authentication);

    void addFavorite(Long productId, Authentication authentication);

    void deleteFavorite(Long productId, Authentication authentication);

}
