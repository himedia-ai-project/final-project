import React, { useEffect } from 'react';
import { Snackbar, Alert } from '@mui/material';
import { useSelector, useDispatch } from 'react-redux';
import { hideToastMessage } from '../redux/NotificationSlice';
import '../styles/Notification.css';

const ToastNotification = () => {
  const dispatch = useDispatch();
  const { showToast, toastMessage } = useSelector((state) => state.notification);

  useEffect(() => {
    if (showToast) {
      const timer = setTimeout(() => {
        dispatch(hideToastMessage());
      }, 5000);
      
      return () => clearTimeout(timer);
    }
  }, [showToast, dispatch]);

  const handleClose = () => {
    dispatch(hideToastMessage());
  };

  return (
    <Snackbar
      open={showToast}
      autoHideDuration={5000}
      onClose={handleClose}
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
        onClose={handleClose} 
        severity="success" 
        variant="filled"
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
        {toastMessage}
      </Alert>
    </Snackbar>
  );
};

export default ToastNotification;
