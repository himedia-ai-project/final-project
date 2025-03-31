import os
from dotenv import load_dotenv

load_dotenv()

PGVECTOR_CONNECTION_STRING = os.getenv(
    "PGVECTOR_CONNECTION_STRING",
    "postgresql+psycopg2://myuser:mypassword@db:5432/mydb"
)
