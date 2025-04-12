# routes/search.py
from fastapi import APIRouter, HTTPException
from services.query import search_documents_with_answer
from models.schema import SearchQuery, SearchResponse

router = APIRouter()

@router.post("/chat/ask", response_model=SearchResponse)
async def search_endpoint(query: SearchQuery):
    """벡터 저장소에서 유사 문서를 검색하고 답변을 생성"""
    try:
        if not query.collection_name:
            raise HTTPException(status_code=400, detail="Collection name is required")
            
        results = await search_documents_with_answer(
            query_text=query.query,
            collection_name=query.collection_name,
            top_k=query.top_k
        )
        return results
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# @router.post("/upload")
# async def upload_endpoint(file: UploadFile = File(...), collection_name: str = Form("langchain")):
#     """PDF 파일을 업로드하고 처리"""
#     try:
#         # 파일 내용 읽기
#         file_content = await file.read()
        
#         # PDF 처리
#         result = await process_pdf(
#             file_content=file_content,
#             file_name=file.filename,
#             collection_name=collection_name
#         )
        
#         return result
        
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"파일 처리 중 오류 발생: {str(e)}")

# @router.get("/collections")
# async def list_collections_endpoint():
#     """컬렉션 목록 조회"""
#     try:
#         collections = await list_collections()
#         return collections
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"컬렉션 목록 조회 중 오류 발생: {str(e)}")
