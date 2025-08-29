import axiosInstance from './axiosInstance';
import store from '../redux/Store';
import { setNotifications, removeNotification } from '../redux/NotificationSlice';

export const fetchNotifications = async () => {
  try {
    const { id: memberId } = store.getState().login || {};
    
    if (!memberId) {
      return [];
    }
    
    const response = await axiosInstance.get('/notifications', {
      params: {
        memberId: memberId
      }
    });
    
    store.dispatch(setNotifications(response.data));
    return response.data;
  } catch (error) {
    console.error("알림 목록 조회 실패:", error);
    return [];
  }
};

export const createNotification = async (message, title = "", fontSize = "normal") => {
  try {
    const { id: memberId } = store.getState().login || {};
    
    if (!memberId) {
      return null;
    }
    
    await axiosInstance.post('/notifications', {
      memberId,
      message,
      title,
    });
    
    await fetchNotifications();
    
    return true;
  } catch (error) {
    console.error("알림 추가 실패:", error);
    return null;
  }
};

export const deleteNotification = async (notificationId) => {
  try {
    await axiosInstance.delete(`/notifications/${notificationId}`);
    store.dispatch(removeNotification(notificationId));
    return true;
  } catch (error) {
    console.error("알림 삭제 실패:", error);
    return false;
  }
};

