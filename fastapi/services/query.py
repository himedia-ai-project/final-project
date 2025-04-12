import re
from typing import Dict, Any
from services.storage import search_documents
from services.llm import create_answer_with_gemini

async def search_documents_with_answer(query_text: str, collection_name: str, top_k: int = 3) -> Dict[str, Any]:
    """벡터 저장소에서 유사 문서를 검색하고 답변을 생성"""
    try:
        # 벡터 저장소에서 유사 문서 검색
        search_results = await search_documents(query_text, collection_name, top_k)
        
        # 문서 내용 정규화
        normalized_docs = []
        for doc in search_results["documents"]:
            content = re.sub(r'\s+', ' ', doc["content"]).strip()
            normalized_docs.append({
                "content": content,
                "metadata": doc["metadata"],
                "score": doc["score"]
            })
        
        # Gemini로 답변 생성
        answer = await create_answer_with_gemini(query_text, normalized_docs)
        
        return {
            "query": query_text,
            "documents": normalized_docs,
            "answer": answer
        }
    except Exception as e:
        raise Exception(f"문서 검색 및 답변 생성 중 오류 발생: {str(e)}")
