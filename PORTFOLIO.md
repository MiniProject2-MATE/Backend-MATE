# Backend-MATE 포트폴리오 정리

## 1. 프로젝트 한 줄 소개

Backend-MATE는 개발자들이 프로젝트 팀원을 모집하고, 지원하고, 합류한 뒤 프로젝트별 게시판으로 소통할 수 있도록 만든 팀 매칭 백엔드 서비스입니다. 단순 CRUD를 넘어서 회원 인증, JWT 기반 인가, 프로젝트 모집 상태 관리, 지원서 승인/거절, 프로젝트 멤버 관리, 관리자 페이지, 이미지 업로드, 예외 응답 표준화까지 포함한 Spring Boot 기반 REST API 프로젝트입니다.

## 2. 포트폴리오에서 강조할 수 있는 핵심 가치

- 사용자, 프로젝트, 지원서, 프로젝트 멤버, 게시글, 댓글로 이어지는 실제 서비스형 도메인 모델을 설계했습니다.
- JWT Access Token과 Refresh Token을 분리해 인증 흐름을 구현했습니다.
- Spring Security를 이용해 일반 API는 Stateless JWT 방식으로, 관리자 페이지는 Form Login 방식으로 분리했습니다.
- 프로젝트 지원, 승인, 거절, 모집 마감, 재모집 등 상태 기반 비즈니스 로직을 서비스 계층에 구현했습니다.
- Soft Delete를 적용하고, 삭제된 데이터까지 고려한 중복 검증과 관리자 복구 기능을 구현했습니다.
- 전역 예외 처리와 ErrorCode enum을 통해 API 에러 응답을 일관된 형식으로 제공합니다.
- Cloudinary 연동을 통해 프로필 이미지 같은 외부 파일 업로드 기능을 구현했습니다.
- Spring Boot Actuator, Spring Boot Admin Client, 관리자 대시보드로 운영 관점의 관리 기능을 고려했습니다.

## 3. 사용 기술

### Backend

- Java 17
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Thymeleaf

### Database

- MariaDB
- H2 Test Database
- JPA/Hibernate
- Flyway 의존성 포함

### Auth & Security

- JWT
- JJWT 0.12.7
- BCryptPasswordEncoder
- CustomUserDetailsService
- SecurityFilterChain
- CORS 설정

### Infra & Tools

- Maven
- Lombok
- Cloudinary
- Springdoc OpenAPI Swagger UI
- Spring Boot Actuator
- Spring Boot Admin Client

## 4. 도메인 설계 포인트

### User

사용자는 이메일, 비밀번호, 닉네임, 전화번호, 포지션, 기술 스택, 프로필 이미지 정보를 가집니다. 회원가입 시 이메일/전화번호/닉네임 중복 여부를 검증하고, 비밀번호는 BCrypt로 암호화해 저장합니다.

포트폴리오 어필 포인트:

- 개인정보성 데이터 검증 로직 구현
- 비밀번호 평문 저장 방지
- Soft Delete된 사용자까지 포함한 중복 검증
- 기본 프로필 이미지 자동 지정
- 닉네임 미입력 시 이메일 기반 기본 닉네임 생성

### Project

프로젝트는 모집글 역할을 하며 제목, 설명, 카테고리, 모집 인원, 현재 인원, 마감일, 기술 스택, 모집 상태 등을 포함합니다. 프로젝트 생성자는 자동으로 OWNER 역할의 ProjectMember가 됩니다.

포트폴리오 어필 포인트:

- 프로젝트 생성과 동시에 소유자 멤버십 생성
- 모집 상태(RECRUITING, CLOSED, DELETED 등) 기반 로직
- 모집 마감/재모집 기능
- 카테고리와 키워드 기반 프로젝트 목록 조회
- 프로젝트 삭제 시 연관 리소스 Soft Delete 처리

### Application

사용자가 프로젝트에 지원할 때 생성되는 지원서입니다. 지원서는 PENDING, ACCEPTED, REJECTED 같은 상태를 가지며, 방장이 승인하면 ProjectMember로 전환됩니다.

포트폴리오 어필 포인트:

