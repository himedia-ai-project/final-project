import axiosInstance from './axiosInstance';
import store from '../redux/Store';
import { updateRecents } from '../redux/LoginSlice';
import { showToastMessage } from '../redux/NotificationSlice';

export const savePdf = async (name, categoryId, file, imageFile = null) => {
    try {
        const formData = new FormData();
        formData.append('name', name);
        formData.append('categoryId', categoryId);
        formData.append('file', file);
        formData.append('image', imageFile || null);
        
        const { id: memberId } = store.getState().login || {};
        if (memberId) {
            formData.append('memberId', memberId);
        } else {
            console.error("로그인된 사용자 정보가 없습니다.");
            throw new Error("로그인이 필요합니다.");
        }

        const response = await axiosInstance.post('/pdf/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        
        if (response.data.status === "success") {
            if (memberId) {
                store.dispatch(showToastMessage(`${name} 제품이 성공적으로 등록되었습니다.`));
            }
        }
        
        return response.data;
    } catch (error) {
        // console.error("PDF 저장 실패:", error);
        throw error;
    }
};

// 로그인 상태에 따라 memberId 추가 여부 결정하는 함수
const getMemberIdParam = () => {
    const state = store.getState();
    return state.login?.id ? { memberId: state.login.id } : {};
};
  
// 1. 제품 선택 시 호출
export const selectProduct = async (productId) => {
    try {
        const body = {
            query: "initial_connection",
            productId: productId,
            top_k: 3,
            newSession: true,
            ...getMemberIdParam()
        };
        
        const response = await axiosInstance.post('/chat', body);
        return response.data;
    } catch (error) {
        console.error("제품 선택 실패:", error);
        throw error;
    }
};
  
// 2. 대화 계속하기
export const continueChat = async (query, productId, sessionId, top_k = 3) => {
    try {
        const body = {
            query: query,
            productId: productId,
            sessionId: sessionId,
            top_k: top_k,
            newSession: false,
            ...getMemberIdParam()
        };
        
        const response = await axiosInstance.post('/chat', body);
        return response.data;
    } catch (error) {
        console.error("대화 계속 실패:", error);
        throw error;
    }
};
  
// 3. 새 채팅 시작
export const createNewChat = async (query, productId, top_k = 3) => {
    try {
            const body = {
            query: query,
            productId: productId,
            top_k: top_k,
            newSession: true,
            ...getMemberIdParam()
        };
        
        const response = await axiosInstance.post('/chat', body);
        return response.data;
    } catch (error) {
        console.error("새 채팅 생성 실패:", error);
        throw error;
    }
};

// 4. 채팅방 나가기
export const endChat = async (sessionId, skipSave = false) => {
    try {
        await axiosInstance.delete(`/chat/${sessionId}?skipSave=${skipSave}`);
        return true;
    } catch (error) {
        console.error("세션 종료 실패:", error);
        throw error;
    }
};

// 5. 이전 대화 내역 가져오기
export const getHistories = async (productId) => {
    try {
        const { id: memberId } = store.getState().login || {};
        
        if (!memberId) {
            return [];
        }
        
        const response = await axiosInstance.get('/history', {
            params: {
                memberId: memberId,
                productId: productId
            }
        });
        return response.data;
    } catch (error) {
        console.error("대화 내역 가져오기 실패:", error);
        return [];
    }
};

// 6. 최근 사용한 제품 목록 가져오기
export const getRecentProducts = async () => {
    try {
        const { id: memberId } = store.getState().login || {};
        
        if (!memberId) {
            return [];
        }
        
        const response = await axiosInstance.get('/history/recent', {
            params: {
                memberId: memberId
            }
        });
        
        store.dispatch(updateRecents(response.data));
        
        return response.data;
    } catch (error) {
        console.error("최근 사용 제품 목록 가져오기 실패:", error);
        return [];
    }
};
