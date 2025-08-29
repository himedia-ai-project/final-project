import { configureStore } from '@reduxjs/toolkit';
import loginReducer from './LoginSlice';
import productReducer from './ProductSlice';
import notificationReducer from './NotificationSlice';

const store = configureStore({
  reducer: {
    login: loginReducer,
    product: productReducer,
    notification: notificationReducer,
  },
});

export default store;
