import os
from dotenv import load_dotenv

load_dotenv()

PGVECTOR_CONNECTION_STRING = os.getenv(
    "PGVECTOR_CONNECTION_STRING",
    "postgresql+psycopg2://postgres:1234@db:5432/test"
)
