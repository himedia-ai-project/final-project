package com.gigigenie.domain.favorite.service;

import com.gigigenie.domain.favorite.dto.FavoriteRequest;

import java.util.List;

public interface FavoriteService {
    List<Integer> list(Integer memberId);

    void addFavorite(FavoriteRequest request);

    void deleteFavorite(FavoriteRequest request);

}
