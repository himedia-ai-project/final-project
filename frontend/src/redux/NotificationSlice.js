import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  notifications: [],
  toastMessage: '',
  showToast: false,
};

const notificationSlice = createSlice({
  name: 'notification',
  initialState,
  reducers: {
    setNotifications: (state, action) => {
      state.notifications = action.payload;
    },
    addNotification: (state, action) => {
      state.notifications.unshift({
        id: action.payload.id || Date.now(),
        message: action.payload.message,
        title: action.payload.title || '',
        time: action.payload.time || new Date().toLocaleTimeString(),
      });
    },
    removeNotification: (state, action) => {
      state.notifications = state.notifications.filter(
        (notification) => notification.id !== action.payload
      );
    },
    showToastMessage: (state, action) => {
      state.toastMessage = action.payload;
      state.showToast = true;
    },
    hideToastMessage: (state) => {
      state.showToast = false;
    }
  },
});

export const { 
  setNotifications,
  addNotification, 
  removeNotification,
  showToastMessage,
  hideToastMessage
} = notificationSlice.actions;

export default notificationSlice.reducer;
