# HomeFit Backend

공공데이터와 사용자 금융 프로필을 결합해 주거 탐색, LH 공공임대 공고 추천, 양도 게시판, 생활권 분석, AI 상담을 제공하는 Spring Boot 기반 백엔드입니다.

## 핵심 기능

- 주택 실거래가 조회: 아파트, 오피스텔, 원룸 실거래 데이터를 지역, 건물명, 거래 유형 기준으로 조회합니다.
- LH 공공임대 공고: LH Open API 공고 목록, 상세, 공급 정보를 수집하고 캐시합니다.
- 맞춤 LH 추천: 회원 금융 프로필, 관심 지역, 공고 유형, 접수 상태를 바탕으로 추천 점수를 계산합니다.
- 관심 공고 알림 메일: 관심 LH 공고의 접수 마감 D-3, D-2, D-1, D-Day 이벤트와 추천 공고 메일을 발송합니다.
- 생활권 분석: 상권, 대중교통, 교통 위험 정보를 결합해 입지 점수를 제공합니다.
- 양도 게시판: 양도글 CRUD, 이미지 업로드, 관심 등록, 댓글형 소통 기능을 제공합니다.
- AI 상담: Spring AI 기반 OpenAI 호환 API와 RAG 문서 인덱스를 사용해 서비스 데이터를 바탕으로 응답합니다.
- 인증/인가: JWT 로그인, OAuth2 로그인, Spring Security 필터 체인을 사용합니다.
- 파일 저장: 로컬 업로드와 AWS S3 업로드 구현을 분리해 운영 환경별로 선택할 수 있습니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.0.6, Spring Web MVC, Spring Security, Spring Batch, Spring Validation |
| Persistence | MyBatis, Spring JDBC, MySQL, H2 Test DB |
| Authentication | JWT, OAuth2 Client, Kakao/Naver/Google OAuth |
| API Docs | springdoc-openapi, Swagger UI |
| AI | Spring AI 2.0.0-RC2, OpenAI compatible Chat/Embedding API, Vector Store Advisor |
| External APIs | LH Open API, 국토교통부 실거래가 API, Kakao Local API, ITS 교통 API, 버스 정류장 API, 금융감독원 금융상품 API |
| Mail | Spring Boot Mail, JavaMailSender, SMTP |
| Storage | AWS SDK S3, local multipart upload |
| Build/Test | Maven Wrapper, JUnit, Spring Security Test, Spring Boot Test |

## 시스템 구조

```text
Vue SPA
  |
  | HTTPS / JSON / multipart
  v
Spring Security
  |
  +-- JWT Filter
  +-- OAuth2 Login Handler
  v
REST Controllers
  |
  v
Services
  |
  +-- Open API Clients
  +-- Recommendation Engine
  +-- Email Scheduler
  +-- AI RAG Service
  v
MyBatis Mappers
  |
  v
MySQL / H2
```

## 주요 패키지

| 패키지 | 역할 |
| --- | --- |
| `com.happyhome.security` | JWT 발급/검증, OAuth2 성공 처리, 인증 필터 |
| `com.happyhome.member` | 회원, 마이데이터 금융 프로필, 메일 수신 동의 |
| `com.happyhome.house` | 아파트 실거래가 조회 |
| `com.happyhome.property` | 오피스텔/원룸 매매 및 전월세 실거래 조회 |
| `com.happyhome.rental` | LH 공공임대 공고 조회, 상세 캐시, 즐겨찾기 |
| `com.happyhome.rental.recommendation` | LH 공고 추천 점수 계산 |
| `com.happyhome.rental.email` | 관심 공고 마감 알림, 추천 공고 메일, 발송 로그 |
| `com.happyhome.transfer` | 양도 게시판, 이미지 업로드, 관심 등록 |
| `com.happyhome.transfer.comment` | 양도글 댓글 API |
| `com.happyhome.analysis` | 생활권 분석 스냅샷, 점수 계산 |
| `com.happyhome.commercial` | 상권 요약 |
| `com.happyhome.transport` | 버스/지하철 접근성 데이터 |
| `com.happyhome.traffic` | ITS 교통 위험 분석 |
| `com.happyhome.loan` | 대출 상품 조회, LTV/DSR 기반 대출 가능성 분석 |
| `com.happyhome.ai` | Spring AI 기반 상담 API와 RAG 문서 생성 |
| `com.happyhome.batch` | LH 공고, 버스 정류장, 금융상품 수집 배치 |
| `com.happyhome.openapi` | 외부 공공 API 클라이언트와 JSON 파서 |

