import axiosInstance from './axiosInstance';

export const favoriteList = async (userId) => {
    try {
        const response = await axiosInstance.get(`/favorite/member`, {
            params: { memberId: userId }
        });
        return response.data;
    } catch (error) {
        console.error("즐겨찾기 목록 조회 실패:", error);
        return [];
    }
};

export const addFavorite = async (memberId, productId) => {
    try {
        const response = await axiosInstance.post(`/favorite/add`, {
            memberId,
            productId
        });
        return response.data;
    } catch (error) {
        console.error("즐겨찾기 추가 실패:", error);
        throw error;
    }
};

export const deleteFavorite = async (memberId, productId) => {
    try {
        const response = await axiosInstance.delete(`/favorite/delete`, {
            data: {
                memberId,
                productId
            }
        });
        return response.data;
    } catch (error) {
        console.error("즐겨찾기 삭제 실패:", error);
        throw error;
    }
};