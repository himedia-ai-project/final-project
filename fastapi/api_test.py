from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import PyMuPDFLoader
from langchain_openai import OpenAIEmbeddings
import os
import tempfile
import shutil
from dotenv import load_dotenv
import traceback

load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# 실행
app = FastAPI()

embeddings = OpenAIEmbeddings(openai_api_key=OPENAI_API_KEY)

@app.post("/upload_pdf/")
async def upload_pdf(file: UploadFile = File(...)):
    try:
        # 임시 파일 저장 (Windows-safe)
        temp_dir = tempfile.gettempdir()
        temp_path = os.path.join(temp_dir, file.filename)

        with open(temp_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # 문서 로드 및 텍스트 분할
        loader = PyMuPDFLoader(temp_path)
        docs = loader.load()

        text_splitter = RecursiveCharacterTextSplitter(chunk_size=210, chunk_overlap=50)
        split_documents = text_splitter.split_documents(docs)

        # 예외 처리: 비어 있는 문서
        if not split_documents:
            return JSONResponse(content={"message": "PDF에 유효한 텍스트가 없습니다."}, status_code=400)

        # 첫 chunk 임베딩 추출
        first_chunk = split_documents[0]
        vector = embeddings.embed_documents([first_chunk.page_content])[0]

        # 임시 파일 삭제
        os.remove(temp_path)

        # 벡터 응답
        return JSONResponse(content={
            "message": "임베딩 성공",
            "embedding": vector
        })

    except Exception as e:
        # 예외 발생 시, 상세한 오류 메시지와 traceback 반환
        error_message = f"오류 발생: {str(e)}\n{traceback.format_exc()}"
        return JSONResponse(content={"message": error_message}, status_code=500)
