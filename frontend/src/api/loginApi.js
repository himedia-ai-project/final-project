import axios from "axios";
import axiosInstance from "./axiosInstance";

import { API_SERVER_HOST } from "../config/ApiConfig";

// API 서버 호스트가 정의되어 있는지 확인
if (!API_SERVER_HOST) {
  console.error("API_SERVER_HOST is not defined in ApiConfig");
}

const host = `${API_SERVER_HOST}/api/member`;

// 로그인
export const loginPost = async (id, password) => {
    const response = await axios.post(
        `${host}/login`,
        { id, password },
        {
            withCredentials: true,
        }
    );
    return response.data;
};

// 로그아웃
export const logoutPost = async () => {
    const response = await axiosInstance.post(`/member/logout`);
    return response.data;
};

// 회원가입
export const joinPost = async (requestData) => {
  try {
    const response = await axios.post(`${host}/join`, {
      email: requestData.email,
      password: requestData.password,
      name: requestData.name,
    });
    return {
      success: true,
      ...response.data
    };
  } catch (error) {
    console.error("회원가입 API 오류:", error);
    throw error;
  }
};

export const checkEmailDuplicate = async (email) => {
  try {
    if (!API_SERVER_HOST) {
      throw new Error("API_SERVER_HOST is not configured");
    }
    
    const response = await axios.get(`${host}/check-email?email=${encodeURIComponent(email)}`);
    return response.data;
  } catch (error) {
    console.error("이메일 중복 체크 오류:", error);
    throw error;
  }
}; 
