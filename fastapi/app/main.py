from fastapi import FastAPI, UploadFile, File
from app.utils import save_uploaded_file
from app.embedding_service import process_pdf

app = FastAPI()

#   """
#     ✅ PDF 파일 업로드 후:
#         1. PyPDFLoader로 텍스트 추출
#         2. RecursiveCharacterTextSplitter로 문장 분할
#         3. PGVector에 임베딩 저장
#   """

@app.post("/upload-pdf/")
async def upload_pdf(file: UploadFile = File(...)):
  
    # PDF 저장
    file_path = save_uploaded_file(file)

    # PDF 처리 및 벡터 저장
    num_chunks = process_pdf(file_path)

    return {"filename": file.filename, "message": "Embedding stored successfully", "chunks": num_chunks}

@app.get("/")
def read_root():
    return {"message": "FastAPI + LangChain + PGVector Service is running!"}
