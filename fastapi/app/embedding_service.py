from langchain.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.embeddings import HuggingFaceEmbeddings
from langchain.vectorstores.pgvector import PGVector
from langchain.schema import Document
from app.config import PGVECTOR_CONNECTION_STRING

# ✅ LangChain의 PGVector 연결
embedding_model = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
vectorstore = PGVector(connection_string=PGVECTOR_CONNECTION_STRING, embedding_function=embedding_model)

def process_pdf(file_path: str):
    loader = PyPDFLoader(file_path)
    pages = loader.load()

    # ✅ 문장 단위로 분할 (각 페이지별로 처리)
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)

    documents = []
    for page in pages:
        # 각 페이지에서 바로 분할 적용 (텍스트 합치기 X)
        text_chunks = text_splitter.split_text(page.page_content)
        
        # LangChain 문서 객체로 변환
        docs = [Document(page_content=chunk, metadata={"source": file_path}) for chunk in text_chunks]
        documents.extend(docs)

    # ✅ PGVector에 저장
    vectorstore.add_documents(documents)

    return len(documents)  # 저장된 문서 개수 반환