- 프로젝트 방장 본인 지원 방지
- 이미 참여 중인 멤버의 중복 지원 방지
- 중복 지원 방지
- PENDING 상태에서만 지원 취소 가능
- 방장만 승인/거절 가능
- 승인 시 지원서의 포지션 정보를 프로젝트 멤버 정보로 복사
- 승인 후 현재 인원 증가 및 정원 초과 방지

### ProjectMember

프로젝트에 실제로 합류한 사용자를 나타내는 중간 엔티티입니다. User와 Project의 N:M 관계를 해소하며, OWNER/MEMBER 역할과 프로젝트 내 포지션을 관리합니다.

포트폴리오 어필 포인트:

- 단순 다대다 매핑 대신 중간 엔티티를 사용해 역할과 포지션 같은 관계 속성을 관리
- OWNER와 MEMBER 권한을 분리
- 프로젝트 참여 목록 조회 시 멤버 포지션까지 응답에 반영

### BoardPost & Comment

프로젝트별 게시글과 댓글 기능을 제공합니다. 프로젝트 내부 커뮤니케이션을 위한 도메인입니다.

포트폴리오 어필 포인트:

- 프로젝트 단위 게시판 구조
- 게시글/댓글 작성자 권한 검증 가능 구조
- 프로젝트 삭제 시 관련 게시글과 댓글까지 Soft Delete 처리

### AdminLog

관리자 페이지에서 회원 삭제/복구, 프로젝트 삭제/복구 같은 주요 관리 액션을 로그로 남깁니다.

포트폴리오 어필 포인트:

- 운영자 액션 추적
- 로그 최대 100개 유지 로직
- 관리자 대시보드에서 최신 로그 페이징 조회

## 5. 인증 및 인가 구현

### JWT 인증 구조

로그인 성공 시 Access Token과 Refresh Token을 발급합니다. Access Token은 API 요청 인증에 사용하고, Refresh Token은 DB에 저장해 Access Token 재발급에 사용합니다.

강조할 코드 흐름:

1. 사용자가 이메일과 비밀번호로 로그인 요청
2. AuthenticationManager가 사용자 인증
3. JwtTokenProvider가 Access Token과 Refresh Token 생성
4. Refresh Token을 DB에 저장하거나 기존 토큰 갱신
5. 이후 요청은 Authorization 헤더의 Bearer Token으로 인증
6. JwtAuthenticationFilter가 토큰을 검증하고 SecurityContext에 인증 객체 저장

포트폴리오 어필 문장:

> Spring Security와 JWT를 결합해 Stateless 인증 구조를 구현했고, Refresh Token을 DB에 저장해 로그아웃 및 토큰 재발급 흐름을 제어했습니다.

### 관리자 인증 분리

일반 API는 JWT 기반 Stateless 인증을 사용하고, `/admin/**` 경로는 별도의 SecurityFilterChain에서 Form Login을 사용합니다.

포트폴리오 어필 문장:

> 사용자 API와 관리자 페이지의 인증 방식을 분리하기 위해 SecurityFilterChain을 경로 기준으로 나누고, 관리자 페이지에는 세션 기반 Form Login을 적용했습니다.

## 6. 비즈니스 로직에서 강조할 부분

### 프로젝트 생성 시 OWNER 자동 등록

프로젝트를 생성하면 Project 엔티티만 저장하는 것이 아니라, 생성자를 ProjectMember 테이블에 OWNER로 함께 저장합니다. 이 구조 덕분에 프로젝트 참여자 목록에서 방장과 일반 멤버를 같은 방식으로 다룰 수 있습니다.

면접 답변 예시:

> 프로젝트 생성자를 별도 owner 필드로만 관리하면 참여자 목록과 권한 처리가 분리될 수 있어서, 생성 즉시 ProjectMember에도 OWNER로 저장했습니다. 이렇게 해서 프로젝트 내 구성원 조회, 권한 체크, 역할 관리를 일관되게 처리할 수 있었습니다.

### 지원 승인 시 멤버 자동 전환

