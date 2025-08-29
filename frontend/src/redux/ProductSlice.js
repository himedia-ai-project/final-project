import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  products: [],
  loading: false,
  error: null,
  categories: [
    { id: "1", name: "텔레비전 (TV)", type: "가전제품", searchKeyword: "TV" },
    { id: "2", name: "냉장고", type: "가전제품", searchKeyword: "냉장고" },
    { id: "3", name: "세탁기", type: "가전제품", searchKeyword: "세탁기" },
    { id: "4", name: "전자레인지", type: "가전제품", searchKeyword: "전자레인지" },
    { id: "5", name: "에어컨", type: "가전제품", searchKeyword: "에어컨" },
    { id: "6", name: "청소기 (유선/무선)", type: "가전제품", searchKeyword: "청소기" },
    { id: "7", name: "정수기", type: "가전제품", searchKeyword: "정수기" },
    { id: "8", name: "커피머신", type: "가전제품", searchKeyword: "커피머신" },
    { id: "9", name: "전기밥솥", type: "가전제품", searchKeyword: "전기밥솥" },
    { id: "10", name: "스마트폰", type: "개인용 전자기기", searchKeyword: "스마트폰" },
    { id: "11", name: "태블릿", type: "개인용 전자기기", searchKeyword: "태블릿" },
    { id: "12", name: "노트북", type: "개인용 전자기기", searchKeyword: "노트북" },
    { id: "13", name: "스마트워치", type: "개인용 전자기기", searchKeyword: "스마트워치" },
    { id: "14", name: "이어폰/헤드폰 (유선/무선)", type: "개인용 전자기기", searchKeyword: "이어폰" },
    { id: "15", name: "전자책 리더기", type: "개인용 전자기기", searchKeyword: "전자책" }
  ]
};

const productSlice = createSlice({
  name: "product",
  initialState,
  reducers: {
    setProducts: (state, action) => {
      state.products = action.payload;
    },
    addProduct: (state, action) => {
      state.products.push(action.payload);
    }
  },
});

export const { setProducts, addProduct } = productSlice.actions;
export default productSlice.reducer;
