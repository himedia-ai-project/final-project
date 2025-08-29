import React, { useState, useEffect } from "react";
import { Box, Typography, Button } from "@mui/material";
import { useNavigate } from "react-router-dom";
import "../styles/IntroPage.css";

// 이미지 임포트
import uploadImg from "../images/upload_img.png";
import chatImg from "../images/chat_img.png";
import searchImg from "../images/search_img.png";
import logo from "../images/manulo_logo.png";

const IntroPage = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);
  const totalSteps = 3;

  // 다음 단계로 이동
  const nextStep = () => {
    if (currentStep < totalSteps - 1) {
      setCurrentStep(currentStep + 1);
    }
  };

  // 이전 단계로 이동
  const prevStep = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  };

  // 특정 단계로 직접 이동
  const goToStep = (step) => {
    if (step >= 0 && step < totalSteps) {
      setCurrentStep(step);
    }
  };

  // 서비스 시작하기 (메인 페이지로 이동)
  const startService = () => {
    navigate("/device");
  };

  // 인트로 건너뛰기
  const skipIntro = () => {
    navigate("/device");
  };

  // 로그인 페이지로 이동
  const goToLogin = () => {
    navigate("/login");
  };

  // 단계별 컨텐츠 정의
  const steps = [
    {
      title: "전자제품 설명서 등록하기",
      description: "구매한 전자제품의 설명서를 업로드하세요.\nAI가 내용을 분석하여 필요한 정보를\n쉽게 찾을 수 있도록 도와드려요.",
      image: uploadImg
    },
    {
      title: "대화형 질의응답으로 쉽게 해결",
      description: '"전원이 안 켜져요", "와이파이 연결 방법은?"\n과 같은 질문을 자연스럽게 물어보세요.\nAI가 즉시 정확한 답변을 제공합니다.',
      image: chatImg
    },
    {
      title: "스마트한 제품 검색 기능",
      description: '제품명은 물론, 특징이나 기능으로도 검색 가능해요.\n"블루투스 스피커" 또는 "음성인식 가능한 TV"처럼\n원하는 제품을 쉽게 찾을 수 있습니다.',
      image: searchImg
    }
  ];

  // 키보드 이벤트 처리
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "ArrowRight") {
        nextStep();
      } else if (e.key === "ArrowLeft") {
        prevStep();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [currentStep]);

  return (
    <Box className="intro-page-container">
      {/* 로고 영역 */}
      <Box className="intro-header">
        <img src={logo} alt="Manulo 로고" className="intro-logo" />
      </Box>
      
      <Box className="intro-banner">
        <Box className="intro-banner-content">
          <Typography variant="h5" className="intro-headline" sx={{ color: '#333', marginBottom: '28px' }}>
            전자제품 설명서를 AI와 쉽고 편리하게!
          </Typography>
          <Typography variant="subtitle1" className="intro-subheadline">
            복잡한 설명서는 이제 그만,<br />
            당신만의 맞춤형 가이드를 만나보세요
          </Typography>
        </Box>

        {/* 가로 슬라이드 영역 */}
        <Box className="intro-slider">
          <Box 
            className="intro-slides-container" 
            style={{ transform: `translateX(-${currentStep * 100}%)` }}
          >
            {steps.map((step, index) => (
              <Box key={index} className="intro-slide">
                <Typography variant="h6" className="step-number" sx={{ mb: 3 }}>
                  STEP {index + 1}
                </Typography>
                <Typography variant="h6" className="step-title">
                  {step.title}
                </Typography>
                <Box className="step-image">
                  <img src={step.image} alt={`STEP ${index + 1} 이미지`} />
                </Box>
                <Typography variant="body1" className="step-desc" style={{ whiteSpace: 'pre-line' }}>
                  {step.description}
                </Typography>
              </Box>
            ))}
          </Box>
        </Box>

        {/* 네비게이션 컨트롤 */}
        <Box className="intro-navigation" sx={{ textAlign: 'center' }}>
          {/* 점 표시 인디케이터 */}
          <Box className="dot-indicators">
            {Array.from({ length: totalSteps }).map((_, index) => (
              <span 
                key={index} 
                className={`dot-indicator ${currentStep === index ? 'active' : ''}`}
                onClick={() => goToStep(index)}
              ></span>
            ))}
          </Box>
          
          {/* 화살표 버튼 */}
          <Box className="arrow-controls" sx={{ margin: '0 auto' }}>
            <Button 
              className="arrow-btn prev" 
              disabled={currentStep === 0}
              onClick={prevStep}
            >
              &lt; 이전
            </Button>
            <Button 
              className="arrow-btn next" 
              disabled={currentStep === totalSteps - 1}
              onClick={nextStep}
            >
              다음 &gt;
            </Button>
          </Box>
        </Box>

        {/* 하단 버튼 영역 */}
        <Box className="intro-actions">
          <Button 
            className="skip-intro-btn"
            onClick={skipIntro}
          >
            건너뛰기
          </Button>
          {currentStep === totalSteps - 1 ? (
            <Box className="final-step-buttons">
              <Button 
                className="login-btn" 
                variant="contained"
                onClick={goToLogin}
                sx={{ backgroundColor: '#00c471' }}
              >
                로그인
              </Button>
              <Button 
                className="start-service-btn" 
                variant="contained"
                onClick={startService}
                sx={{ backgroundColor: '#495057' }}
              >
                시작하기
              </Button>
            </Box>
          ) : (
            <Button 
              className="close-intro-btn"
              onClick={() => goToStep(totalSteps - 1)}
            >
              마지막으로
            </Button>
          )}
        </Box>
      </Box>
    </Box>
  );
};

export default IntroPage;