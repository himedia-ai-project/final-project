FROM node:18-alpine AS build

WORKDIR /app

# 1. 캐시 활용을 위해 의존성만 먼저 복사
COPY package*.json ./
RUN npm install --legacy-peer-deps

# 2. 전체 소스 복사
COPY . .

# 3. sourcemap 비활성화로 빌드 속도 향상
ENV GENERATE_SOURCEMAP=false

# 4. 빌드
RUN npm run build

# 5. 정적 파일 서버 설치
RUN npm install -g serve

EXPOSE 3000

CMD ["serve", "-s", "build", "-l", "3000"]