## 주요 API

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/members/login` | 일반 로그인 및 JWT 발급 |
| `GET` | `/api/members/me` | 현재 회원 조회 |
| `PUT` | `/api/members/me` | 회원 정보 및 LH 메일 수신 동의 수정 |
| `GET` | `/api/houses` | 아파트 실거래가 조회 |
| `GET` | `/api/properties` | 오피스텔/원룸 실거래가 조회 |
| `GET` | `/api/rentals` | LH 공고 목록 조회 |
| `GET` | `/api/rentals/{noticeId}` | LH 공고 상세 및 공급 정보 조회 |
| `POST` | `/api/rentals/{noticeId}/favorite/toggle` | 관심 LH 공고 토글 |
| `GET` | `/api/rentals/recommendations` | 맞춤 LH 추천 공고 조회 |
| `POST` | `/api/rentals/favorites/emails/send` | 관심 LH 공고 마감 알림 수동 발송 |
| `POST` | `/api/rentals/recommendations/emails/send` | 맞춤 LH 추천 메일 수동 발송 |
| `GET` | `/api/transfers` | 양도 게시판 목록 |
| `POST` | `/api/transfers` | 양도글 작성, 이미지 multipart 업로드 |
| `GET` | `/api/transfers/{transferId}/comments` | 양도글 댓글 조회 |
| `POST` | `/api/transfers/{transferId}/comments` | 양도글 댓글 작성 |
| `POST` | `/api/analysis` | 생활권 분석 |
| `POST` | `/api/loans/property-analysis` | 부동산 대출 가능성 분석 |
| `POST` | `/api/ai/chat` | AI 상담 |

Swagger UI는 실행 후 `/swagger-ui.html`에서 확인할 수 있습니다.

## 데이터베이스

주요 테이블은 `src/main/resources/schema.sql`에 정의되어 있습니다.

| 테이블 | 설명 |
| --- | --- |
| `members` | 회원 및 LH 메일 수신 동의 |
| `member_financial_profiles` | 마이데이터 기반 금융 프로필 |
| `houseinfos`, `housedeals` | 아파트 기본 정보와 실거래가 |
| `rental_notice_cache` | LH 공고 목록 캐시 |
| `lh_notice_details`, `lh_notice_supplies` | LH 공고 상세와 공급 정보 캐시 |
| `favorite_rental_notices` | 관심 LH 공고 |
| `rental_notice_email_logs` | 공고별 이벤트 메일 중복 발송 방지 로그 |
| `transfers`, `transfer_images`, `transfer_comments` | 양도 게시판, 이미지, 댓글 |
| `loan_products`, `loan_rate_options` | 금융상품 데이터 |
| `analysis_snapshot` | 생활권 분석 결과 스냅샷 |
| `bus_city_codes`, `bus_stops` | 버스 정류장 데이터 |

## 배치와 스케줄링

- LH 공고 수집: `batch.lh.enabled`, `batch.lh.cron`
- 버스 정류장 수집: `batch.bus.enabled`, `batch.bus.cron`
- 관심 LH 공고 메일: `rental.notice.email.enabled`, `rental.notice.email.cron`
- 중복 메일 방지: `rental_notice_email_logs`의 `user_id + notice_id + event_type` 기준으로 발송 여부를 확인합니다.

## 환경 변수

실제 값은 `.env`에 두고, 공유용 예시는 `.env.example`을 사용합니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/ssafyhome?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=ssafy
DB_PASSWORD=ssafy

FRONTEND_ORIGIN=http://localhost:5173
FRONTEND_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080

JWT_SECRET=change-me

OPENAPI_DATA_SERVICE_KEY=
OPENAPI_ITS_API_KEY=
OPENAPI_KAKAO_REST_KEY=
OPENAPI_BUS_SERVICE_KEY=
OPENAPI_FINLIFE_AUTH=

OPENAPI_CHAT_GPT=
OPENAPI_CHAT_GPT_BASE_URL=https://api.openai.com
OPENAPI_CHAT_GPT_MODEL=gpt-4o-mini
OPENAPI_CHAT_GPT_EMBEDDING_MODEL=text-embedding-3-small

OAUTH_KAKAO_CLIENT_ID=
OAUTH_KAKAO_CLIENT_SECRET=
OAUTH_NAVER_CLIENT_ID=
OAUTH_NAVER_CLIENT_SECRET=
OAUTH_GOOGLE_CLIENT_ID=
OAUTH_GOOGLE_CLIENT_SECRET=

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true

S3_ACCESS_KEY=
S3_SECRET_KEY=
S3_BUCKET=
S3_REGION=ap-northeast-2
S3_PUBLIC_BASE_URL=
```

## 실행 방법

### 1. 데이터베이스 준비

```sql
CREATE DATABASE ssafyhome
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. 환경 변수 설정

```powershell
Copy-Item .env.example .env
```

필요한 DB, 공공 API, OAuth, SMTP, S3 값을 채웁니다.

### 3. 서버 실행

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

기본 포트는 `8080`입니다. `PORT` 환경 변수로 변경할 수 있습니다.

## 테스트

```powershell
.\mvnw.cmd test
```

테스트는 H2, Spring Boot Test, Spring Security Test를 사용합니다.

## 프론트엔드 연동

프론트엔드는 별도 저장소 `pjt_front`에서 개발합니다.

- 개발 서버: `http://127.0.0.1:5173`
- API 기본 주소: `VITE_BACKEND_ORIGIN`
- 백엔드 정적 배포 빌드 출력: `pjt_front`의 `pnpm build:backend`가 `src/main/resources/static/app`으로 Vue SPA를 빌드합니다.

## 프로젝트 특징

- REST API와 SPA 배포를 동시에 고려한 구조입니다.
- 공공 API 실패나 외부 데이터 지연을 줄이기 위해 LH 상세 데이터는 캐시 우선으로 조회합니다.
- 추천, 메일, 즐겨찾기, 금융 프로필을 분리해 기능별 테스트와 확장이 쉽습니다.
- AI 상담은 RAG 문서 생성을 별도 팩토리로 분리해 모델 호출과 데이터 준비 책임을 나눴습니다.
- 양도 게시판 이미지는 저장소 인터페이스를 통해 로컬/S3 구현을 교체할 수 있습니다.
