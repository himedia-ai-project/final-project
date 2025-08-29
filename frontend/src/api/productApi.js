import axiosInstance from './axiosInstance';

export const productList = async () => {
    try {
        const response = await axiosInstance.get('/product/list');
        return response.data;
    } catch (error) {
        console.error("제품 전체조회 실패:", error);
        throw error;
    }
};

export const searchProducts= async (query) => {
    try {
        const response = await axiosInstance.get(`/product/search?query=${query}`);
        return response.data;
    } catch (error) {
        console.error("제품 특징기반 검색 실패:", error);
        throw error;
    }
};