지원서가 승인되면 Application 상태를 ACCEPTED로 변경하고, 동시에 ProjectMember를 생성합니다. 지원서에 입력한 포지션 정보도 멤버 정보로 복사합니다.

면접 답변 예시:

> 지원서와 멤버는 생명주기가 다르기 때문에 분리했습니다. 지원서는 이력과 상태를 관리하고, 승인된 이후 실제 프로젝트 참여 권한은 ProjectMember가 담당하도록 설계했습니다.

### 모집 정원 관리

프로젝트에 멤버가 추가될 때 현재 인원을 증가시키고, 정원이 차면 모집 상태를 닫을 수 있는 구조입니다. 지원 승인 전에 프로젝트가 CLOSED 상태인지 확인해 마감된 프로젝트에 추가 합류가 발생하지 않도록 했습니다.

면접 답변 예시:

> 모집 상태와 현재 인원을 함께 관리해 단순히 지원서를 승인하는 데서 끝나지 않고, 프로젝트의 모집 가능 여부까지 비즈니스 규칙으로 묶었습니다.

### Soft Delete와 복구

회원과 프로젝트는 물리 삭제 대신 deletedAt을 이용한 Soft Delete를 사용합니다. 관리자 페이지에서는 삭제된 데이터까지 조회하고 복구할 수 있습니다.

면접 답변 예시:

> 서비스 운영 중 실수로 삭제한 데이터를 복구할 수 있도록 Soft Delete를 적용했습니다. 또한 삭제된 데이터의 이메일이나 전화번호가 재가입에 악용되거나 중복 충돌을 만들 수 있어, 중복 검증 시 삭제 데이터까지 포함하도록 했습니다.

## 7. 예외 처리 및 응답 표준화

ErrorCode enum에서 에러 코드, 메시지, HTTP 상태를 한 곳에서 관리합니다. BusinessException, EntityNotFoundException, Validation 예외를 DefaultExceptionAdvice에서 처리해 클라이언트가 일관된 에러 응답을 받을 수 있도록 했습니다.

포트폴리오 어필 포인트:

- 도메인별 에러 코드 분리: AUTH, USER, PROJECT, APPLY, MEMBER, BOARD, COMMENT, VALID, SERVER
- 비즈니스 예외와 검증 예외 처리 분리
- Validation 실패 시 fieldErrors로 필드 단위 오류 제공
- 예상하지 못한 예외는 INTERNAL_SERVER_ERROR로 통일

포트폴리오 어필 문장:

> 예외 처리를 컨트롤러마다 흩어두지 않고 @RestControllerAdvice로 중앙화해 유지보수성과 API 응답 일관성을 높였습니다.

## 8. 관리자 페이지 구현

Thymeleaf 기반 관리자 페이지를 제공합니다.

주요 기능:

- 관리자 로그인
- 대시보드
- 전체 회원 수 조회
- 전체 프로젝트 수 조회
- 회원 목록 페이징
- 프로젝트 목록 페이징
- 삭제된 회원/프로젝트 포함 조회
- 회원 삭제 및 복구
- 프로젝트 삭제 및 복구
- 관리자 작업 로그 조회
- 회원/프로젝트/로그 검색

포트폴리오 어필 문장:

> REST API뿐 아니라 운영자가 데이터를 관리할 수 있는 Thymeleaf 기반 관리자 화면을 함께 구현해 서비스 운영 관점까지 고려했습니다.

## 9. 파일 업로드 구현

CloudinaryService를 통해 MultipartFile을 Cloudinary에 업로드하고, 업로드 결과로 받은 secure_url을 서비스에서 사용할 수 있도록 했습니다.

포트폴리오 어필 포인트:

- 서버 로컬 저장소에 이미지를 직접 저장하지 않고 외부 이미지 스토리지 사용
- HTTPS 이미지 URL 반환
- Cloudinary 설정을 별도 Config로 분리

포트폴리오 어필 문장:

> 이미지 파일은 애플리케이션 서버에 직접 저장하지 않고 Cloudinary에 업로드해 서버 저장소 의존도를 줄이고, 배포 환경에서도 안정적으로 접근 가능한 URL을 사용했습니다.

