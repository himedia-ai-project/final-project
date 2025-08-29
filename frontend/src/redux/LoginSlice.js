import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  isLogin: false,
  loading: false,
  error: null,
  message: null,
  id: null,
  user: null,
  favoriteList: [],
  recentList: [],
};

const loginSlice = createSlice({
  name: "login",
  initialState,
  reducers: {
    loginStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    loginSuccess: (state, action) => {
      state.isLogin = true;
      state.loading = false;
      state.error = null;
      state.id = action.payload.id;
      state.user = {
        id: action.payload.id,
        name: action.payload.name,
        role: action.payload.role,
      };
      state.message = action.payload.message;
      state.favoriteList = action.payload.favoriteList || [];
      state.recentList = action.payload.recentList || [];
    },
    loginFailure: (state, action) => {
      state.loading = false;
      state.error = action.payload;
    },
    logout: (state) => {
      state.isLogin = false;
      state.id = null;
      state.user = null;
      state.favoriteList = [];
      state.recentList = [];
    },
    clearError: (state) => {
      state.error = null;
      state.message = null;
    },
    updateFavorites: (state, action) => {
      state.favoriteList = action.payload;
    },
    updateRecents: (state, action) => {
      state.recentList = action.payload;
    }
  },
});

export const { 
  loginStart, 
  loginSuccess, 
  loginFailure, 
  logout, 
  clearError, 
  updateFavorites,
  updateRecents
} = loginSlice.actions;

export default loginSlice.reducer;
