# 🗞️ Newsfeed Application

개인화된 뉴스 피드 서비스를 제공하는 Spring Boot 기반의 백엔드 애플리케이션입니다.

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [설치 및 실행](#-설치-및-실행)
- [API 문서](#-api-문서)
- [프로젝트 구조](#-프로젝트-구조)
- [설정](#-설정)
- [개발 가이드](#-개발-가이드)

## 🎯 프로젝트 개요

Newsfeed Application은 사용자가 관심 있는 키워드를 구독하고, 다양한 뉴스 플랫폼에서 관련 뉴스를 자동으로 수집하여 개인화된 뉴스 피드를 제공하는 서비스입니다.

### 핵심 가치
- **개인화**: 사용자 맞춤형 뉴스 큐레이션
- **실시간성**: 자동화된 뉴스 수집 및 업데이트
- **다양성**: 여러 뉴스 플랫폼 지원 (네이버, 구글, 다음)
- **확장성**: 마이크로서비스 아키텍처 기반 설계

## ✨ 주요 기능

### 🔐 사용자 관리
- 회원가입 및 로그인
- JWT 기반 인증 시스템
- Refresh Token을 통한 보안 강화

### 🏷️ 키워드 구독 시스템
- 관심 키워드 구독/구독 해제
- 키워드별 활성화/비활성화 토글
- 실시간 키워드 기반 뉴스 수집

### 📰 뉴스 수집 엔진
- **다중 플랫폼 지원**: 네이버, 구글 뉴스, 다음
- **Selenium 기반 크롤링**: 동적 콘텐츠 처리
- **비동기 처리**: CompletableFuture를 활용한 병렬 크롤링
- **자동 스케줄링**: 주기적 뉴스 업데이트

### 🎯 개인화 뉴스 피드
- 사용자 구독 키워드 기반 뉴스 필터링
- 선호 플랫폼별 뉴스 제공
- 페이지네이션 지원
- 읽음 상태 관리

### 🔧 시스템 기능
- **도메인 이벤트**: Event-Driven Architecture
- **성능 최적화**: JPA 배치 처리, 커넥션 풀 최적화
- **메모리 관리**: G1GC 사용, 힙 덤프 자동 생성

## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot 3.5.4**: 메인 프레임워크
- **Java 17**: 프로그래밍 언어
- **Spring Security**: 인증 및 보안
- **Spring Data JPA**: 데이터 접근 계층

### Database
- **MySQL**: 메인 데이터베이스
- **HikariCP**: 커넥션 풀

### 외부 라이브러리
- **JWT (jsonwebtoken 0.12.3)**: 토큰 기반 인증
- **Selenium 4.15.0**: 웹 크롤링
- **WebDriverManager 5.6.2**: 브라우저 드라이버 자동 관리
- **Jsoup 1.17.2**: HTML 파싱
- **Lombok**: 코드 간소화

### Build & Test
- **Gradle**: 빌드 도구
- **JUnit 5**: 테스트 프레임워크

## 🏗️ 아키텍처

### Clean Architecture 기반 설계

```
src/main/java/com/suman/newsfeed/
├── application/          # 애플리케이션 서비스 계층
│   ├── usecase/         # 비즈니스 로직 구현
│   └── event/           # 도메인 이벤트 핸들러
├── domain/              # 도메인 계층 (핵심 비즈니스 로직)
│   ├── news/           # 뉴스 도메인
│   ├── user/           # 사용자 도메인
│   ├── userNewsFeed/   # 사용자 뉴스피드 도메인
│   └── shared/         # 공통 도메인 요소
├── infrastructure/      # 인프라스트럭처 계층
│   ├── database/       # 데이터베이스 구현
│   ├── external/       # 외부 서비스 연동
│   ├── security/       # 보안 구현
│   └── scheduler/      # 스케줄링
├── presentation/        # 프레젠테이션 계층
│   ├── controller/     # REST API 컨트롤러
│   └── dto/           # 데이터 전송 객체
└── config/             # 설정 클래스
```

### 주요 디자인 패턴
- **Strategy Pattern**: 다중 뉴스 플랫폼 크롤링
- **Repository Pattern**: 데이터 접근 추상화
- **Event-Driven Architecture**: 도메인 이벤트 기반 처리
- **Use Case Pattern**: 비즈니스 로직 캡슐화

## 🚀 설치 및 실행

### 사전 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Chrome 브라우저 (Selenium 크롤링용)

### 1. 저장소 클론
```bash
git clone <repository-url>
cd newsfeed-backend
```

### 2. 데이터베이스 설정
```sql
CREATE DATABASE news_feed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'newsfeed_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON news_feed.* TO 'newsfeed_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 애플리케이션 설정
`src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보를 수정하세요:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/news_feed?useUnicode=true&characterEncoding=UTF-8
    username: newsfeed_user
    password: your_password
```

### 4. 빌드 및 실행
```bash
# Gradle 빌드
./gradlew build

# 애플리케이션 실행 (최적화된 JVM 옵션 포함)
./start.sh

# 또는 직접 실행
./gradlew bootRun
```

### 5. 애플리케이션 확인
- 서버 주소: `http://localhost:8080`
- 헬스 체크: `http://localhost:8080/actuator/health` (설정 시)

## 📚 API 문서

### 인증 API
```http
POST /api/users/register    # 회원가입
POST /api/users/login       # 로그인
```

### 키워드 관리 API
```http
POST /api/user-keywords/subscribe           # 키워드 구독
PUT  /api/user-keywords/active/{keywordId}  # 키워드 활성화/비활성화
GET  /api/user-keywords/my-keywords         # 내 키워드 목록
```

### 뉴스 플랫폼 API
```http
POST /api/user-news-platforms/subscribe     # 플랫폼 구독
GET  /api/user-news-platforms/my-platforms  # 내 플랫폼 목록
```

### 뉴스 피드 API
```http
GET /api/news/personalized?page=0&size=20   # 개인화 뉴스 조회
```

### 요청/응답 예시

#### 회원가입
```json
POST /api/users/register
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "사용자"
}
```

#### 키워드 구독
```json
POST /api/user-keywords/subscribe
Authorization: Bearer {JWT_TOKEN}
{
  "keywords": ["AI", "스타트업", "기술"]
}
```

## 📁 프로젝트 구조

### 핵심 도메인 모델

#### News (뉴스)
- 뉴스 제목, 내용, URL, 플랫폼 정보
- 키워드와의 연관관계
- 생성 시 도메인 이벤트 발생

#### User (사용자)
- 사용자 기본 정보 및 인증 정보
- 구독 키워드 및 플랫폼 관리
- JWT 토큰 관리

#### UserNewsFeed (사용자 뉴스피드)
- 사용자별 개인화된 뉴스 목록
- 읽음 상태 관리
- 키워드 기반 필터링

### 크롤링 시스템

#### 지원 플랫폼
- **NAVER**: 네이버 뉴스
- **GOOGLE**: 구글 뉴스  
- **DAUM**: 다음 뉴스

#### 크롤링 전략
- Strategy Pattern으로 플랫폼별 크롤링 로직 분리
- Selenium WebDriver를 통한 동적 콘텐츠 처리
- 병렬 처리로 성능 최적화

## ⚙️ 설정

### JVM 최적화 설정
```bash
# start.sh에 포함된 JVM 옵션
-Xmx2g                          # 최대 힙 메모리 2GB
-Xms1g                          # 초기 힙 메모리 1GB
-XX:+UseG1GC                    # G1 가비지 컬렉터 사용
-XX:MaxGCPauseMillis=200        # GC 일시정지 시간 최대 200ms
-XX:+HeapDumpOnOutOfMemoryError # OOM 시 힙 덤프 생성
```

### 데이터베이스 최적화
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 25                    # 배치 처리 크기
          connection:
            provider_disables_autocommit: true
          cache:
            use_second_level_cache: true    # 2차 캐시 사용
            use_query_cache: true           # 쿼리 캐시 사용
```

### 크롤링 설정
```yaml
naver:
  api:
    enabled: true      # 네이버 크롤링 활성화
    timeout: 30000     # 타임아웃 30초
    headless: true     # 헤드리스 모드

google:
  news:
    enabled: false     # 구글 뉴스 비활성화
    
daum:
  enabled: false       # 다음 뉴스 비활성화
```

## 🔧 개발 가이드

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "NewsCollectionServiceTest"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

### 코드 스타일
- **Lombok** 사용으로 보일러플레이트 코드 최소화
- **도메인 주도 설계(DDD)** 원칙 준수
- **Clean Architecture** 계층 분리
- **SOLID 원칙** 적용

### 주요 개발 규칙
1. **도메인 로직**은 `domain` 패키지에 위치
2. **비즈니스 로직**은 `application/usecase`에 구현
3. **외부 의존성**은 `infrastructure`에서 처리
4. **API 엔드포인트**는 `presentation/controller`에 정의
5. **도메인 이벤트**를 통한 느슨한 결합 유지

### 새로운 뉴스 플랫폼 추가하기

1. `NewsPlatform` enum에 새 플랫폼 추가
2. `CrawlerStrategy` 인터페이스 구현
3. `application.yml`에 설정 추가
4. Spring Bean으로 등록

```java
@Component
public class NewPlatformCrawlerStrategy implements CrawlerStrategy {
    @Override
    public NewsPlatform getPlatform() {
        return NewsPlatform.NEW_PLATFORM;
    }
    
    @Override
    public List<NewsDataDto> crawlNews(String keyword, Long pageNumber, int pageSize) {
        // 크롤링 로직 구현
    }
}
```
