import React, { useState, useEffect } from "react";
import { 
  Box, 
  Typography, 
  CircularProgress, 
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stack,
  Snackbar,
  Alert
} from "@mui/material";
import SendIcon from "@mui/icons-material/Send";
import "../styles/ChatPage.css";
import { useLocation, useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import ReactMarkdown from 'react-markdown';
import { selectProduct, continueChat, createNewChat, endChat, getHistories, getRecentProducts  } from '../api/chatApi';

const ChatPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { deviceName, productId } = location.state || {};
  const [openExitDialog, setOpenExitDialog] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
  const [sessionId, setSessionId] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [historyLoaded, setHistoryLoaded] = useState(false);
  
  // 로그인 상태 확인
  const { id } = useSelector(state => state.login || { id: null });
  const isLoggedIn = !!id;

  // 컴포넌트 마운트 시 제품 선택 처리
  useEffect(() => {
    if (!deviceName || !productId) {
      navigate("/device");
      return;
    }

    // 기본 환영 메시지 설정
    setMessages([
      {
        role: "bot",
        text: "안녕하세요. 어떤 사용법을 알려드릴까요?",
      },
    ]);

    // 로그인한 사용자면 이전 대화 내역 가져오기
    const fetchChatHistory = async () => {
      if (isLoggedIn) {
        try {
          const histories = await getHistories(productId);
          
          if (histories && histories.length > 0) {
            // 이전 대화 내역을 시간 순으로 정렬
            const sortedHistories = [...histories].sort((a, b) => 
              new Date(a.createdAt) - new Date(b.createdAt)
            );
            
            // 대화 내역 형태로 변환하여 메시지 배열에 추가
            const historyMessages = [];
            
            sortedHistories.forEach(history => {
              historyMessages.push({
                role: "user", 
                text: history.queryText,
                isHistory: true
              });
              
              historyMessages.push({
                role: "bot", 
                text: history.responseText,
                isHistory: true
              });
            });
            
            // 모든 메시지를 세팅 (환영 메시지 + 이전 대화)
            if (historyMessages.length > 0) {
              setMessages(prev => [
                ...prev,
                {
                  role: "system",
                  text: "이전 대화 내역입니다."
                },
                ...historyMessages
              ]);
            }
          }
          setHistoryLoaded(true);
        } catch (error) {
          console.error("이전 대화 내역 가져오기 실패:", error);
          setHistoryLoaded(true);
        }
      } else {
        setHistoryLoaded(true);
      }
    };
   
    // 제품 선택 시 초기화
    const initializeChat = async () => {
      try {
        setIsLoading(true);
        const response = await selectProduct(productId);
        setSessionId(response.sessionId);
      } catch (error) {
        console.error("채팅 초기화 실패:", error);
        setSnackbar({
          open: true,
          message: '채팅을 초기화하는데 실패했습니다.',
          severity: 'error'
        });
      } finally {
        setIsLoading(false);
        
        // 채팅 초기화 후 대화 내역 가져오기
        await fetchChatHistory();
      }
    };

    initializeChat();
  }, [deviceName, productId, navigate, isLoggedIn]);

  // 메시지 전송 처리
  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const userMessage = { role: "user", text: input };
    setMessages(prev => [...prev, userMessage]);
    
    setMessages(prev => [...prev, { role: "bot", isLoading: true }]);
    setIsLoading(true);
    
    const startTime = Date.now();
    
    try {
      // 세션 ID가 있으면 continueChat, 없으면 createNewChat 사용
      const response = sessionId 
        ? await continueChat(input, productId, sessionId, 3)
        : await createNewChat(input, productId, 3);

      // 세션 ID 업데이트 (없었던 경우)
      if (!sessionId && response.sessionId) {
        setSessionId(response.sessionId);
      }

      const endTime = Date.now();
      const queryTime = endTime - startTime;

      setMessages(prev => [
        ...prev.slice(0, -1),
        { role: "bot", text: response.answer, queryTime }
      ]);
    } catch (error) {
      console.error("답변 생성 실패:", error);
      setMessages(prev => [
        ...prev.slice(0, -1),
        { role: "bot", text: "답변을 생성하는 중에 문제가 발생했습니다.", queryTime: 0 }
      ]);
    } finally {
      setIsLoading(false);
    }

    setInput("");
  };

  // 채팅방 나가기 버튼 클릭
  const handleExitClick = () => {
    setOpenExitDialog(true);
  };

  // 다이얼로그 닫기
  const handleCloseDialog = () => {
    setOpenExitDialog(false);
  };

  // 저장 후 나가기
  const handleSaveAndExit = async () => {
    if (sessionId && isLoggedIn) {
      try {
        await endChat(sessionId);
        await getRecentProducts();

        setSnackbar({
          open: true,
          message: '채팅 내역이 저장되었습니다.',
          severity: 'success'
        });
        setTimeout(() => {
          navigate('/device');
        }, 1500);
      } catch (error) {
        setSnackbar({
          open: true,
          message: '채팅 내역 저장에 실패했습니다.',
          severity: 'error'
        });
      }
    } else {
      navigate('/device');
    }
  };

  // 저장 없이 나가기
  const handleConfirmExit = async () => {
    if (sessionId && isLoggedIn) {
        try {
            // skipSave를 true로 설정하여 저장하지 않고 세션 종료
            await endChat(sessionId, true);
            await getRecentProducts();
        } catch (error) {
            console.error("세션 종료 실패:", error);
        }
    }
    navigate('/device');
};

  // 새 채팅 시작
  const handleNewChat = async () => {
    // 기존 세션 종료 (저장하지 않음)
    if (sessionId && isLoggedIn) {
        try {
            // skipSave를 true로 설정하여 저장하지 않고 세션 종료
            await endChat(sessionId, true);
            await getRecentProducts();
        } catch (error) {
            console.error("세션 종료 실패:", error);
        }
    }
    
    // 새 세션 시작
    setSessionId(null);
    setMessages([
        {
            role: "bot",
            text: "안녕하세요. 어떤 사용법을 알려드릴까요?",
        },
    ]);
    setSnackbar({
        open: true,
        message: '새로운 채팅이 시작되었습니다.',
        severity: 'info'
    });
};

  return (
    <Box className="chat-main">
      <Box className="chat-header">
        <Typography>{deviceName}</Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button 
            onClick={handleNewChat}
            variant="text"
            className="exit-button"
          >
            새 채팅
          </Button>
          <Button 
            onClick={handleExitClick}
            variant="text"
            className="exit-button"
          >
            채팅방 나가기
          </Button>
        </Box>
      </Box>

      {/* 다이얼로그 및 스낵바 컴포넌트 */}
      <Dialog
        open={openExitDialog}
        onClose={handleCloseDialog}
        PaperProps={{
          className: "exit-dialog-paper"
        }}
      >
        <DialogTitle className="exit-dialog-title">
          채팅방 나가기
        </DialogTitle>
        <DialogContent>
          <Typography className="exit-dialog-content">
            {isLoggedIn ? "채팅 내역을 저장하시겠습니까?" : "정말 나가시겠습니까?"}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Stack 
            direction="row" 
            spacing={1}
            className="dialog-buttons"
          >
            {isLoggedIn && (
              <Button
                variant="contained"
                onClick={handleSaveAndExit}
                className="save-button"
              >
                저장하기
              </Button>
            )}
            <Button
              variant="contained"
              onClick={handleConfirmExit}
              className="exit-button-dialog"
            >
              나가기
            </Button>
            <Button
              variant="outlined"
              onClick={handleCloseDialog}
              className="cancel-button"
            >
              취소
            </Button>
          </Stack>
        </DialogActions>
      </Dialog>

      <Snackbar 
        open={snackbar.open} 
        autoHideDuration={1500} 
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        sx={{
          '& .MuiPaper-root': {
            width: 'auto',
            minWidth: 'auto',
            maxWidth: '90%'
          }
        }}
      >
        <Alert 
          onClose={() => setSnackbar({ ...snackbar, open: false })} 
          severity={snackbar.severity}
          sx={{ 
            backgroundColor: '#4AD395',
            color: '#ffffff',
            '& .MuiAlert-icon': { color: '#ffffff' },
            width: 'auto',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '6px 16px'
          }}
          className="toast-notification"
        >
          {snackbar.message}
        </Alert>
      </Snackbar>

      {/* 메시지 목록 */}
      <Box className="chat-messages">
        {messages.map((msg, idx) => (
          <Box 
            key={idx} 
            className={`message ${msg.role} ${msg.isHistory ? 'history-message' : ''}`}
          >
            {msg.isLoading ? (
              <Box className="loading-container">
                <CircularProgress 
                  size={20} 
                  className="loading-spinner"
                />
                <Typography>답변 생성 중...</Typography>
              </Box>
            ) : (
              <ReactMarkdown
                components={{
                  h1: ({node, ...props}) => <Typography variant="h4" gutterBottom {...props} />,
                  h2: ({node, ...props}) => <Typography variant="h5" gutterBottom {...props} />,
                  h3: ({node, ...props}) => <Typography variant="h6" gutterBottom {...props} />,
                  p: ({node, ...props}) => <Typography paragraph {...props} style={{margin: '8px 0'}} />,
                  ul: ({node, ...props}) => <Box component="ul" sx={{pl: 2}} {...props} />,
                  ol: ({node, ...props}) => <Box component="ol" sx={{pl: 2}} {...props} />,
                  li: ({node, ...props}) => <Typography component="li" {...props} style={{margin: '4px 0'}} />,
                  blockquote: ({node, ...props}) => (
                    <Box
                      sx={{
                        borderLeft: '4px solid #ccc',
                        paddingLeft: 2,
                        margin: '10px 0',
                        color: 'text.secondary',
                        bgcolor: 'rgba(0,0,0,0.03)'
                      }}
                      {...props}
                    />
                  ),
                  strong: ({node, ...props}) => <Typography component="span" fontWeight="bold" {...props} />
                }}
              >
                {msg.text}
              </ReactMarkdown>
            )}
          </Box>
        ))}
      </Box>

      {/* 입력 필드 */}
      <Box className="chat-input">
        <div className="input-container">
          <input
            type="text"
            placeholder="해당 제품의 궁금한 점을 질문하세요!"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && !isLoading && handleSend()}
            disabled={isLoading || !historyLoaded}
          />
          <button 
            className="input-send-button" 
            onClick={handleSend}
            disabled={isLoading || !historyLoaded}
          >
            <SendIcon fontSize="small" />
          </button>
        </div>
      </Box>
    </Box>
  );
};

export default ChatPage;
