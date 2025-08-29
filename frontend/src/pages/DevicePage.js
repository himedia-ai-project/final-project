import FavoriteIcon from "@mui/icons-material/Favorite";
import FavoriteBorderIcon from "@mui/icons-material/FavoriteBorder";
import SearchIcon from "@mui/icons-material/Search";
import { Box, Typography } from "@mui/material";
import React, { useEffect, useState, useCallback, useMemo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { productList, searchProducts } from "../api/productApi";
import { addFavorite, deleteFavorite, favoriteList } from "../api/favoriteApi";
import { setProducts } from "../redux/ProductSlice";
import { updateFavorites } from "../redux/LoginSlice";
import { showToastMessage } from "../redux/NotificationSlice";
import "../styles/DevicePage.css";

const DevicePage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [searchQuery, setSearchQuery] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);
  const products = useSelector((state) => state.product.products) || [];
  const userFavorites = useSelector((state) => state.login.favoriteList) || [];
  const userId = useSelector((state) => state.login.user?.id);
  const [searchMode, setSearchMode] = useState('name');
  const [hasCategoryInQuery, setHasCategoryInQuery] = useState(false);
  const MIN_SEARCH_LENGTH = 7;
  
  const categoriesFromRedux = useSelector((state) => state.product.categories);
  const categories = useMemo(() => categoriesFromRedux || [], [categoriesFromRedux]);

  const fetchProducts = useCallback(async () => {
    try {
      const data = await productList();
      dispatch(setProducts(data));
    } catch (error) {
      console.error("제품 목록 가져오기 실패:", error);
    }
  }, [dispatch]);

  const fetchFavorites = useCallback(async () => {
    if (!userId) return;
    
    try {
      const favorites = await favoriteList(userId);
      if (Array.isArray(favorites)) {
        dispatch(updateFavorites(favorites));
      }
    } catch (error) {
      console.error("즐겨찾기 목록 가져오기 실패:", error);
    }
  }, [userId, dispatch]);

  useEffect(() => {
    fetchProducts();
    fetchFavorites();
  }, [fetchProducts, fetchFavorites]);

  useEffect(() => {
    if (searchMode === 'name') {
      fetchProducts();
    }
  }, [searchMode, fetchProducts]);

  useEffect(() => {
    if (searchMode === 'feature' && searchQuery) {
      const queryLower = searchQuery.toLowerCase();
      const categoryIncluded = categories.some(category => 
        queryLower.includes(category.searchKeyword.toLowerCase())
      );
      setHasCategoryInQuery(categoryIncluded);
    } else {
      setHasCategoryInQuery(false);
    }
  }, [searchQuery, searchMode, categories]);

  const filteredDevices = products.filter((device) => {
    if (!device || !device.name) return false;
    return device.name.toLowerCase().includes(searchQuery.toLowerCase());
  });

  const handleDeviceClick = (device) => {
    navigate("/chat", { state: { deviceName: device.name, productId: device.id } });
  };

  const isFavorite = (productId) => {
    return userFavorites.includes(productId);
  };

  const toggleFavorite = async (e, deviceId) => {
    e.stopPropagation();

    if (!userId) {
      dispatch(showToastMessage("로그인이 필요합니다."));
      return;
    }

    if (isProcessing) return;

    setIsProcessing(true);
    try {
      let newFavorites;
      if (isFavorite(deviceId)) {
        await deleteFavorite(userId, deviceId);
        newFavorites = userFavorites.filter(id => id !== deviceId);
      } else {
        await addFavorite(userId, deviceId);
        newFavorites = [...userFavorites, deviceId];
      }
      
      dispatch(updateFavorites(newFavorites));
    } catch (error) {
      console.error("즐겨찾기 토글 실패:", error);
      dispatch(showToastMessage("즐겨찾기 처리 중 오류가 발생했습니다."));
      await fetchFavorites();
    } finally {
      setIsProcessing(false);
    }
  };

  const handleSearch = async (e) => {
    if (e.key === 'Enter') {
      try {
        if (searchMode === 'feature') {
          if (searchQuery.trim().length < MIN_SEARCH_LENGTH) {
            dispatch(showToastMessage(`검색어는 최소 ${MIN_SEARCH_LENGTH}자 이상이어야 합니다.`));
            return;
          }
          
          if (!hasCategoryInQuery) {
            dispatch(showToastMessage("검색어에 제품 카테고리(TV, 세탁기 등)를 포함해주세요."));
            return;
          }
          
          const searchResults = await searchProducts(searchQuery);
          dispatch(setProducts(searchResults));
        }
      } catch (error) {
        console.error("검색 실패:", error);
        dispatch(showToastMessage("검색 중 오류가 발생했습니다."));
      }
    }
  };

  const handleSearchModeChange = (e) => {
    const newMode = e.target.value;
    setSearchMode(newMode);
    setSearchQuery('');
  };

  return (
    <Box className="device-page-container">
      <Box className="search-section">
        <Box className="search-bar">
          <SearchIcon sx={{ color: "#777777" }} />
          <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
            <input
              type="text"
              placeholder={searchMode === 'name' 
                ? "제품명으로 검색..." 
                : "제품 특징으로 검색..."}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={handleSearch}
              className={searchMode === 'feature' && searchQuery && !hasCategoryInQuery ? "missing-category" : ""}
            />
            <select 
              value={searchMode}
              onChange={handleSearchModeChange}
              className="search-select"
            >
              <option value="name">제품명 검색</option>
              <option value="feature">특징 기반 검색</option>
            </select>
          </Box>
        </Box>
        
        {searchMode === 'feature' && !searchQuery && (
          <Box className="search-guide">
            <Typography variant="body2" color="text.secondary">
              <strong>카테고리</strong>와 찾는 <strong>특징</strong>을 함께 입력하세요. (예: TV 스마트 기능, 세탁기 건조 기능)
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
              검색 결과의 정확도를 높이기 위해 카테고리를 반드시 포함해주세요.
            </Typography>
          </Box>
        )}
      </Box>

      <Box className="device-grid">
        {(searchMode === 'name' ? filteredDevices : products).map((device) => (
          <Box
            key={device.id}
            className="device-item"
            onClick={() => handleDeviceClick(device)}
          >
            <Box className="device-icon-container">
              <img
                src={device.url}
                alt={device.name}
                className="device-icon"
              />
              <div
                className={`favorite-icon ${isFavorite(device.id) ? "active" : ""}`}
                onClick={(e) => toggleFavorite(e, device.id)}
              >
                {isFavorite(device.id) ? (
                  <FavoriteIcon fontSize="small" />
                ) : (
                  <FavoriteBorderIcon fontSize="small" />
                )}
              </div>
            </Box>
            <Typography className="device-name">{device.name}</Typography>
          </Box>
        ))}
      </Box>
    </Box>
  );
};

export default DevicePage;