## 10. API 기능 정리

### Auth

- 회원가입
- 로그인
- Access Token 재발급
- 로그아웃
- 이메일 찾기
- 비밀번호 재설정

### User

- 내 정보 조회
- 내 정보 수정
- 전화번호 중복 확인
- 닉네임 중복 확인
- 내가 작성한 프로젝트 조회
- 내가 참여 중인 프로젝트 조회

### Project

- 프로젝트 생성
- 프로젝트 목록 조회
- 프로젝트 상세 조회
- 프로젝트 수정
- 프로젝트 삭제
- 모집 마감
- 재모집

### Application

- 프로젝트 지원
- 지원서 목록 조회
- 내 지원 현황 조회
- 지원 취소
- 지원 승인
- 지원 거절

### ProjectMember

- 프로젝트 멤버 조회
- 프로젝트 멤버 역할/포지션 관리 기반

### BoardPost & Comment

- 프로젝트 게시글 작성/조회/수정/삭제
- 댓글 작성/조회/수정/삭제

### Admin

- 관리자 로그인
- 관리자 대시보드
- 회원 관리
- 프로젝트 관리
- 관리자 로그 관리
- 검색

## 11. 기술적으로 설명하기 좋은 설계 선택

### DTO와 Entity 분리

RequestDto와 ResponseDto를 사용해 API 입력/출력 모델과 JPA Entity를 분리했습니다. Mapper 클래스를 통해 변환 책임을 분리해 컨트롤러와 서비스의 복잡도를 낮췄습니다.

### Service Interface와 Impl 분리

서비스 계층을 인터페이스와 구현체로 분리해 역할을 명확히 했습니다. 기능 확장이나 테스트 대역 구성 시 유리한 구조입니다.

### Transaction 경계 설정

조회 메서드에는 `@Transactional(readOnly = true)`를 적용하고, 상태 변경 로직에는 `@Transactional`을 적용했습니다. 지원 승인처럼 여러 엔티티가 함께 변경되는 작업은 하나의 트랜잭션 안에서 처리됩니다.

### 권한 검증 위치

방장만 가능한 작업, 지원자 본인만 가능한 작업 등 비즈니스 권한 검증을 서비스 계층에서 수행합니다. 컨트롤러에 의존하지 않고 핵심 도메인 규칙을 서비스에 둔 점을 강조할 수 있습니다.

### CORS 설정

프론트엔드 개발 서버 및 ngrok 주소를 허용하고, Authorization 헤더를 노출하도록 설정했습니다. 프론트엔드와 백엔드가 분리된 환경에서 JWT 인증이 동작하도록 고려했습니다.

## 12. 면접에서 받을 수 있는 질문과 답변 소재

### Q. 왜 Refresh Token을 DB에 저장했나요?

Access Token은 Stateless하게 검증할 수 있지만, Refresh Token까지 완전히 Stateless하게 두면 로그아웃이나 강제 만료 처리가 어렵습니다. DB에 저장하면 로그아웃 시 Refresh Token을 삭제해 재발급을 막을 수 있고, 사용자별 토큰 상태를 서버에서 제어할 수 있습니다.

### Q. 왜 Soft Delete를 사용했나요?

회원이나 프로젝트 데이터는 운영 중 실수로 삭제될 수 있고, 관리자 복구가 필요할 수 있습니다. 또한 지원서, 게시글, 댓글처럼 연관 데이터가 있는 도메인은 물리 삭제 시 이력 추적이 어려워질 수 있어 Soft Delete가 더 적합하다고 판단했습니다.

### Q. Application과 ProjectMember를 왜 분리했나요?

Application은 지원 과정의 상태를 관리하는 엔티티이고, ProjectMember는 실제 프로젝트에 합류한 사용자를 나타내는 엔티티입니다. 지원 전/후의 책임이 다르기 때문에 분리했고, 승인 시 Application을 ACCEPTED로 바꾸면서 ProjectMember를 생성하는 흐름으로 구현했습니다.

### Q. 관리자 페이지를 왜 REST API가 아니라 Thymeleaf로 만들었나요?

