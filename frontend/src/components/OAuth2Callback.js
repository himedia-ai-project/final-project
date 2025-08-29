import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { loginSuccess } from '../redux/LoginSlice';
import { favoriteList } from '../api/favoriteApi';
import { getRecentProducts } from '../api/chatApi';
import { Box, CircularProgress, Typography } from '@mui/material';
import axios from 'axios';
import { API_SERVER_HOST } from '../config/ApiConfig';

const OAuth2Callback = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  
  useEffect(() => {
    const handleOAuth2Login = async () => {
      try {
        const params = new URLSearchParams(location.search);
        const id = params.get('id');
        const name = params.get('name');
        const role = params.get('role');
        
        if (!id || !name || !role) {
          console.error('Missing OAuth2 parameters');
          navigate('/login?error=missing-oauth-params');
          return;
        }
        
        // 현재 로그인 상태 확인
        const checkLoginRes = await axios.get(`${API_SERVER_HOST}/api/member/me`, {
          withCredentials: true
        });
        
        if (!checkLoginRes.data.isLoggedIn) {
          console.warn('토큰은 전달되었지만 로그인 상태가 아닙니다');
        }
        
        // 즐겨찾기 목록과 최근 사용 제품 목록 가져오기
        let favs = [];
        let recents = [];
        
        try {
          favs = await favoriteList(id);
        } catch (err) {
          console.warn('즐겨찾기 로드 실패:', err);
        }
        
        try {
          recents = await getRecentProducts();
        } catch (err) {
          console.warn('최근 제품 로드 실패:', err);
        }
        
        // Redux 상태 업데이트
        dispatch(loginSuccess({
          id,
          name,
          role,
          favoriteList: favs || [],
          recentList: recents || [],
          message: '구글 로그인에 성공했습니다.'
        }));
        
        navigate('/device');
      } catch (error) {
        console.error('OAuth2 콜백 처리 오류:', error);
        navigate('/login?error=oauth-processing');
      }
    };
    
    handleOAuth2Login();
  }, [dispatch, location.search, navigate]);
  
  return (
    <Box sx={{ 
      display: 'flex', 
      flexDirection: 'column',
      alignItems: 'center', 
      justifyContent: 'center',
      height: '100vh' 
    }}>
      <CircularProgress size={60} />
      <Typography variant="h6" sx={{ mt: 3 }}>
        구글 로그인 처리 중...
      </Typography>
      <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
        잠시만 기다려 주세요.
      </Typography>
    </Box>
  );
};

export default OAuth2Callback;
