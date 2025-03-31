import os
from pathlib import Path
from fastapi import UploadFile

# 파일을 저장할 디렉터리 지정 (디렉터리가 없다면 생성)
UPLOAD_DIRECTORY = Path("./uploaded_files")
UPLOAD_DIRECTORY.mkdir(parents=True, exist_ok=True)

def save_uploaded_file(file: UploadFile) -> str:
    
    file_location = UPLOAD_DIRECTORY / file.filename
    
    with open(file_location, "wb") as buffer:
        buffer.write(file.file.read())

    return str(file_location)
