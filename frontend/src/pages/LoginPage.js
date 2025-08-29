import { Box, Button, TextField, Typography, Link, Alert } from "@mui/material";
import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import logo from "../images/manulo_logo.png";
import { loginStart, loginSuccess, loginFailure, clearError } from "../redux/LoginSlice";
import { loginPost, joinPost, checkEmailDuplicate } from "../api/loginApi";
import { favoriteList } from "../api/favoriteApi";
import { getRecentProducts } from "../api/chatApi";
import GoogleIcon from '@mui/icons-material/Google';
import { API_SERVER_HOST } from "../config/ApiConfig";
import "../styles/LoginPage.css";

const LoginPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { loading, error: reduxError, message } = useSelector((state) => state.login || {});
  const [isLogin, setIsLogin] = useState(true);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [emailError, setEmailError] = useState("");

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  const validateEmail = (email) => {
    const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
    return emailRegex.test(email);
  };

  const validatePassword = (password) => {
    if (password.length < 8) {
      dispatch(loginFailure("비밀번호는 최소 8자 이상이어야 합니다."));
      return false;
    }
    if (!/[0-9]/.test(password)) {
      dispatch(loginFailure("비밀번호는 숫자를 포함해야 합니다."));
      return false;
    }
    if (!/[a-z]/.test(password)) {
      dispatch(loginFailure("비밀번호는 소문자를 포함해야 합니다."));
      return false;
    }
    if (!/[A-Z]/.test(password)) {
      dispatch(loginFailure("비밀번호는 대문자를 포함해야 합니다."));
      return false;
    }
    if (!/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password)) {
      dispatch(loginFailure("비밀번호는 특수문자를 포함해야 합니다."));
      return false;
    }
    return true;
  };

  const handleEmailChange = async (e) => {
    const value = e.target.value;
    setEmail(value);
    setEmailError("");

    if (!validateEmail(value)) {
      setEmailError("올바른 이메일 형식이 아닙니다.");
      return;
    }

    if (!isLogin && value) {
      try {
        const response = await checkEmailDuplicate(value);
        if (response) {
          setEmailError("이미 사용 중인 이메일입니다.");
        }
      } catch (error) {
        console.error("이메일 중복 체크 오류:", error);
        setEmailError("이메일 중복 확인 중 오류가 발생했습니다.");
      }
    }
  };

  const handlePasswordChange = (e) => {
    const value = e.target.value;
    setPassword(value);
  };

  const handleLogin = async () => {
    if (!validateEmail(email)) {
      dispatch(loginFailure("올바른 이메일 형식이 아닙니다."));
      return;
    }

    try {
      dispatch(loginStart());
      const response = await loginPost(email, password);
      const favorites = await favoriteList(response.id);
      const recents = await getRecentProducts();
      
      dispatch(loginSuccess({
        id: response.id,
        name: response.name,
        role: response.role,
        favoriteList: favorites,
        recentList: recents,
        message: "로그인에 성공했습니다."
      }));
      
      navigate("/device");
    } catch (error) {
      dispatch(loginFailure(error.response?.data?.message || "로그인에 실패했습니다."));
    }
  };

  const handleContinueWithoutLogin = () => {
    navigate("/device");
  };

  const handleJoin = async () => {
    if (!validateEmail(email)) {
      dispatch(loginFailure("올바른 이메일 형식이 아닙니다."));
      return;
    }
    if (!validatePassword(password)) return;
    if (password !== confirmPassword) {
      dispatch(loginFailure("비밀번호가 일치하지 않습니다."));
      return;
    }
    if (!name || name.length < 2) {
      dispatch(loginFailure("이름은 2자 이상이어야 합니다."));
      return;
    }

    try {
      dispatch(loginStart());
      const response = await joinPost({ email, password, name });
      
      if (response && response.success) {
        dispatch(loginSuccess({ 
          message: "회원가입이 완료되었습니다. 로그인 해주세요."
        }));
        setIsLogin(true);
      } else {
        dispatch(loginFailure(response?.message || "회원가입에 실패했습니다."));
      }
    } catch (error) {
      console.error("회원가입 오류:", error);
      dispatch(loginFailure(error.response?.data?.message || "회원가입 중 오류가 발생했습니다."));
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = `${API_SERVER_HOST}/oauth2/authorization/google`;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    isLogin ? handleLogin() : handleJoin();
  };

  const handleLogoClick = () => {
    navigate("/device");
  };

  return (
    <Box className="login-page-container">
      <Box className="login-wrapper">
        <Box className="login-header-bar">
          <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
            <img
              src={logo}
              alt="Logo"
              onClick={handleLogoClick}
              className="login-logo"
            />
          </Box>
        </Box>

        <Box className="login-content">
          <Typography variant="h6" sx={{ mb: 2, textAlign: "center" }}>
            {isLogin ? "로그인" : "회원가입"}
          </Typography>

          {(reduxError || emailError) && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {reduxError || emailError}
            </Alert>
          )}

          {message && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {message}
            </Alert>
          )}

          {isLogin && (
            <Box className="guest-button-container">
              <Button
                fullWidth
                variant="outlined"
                startIcon={<GoogleIcon />}
                onClick={handleGoogleLogin}
                className="google-button"
                sx={{
                  mb: 2,
                  color: '#757575',
                  borderColor: '#DADCE0',
                  '&:hover': {
                    borderColor: '#DADCE0',
                    backgroundColor: '#F2F2F2',
                  },
                  height: '48px',
                  textTransform: 'none',
                  fontSize: '16px',
                  fontWeight: 500,
                }}
              >
                Google로 계속하기
              </Button>
            </Box>
          )}
          
          {isLogin && (
            <Box className="divider">
              <Typography variant="body2" className="divider-text">
                또는
              </Typography>
            </Box>
          )}

          <form onSubmit={handleSubmit} className="login-form">
            {!isLogin && (
              <TextField
                fullWidth
                label="이름"
                variant="outlined"
                value={name}
                onChange={(e) => setName(e.target.value)}
                sx={{ mb: 2 }}
                required
              />
            )}

            <TextField
              fullWidth
              label="이메일"
              variant="outlined"
              type="email"
              value={email}
              onChange={handleEmailChange}
              error={!!emailError}
              helperText={emailError}
              sx={{ mb: 2 }}
              required
            />

            <TextField
              fullWidth
              label="비밀번호"
              variant="outlined"
              type="password"
              value={password}
              onChange={handlePasswordChange}
              sx={{ mb: 2 }}
              required
            />

            {!isLogin && (
              <TextField
                fullWidth
                label="비밀번호 확인"
                variant="outlined"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                sx={{ mb: 2 }}
                required
              />
            )}

            <Button
              fullWidth
              variant="contained"
              type="submit"
              className="login-button"
              disabled={loading || (!isLogin && !!emailError)}
            >
              {loading ? "처리 중..." : (isLogin ? "로그인" : "회원가입")}
            </Button>
          </form>

          <Box sx={{ textAlign: "center", mb: 2 }}>
            <Typography variant="body2">
              {isLogin ? "계정이 없으신가요? " : "이미 계정이 있으신가요? "}
              <Link
                component="button"
                variant="body2"
                onClick={() => {
                  setIsLogin(!isLogin);
                  dispatch(clearError());
                  setEmailError("");
                }}
                className="link-text"
              >
                {isLogin ? "회원가입" : "로그인"}
              </Link>
            </Typography>
          </Box>

          {isLogin && (
            <>
              <Box className="divider">
                <Typography variant="body2" className="divider-text">
                  또는
                </Typography>
              </Box>
              
              <Box className="guest-button-container">
                <Button
                  fullWidth
                  variant="contained"
                  className="guest-button"
                  onClick={handleContinueWithoutLogin}
                >
                  로그아웃 유지
                </Button>
              </Box>
            </>
          )}

          <Typography variant="caption" className="terms-text">
            {isLogin
              ? "로그인 시 서비스 이용약관과 개인정보 처리방침에 동의하게 됩니다."
              : "회원가입 시 서비스 이용약관과 개인정보 처리방침에 동의하게 됩니다."}
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};

export default LoginPage;
