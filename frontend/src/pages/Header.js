import { useState, useEffect } from "react";
import Box from "@mui/material/Box";
import logo from "../images/manulo_logo.png";
import { Button, IconButton } from "@mui/material";
import { useNavigate, useLocation } from "react-router-dom";
import MenuIcon from "@mui/icons-material/Menu";
import LogoutIcon from "@mui/icons-material/Logout";
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../redux/LoginSlice";
import { logoutPost } from "../api/loginApi";
import "../styles/Header.css";
import NotificationComponent from "../components/NotificationComponent";
import { fetchNotifications, deleteNotification } from "../api/notificationApi";
import { showToastMessage } from "../redux/NotificationSlice";

const Header = ({ isLoggedIn, onMenuClick, userRole }) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState(null);
  const notifications = useSelector((state) => state.notification.notifications);
  const { id: memberId } = useSelector((state) => state.login || {});
  
  // 현재 채팅 페이지인지 확인
  const isChatPage = location.pathname === "/chat";

  useEffect(() => {
    let interval;
    
    if (isLoggedIn && memberId) {
      fetchNotifications();
      
      interval = setInterval(() => {
        fetchNotifications();
      }, 30000);
    }
    
    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [isLoggedIn, memberId]);

  const handleLoginClick = () => {
    navigate("/login");
  };

  
  const handleLogoClick = () => {
    if (isChatPage) {
      dispatch(showToastMessage("채팅 진행 중에는 홈으로 이동할 수 없습니다.\n채팅방 나가기를 이용해주세요."));
      return;
    }
    navigate("/device");
  };

  const handleLogout = async () => {
    try {
      await logoutPost();
      dispatch(logout());
      navigate("/");
    } catch (error) {
      console.error("로그아웃 실패:", error);
      dispatch(logout());
      navigate("/");
    }
  };

  const handleNotificationClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleCloseNotifications = () => {
    setAnchorEl(null);
  };

  const handleClearNotification = (id) => {
    deleteNotification(id);
  };

  const showMenuButton = isLoggedIn && userRole === "USER";
  const notificationOpen = Boolean(anchorEl);

  return (
    <Box className="header">
      <img
        src={logo}
        alt="Logo"
        onClick={handleLogoClick}
        className={`header-logo ${isChatPage ? 'disabled-logo' : ''}`}
      />
      <Box sx={{ display: 'flex', gap: 1 }}>
        {showMenuButton && (
          <>
            <IconButton
              onClick={handleNotificationClick}
              aria-label="notifications"
              className="notification-button"
            >
              <NotificationsIcon sx={{ color: '#00c471' }} />
            </IconButton>
            <IconButton
              onClick={handleLogout}
              aria-label="logout"
              className="logout-button"
            >
              <LogoutIcon />
            </IconButton>
            <IconButton
              onClick={onMenuClick}
              aria-label="menu"
              className="menu-button"
            >
              <MenuIcon />
            </IconButton>
          </>
        )}
        {!isLoggedIn && (
          <Button
            variant="contained"
            onClick={handleLoginClick}
            size="small"
            className="login-btn"
          >
            로그인
          </Button>
        )}
      </Box>

      <NotificationComponent
        anchorEl={anchorEl}
        open={notificationOpen}
        onClose={handleCloseNotifications}
        notifications={notifications}
        onClearNotification={handleClearNotification}
      />
    </Box>
  );
};

export default Header;
