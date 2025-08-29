# MANULO  

**LLM 기반 전자제품 설명서 질의응답 시스템**  

사용자가 업로드한 전자제품 설명서를 벡터화하여 저장하고,  
자연어 질문에 대해 **AI가 실시간으로 응답**하는 챗봇 서비스입니다.  

단순 키워드 검색을 넘어 **문맥 기반 답변**을 제공하여,  
제품 이해도를 높이고 활용 경험을 풍부하게 만듭니다.  

---

## ✨ 주요 기능  
- **설명서 업로드**: PDF 업로드 → 텍스트 청크 분리 → OpenAI 임베딩 저장  
- **대화형 질의응답**: Redis 세션 컨텍스트 + pgvector 유사도 검색 + Gemini 응답 생성  
- **제품 검색**: 제품명 키워드 검색 + 특징 기반 유사 제품 탐색  
- **사용자 기능**: 회원가입/로그인 (JWT 기반), 즐겨찾기, 알림  

---

## 🛠 기술 스택
- **Frontend**: React
- **Backend**: Java, Spring Boot, Spring Security, JPA
- **Database**: PostgreSQL, VectorDB((pgvector)
- **AI**: OpenAI Embeddings, Google Gemini
- **Infra**: AWS EC2, S3, Docker, GitHub Actions

---

## ⚙️ 실행 방법

### 1) 요구 사항
- Docker & Docker Compose
- OpenAI API Key, Gemini API Key
- Google OAuth Client ID/Secret

---

### 2) 환경 변수 설정

#### 📂 backend/.env
```env
# Database
DB_URL=jdbc:postgresql://db:5432/manulo
DB_USERNAME=manulo
DB_PASSWORD=manulo

# API Keys
OPENAI_API_KEY=sk-...
GEMINI_API_KEY=...

# JWT
JWT_SECRET=change-me
JWT_EXPIRATION=3600000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# AWS S3
AWS_ACCESS_KEY=...
AWS_SECRET_KEY=...
AWS_S3_BUCKET_NAME=your-bucket
AWS_REGION=ap-northeast-2

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Google OAuth
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
GOOGLE_REDIRECT_URL=http://localhost:3000/oauth/callback
```

📂 frontend/.env
```env
코드 복사
REACT_APP_API_URL=http://localhost:8080
REACT_APP_FRONT_URL=http://localhost:3000
```

---

### 3) 실행 (Docker Compose)
```bash
docker compose up -d --build
```
- Backend: http://localhost:8080
- Frontend: http://localhost:3000

---

## 📄 추가 문서  

- [프로젝트 소개 (Wiki)](../../wiki/01_프로젝트-소개)

---