관리자 페이지는 내부 운영자가 사용하는 화면이라 별도의 프론트엔드 앱 없이 서버 렌더링으로 빠르게 구현할 수 있습니다. Spring Security Form Login과도 자연스럽게 결합할 수 있어 관리자 인증과 화면 렌더링을 단순하게 구성했습니다.

### Q. 전역 예외 처리를 도입한 이유는 무엇인가요?

컨트롤러마다 try-catch를 작성하면 응답 형식이 흐트러지고 유지보수가 어려워집니다. 그래서 `@RestControllerAdvice`를 사용해 예외 처리를 중앙화하고, ErrorCode enum으로 에러 코드와 HTTP 상태를 일관되게 관리했습니다.

## 13. 이력서/포트폴리오에 바로 넣기 좋은 문장

- Spring Boot와 JPA를 기반으로 프로젝트 팀원 모집 플랫폼의 REST API 서버를 설계 및 구현했습니다.
- Spring Security와 JWT를 이용해 Access Token/Refresh Token 기반 인증 흐름을 구현하고, Refresh Token을 DB에 저장해 로그아웃 및 토큰 재발급을 제어했습니다.
- 프로젝트 지원, 승인, 거절, 모집 마감, 재모집 등 상태 기반 비즈니스 로직을 서비스 계층에 구현했습니다.
- User-Project 다대다 관계를 ProjectMember 중간 엔티티로 분리해 역할과 포지션 같은 관계 속성을 관리했습니다.
- Soft Delete를 적용하고 관리자 페이지에서 삭제/복구 및 운영 로그 조회가 가능하도록 구현했습니다.
- `@RestControllerAdvice`와 ErrorCode enum을 사용해 예외 응답을 표준화했습니다.
- Cloudinary 연동을 통해 이미지 업로드 후 외부 접근 가능한 secure URL을 반환하도록 구현했습니다.
- Thymeleaf 기반 관리자 대시보드를 구현해 회원, 프로젝트, 로그 데이터를 운영자가 관리할 수 있도록 했습니다.

## 14. GitHub README에 추가하면 좋은 섹션

### 추천 README 구성

1. 프로젝트 소개
2. 주요 기능
3. 기술 스택
4. 아키텍처 및 패키지 구조
5. ERD 또는 도메인 관계 설명
6. 인증/인가 흐름
7. 핵심 비즈니스 로직
8. API 명세 링크 또는 Swagger 주소
9. 실행 방법
10. 트러블슈팅 및 개선 사항

### README용 짧은 소개 문구

Backend-MATE는 개발자 프로젝트 팀원 모집과 지원 관리를 위한 Spring Boot 기반 백엔드 서비스입니다. JWT 인증, 프로젝트 모집 상태 관리, 지원서 승인/거절, 프로젝트 멤버 관리, 게시판/댓글, 관리자 대시보드, 이미지 업로드 기능을 제공하며, 실제 서비스 운영을 고려해 Soft Delete, 전역 예외 처리, 관리자 로그 기능을 함께 구현했습니다.

## 15. 보완하면 더 좋아질 부분

- README 파일 인코딩이 깨져 보여 포트폴리오 전달 전 UTF-8로 복구하는 것이 좋습니다.
- 단위 테스트와 서비스 계층 테스트를 보강하면 신뢰도가 더 올라갑니다.
- Swagger API 설명을 더 자세히 작성하면 협업 경험을 어필하기 좋습니다.
- ERD 이미지를 README에 추가하면 도메인 설계가 한눈에 들어옵니다.
- Docker Compose로 MariaDB 실행 환경을 제공하면 실행 편의성이 좋아집니다.
- Refresh Token Rotation, 만료 시간 설정 외부화, 토큰 탈취 대응 전략을 추가하면 보안 어필이 더 강해집니다.
- 관리자 검색 로직은 현재 메모리 필터링보다 Repository 쿼리 기반으로 개선하면 대용량 데이터에서 더 안정적입니다.
- Cloudinary 업로드 예외도 전역 ErrorCode와 연결하면 예외 응답 일관성이 더 좋아집니다.
