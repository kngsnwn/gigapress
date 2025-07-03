# Domain/Schema Service

## Overview
Domain/Schema Service는 GigaPress 시스템의 핵심 서비스로, 자연어 요구사항을 분석하여 도메인 모델과 데이터베이스 스키마를 자동으로 생성합니다.

## 주요 기능
- **Requirements Analysis**: 자연어 요구사항 분석
- **Domain Model Generation**: DDD 기반 도메인 모델 생성
- **Database Schema Design**: 최적화된 DB 스키마 설계
- **Entity Relationship Mapping**: JPA 엔티티 및 관계 매핑

## 기술 스택
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- Redis
- Flyway (DB Migration)

## 실행 방법

### Prerequisites
- Java 17+
- Docker & Docker Compose (인프라 서비스용)

### 로컬 실행
```bash
# PostgreSQL 데이터베이스 생성
createdb gigapress_domain

# 애플리케이션 실행
./gradlew bootRun
```

### Docker로 실행
```bash
# 이미지 빌드
docker build -t gigapress/domain-schema-service:latest .

# 컨테이너 실행
docker run -d \
  --name domain-schema-service \
  --network gigapress-network \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  gigapress/domain-schema-service:latest
```

## API Documentation
- Swagger UI: http://localhost:8083/swagger-ui
- OpenAPI Spec: http://localhost:8083/api-docs
