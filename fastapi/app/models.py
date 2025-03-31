from sqlalchemy import Column, String
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.dialects.postgresql import BYTEA

Base = declarative_base()

class DocumentEmbedding(Base):
    __tablename__ = "document_embeddings"

    id = Column(String, primary_key=True, index=True)
    filename = Column(String, index=True)
    embedding = Column(BYTEA)  # 임베딩 데이터를 BYTEA로 저장
