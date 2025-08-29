# 빌드용 JDK 이미지
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Gradle 래퍼 파일 복사
COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

# 빌드에 필요한 설정 파일 복사
COPY build.gradle .
COPY settings.gradle .

# 소스 디렉토리 복사
COPY src src

# 의존성 다운로드 및 애플리케이션 빌드
RUN ./gradlew build -x test

# 런타임용 JRE 이미지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
