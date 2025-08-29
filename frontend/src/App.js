import { Box } from "@mui/material";
import { useEffect, useRef, useState, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Route, Routes, useLocation } from "react-router-dom";
import { productList } from "./api/productApi";
import axios from "axios";
import { API_SERVER_HOST } from "./config/ApiConfig";
import "./App.css";
import ChatPage from "./pages/ChatPage";
import DevicePage from "./pages/DevicePage";
import IntroPage from "./pages/IntroPage";
import Header from "./pages/Header";
import LoginPage from "./pages/LoginPage";
import SideLayout from "./pages/SideLayout";
import ToastNotification from "./components/ToastNotification";
import { setProducts } from "./redux/ProductSlice";
import { loginSuccess } from "./redux/LoginSlice";
import OAuth2Callback from "./components/OAuth2Callback";
import { fetchNotifications } from "./api/notificationApi";

function App() {
  const location = useLocation();
  const dispatch = useDispatch();
  const isLoginPage = location.pathname === "/login";
  const isIntroPage = location.pathname === "/";
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { isLogin, user } = useSelector((state) => state.login || {});
  const sidebarRef = useRef(null);
  
  const isUserRole = () => {
    if (!user || !user.role) return false;
    return user.role === "USER";
  };
  
  const getUserRole = () => {
    if (!user || !user.role) return null;
    return user.role;
  };

  const fetchProducts = useCallback(async () => {
    try {
      const data = await productList();
      dispatch(setProducts(data));
    } catch (error) {
      console.error("제품 목록 가져오기 실패:", error);
    }
  }, [dispatch]);

  const checkLoginStatus = useCallback(async () => {
    if (isLogin) return;
    
    try {
      const response = await axios.get(`${API_SERVER_HOST}/api/member/me`, {
        withCredentials: true
      });
      
      if (response.data.isLoggedIn) {
        dispatch(loginSuccess({
          id: response.data.id,
          name: response.data.name,
          role: response.data.role,
          favoriteList: response.data.favoriteList || [],
          recentList: response.data.recentList || []
        }));
      }
    } catch (error) {
      console.error("로그인 상태 확인 중 오류:", error);
    }
  }, [isLogin, dispatch]);

  useEffect(() => {
    checkLoginStatus();
    fetchProducts();
  }, [checkLoginStatus, fetchProducts]);

  useEffect(() => {
    if (isLogin && user && user.id) {
      fetchNotifications();
    }
  }, [isLogin, user]);

  useEffect(() => {
    function handleClickOutside(event) {
      if (
        sidebarOpen &&
        sidebarRef.current &&
        !sidebarRef.current.contains(event.target) &&
        !event.target.closest(".menu-button")
      ) {
        setSidebarOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [sidebarOpen]);

  useEffect(() => {
    setSidebarOpen(false);
  }, [isLogin]);

  const showSidebar = !isLoginPage && isLogin && isUserRole();

  return (
    <div className="App">
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          height: "100vh",
          maxWidth: "480px",
          margin: "0 auto",
          backgroundColor: "#f9f9f9",
          position: "relative",
          overflow: "hidden",
        }}
      >
        {(!isLoginPage && !isIntroPage) && (
          <Header
            isLoggedIn={isLogin}
            userRole={getUserRole()}
            onMenuClick={() => setSidebarOpen(!sidebarOpen)}
          />
        )}

        <Box sx={{ flex: 1, display: "flex", flexDirection: "column", overflow: "auto" }}>
          <Routes>
            <Route path="/" element={<IntroPage />} />
            <Route path="/device" element={<DevicePage />} />
            <Route path="/chat" element={<ChatPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/oauth2-callback" element={<OAuth2Callback />} />
          </Routes>
        </Box>

        {showSidebar && (
          <Box
            ref={sidebarRef}
            sx={{
              position: "fixed",
              top: "60px",
              width: "240px",
              height: "calc(100% - 60px)",
              transition: "right 0.3s ease",
              zIndex: 1300,
              right: sidebarOpen ? { xs: 0, sm: "calc(50% - 240px)" } : "-250px",
            }}
          >
            <SideLayout onClose={() => setSidebarOpen(false)} onProductUpdate={fetchProducts} />
          </Box>
        )}
      </Box>
      <ToastNotification />
    </div>
  );
}

export default App;
