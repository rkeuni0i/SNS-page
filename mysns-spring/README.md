# MySNS (Spring Boot)

기존 JSP/Servlet MySNS를 Spring Boot REST API로 옮긴 프로젝트. 계층: `controller → service → repository(JdbcTemplate)`.

## 실행

```bash
# 1. DB (기존과 동일한 스키마)
mysql -u root -p < db/schema.sql
mysql -u root -p < db/data.sql      # 선택. 시드 계정 비밀번호는 모두 test1234!

# 2. 앱
DB_PASSWORD=<비밀번호> JWT_SECRET=<32바이트 이상 랜덤 문자열> ./gradlew bootRun
# PowerShell: $env:DB_PASSWORD='...'; $env:JWT_SECRET='...'; .\gradlew.bat bootRun
```

기존 DB(세션/PBKDF2 버전)를 쓰고 있다면 `db/migration_jwt_bcrypt.sql`을 먼저 한 번 실행한다(데이터는 지우지 않고, 여러 번 실행해도 안전).
`JWT_SECRET` 생성 예: `openssl rand -base64 48` — 저장소에 커밋하지 않는다. 바꾸면 발급된 토큰은 모두 무효가 되어 다시 로그인해야 한다.

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- 환경변수: `DB_PASSWORD`·`JWT_SECRET`(필수), `DB_URL`, `DB_USERNAME`, `UPLOAD_DIR`(기본 `./uploads`), `SERVER_PORT`(기본 8081), `COOKIE_SECURE`(HTTPS 배포 시 true)
- React 개발 서버: `cd frontend && npm install && npm run dev` (http://localhost:5173, API는 8081로 프록시). 배포용 빌드는 `npm run build` → `src/main/resources/static`

## 인증 (Spring Security + JWT)

| | 내용 |
|---|---|
| Access 토큰 | JWT(HS256), 15분. 로그인 응답 본문으로 받고 `Authorization: Bearer <token>`으로 보낸다. React는 메모리에만 보관 |
| Refresh 토큰 | JWT, 7일. **HttpOnly + SameSite=Strict 쿠키**(경로 `/api/auth`)로만 전달되어 JavaScript가 읽을 수 없다. DB(`refresh_token`)에 jti를 저장 |
| 재발급 | `POST /api/auth/refresh` — 쓸 때마다 기존 토큰을 폐기하고 새로 발급(rotation). 이미 쓴 토큰은 거부 |
| 로그아웃·탈퇴 | DB에서 refresh 토큰을 지워 서버에서 즉시 무효화 |
| 비밀번호 | BCrypt. 기존 PBKDF2 계정은 **로그인에 성공하는 순간 BCrypt로 자동 전환**(salt 컬럼은 NULL이 됨) |
| 접근 제어 | 가입·로그인·재발급·로그아웃·정적 파일·Swagger만 공개, 그 외 `/api/**`는 기본 차단 |

Swagger에서 호출해 보려면: `POST /api/auth/login`을 실행해 응답의 `accessToken`을 복사 → 우측 상단 **Authorize**에 붙여 넣기.

## API

| 기능 | API |
|---|---|
| 회원가입 | `POST /api/users` |
| 로그인 / 토큰 재발급 / 로그아웃 / 내 정보 | `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`, `GET /api/auth/me` |
| 가입자 목록 / 프로필 | `GET /api/users`, `GET /api/users/{id}` |
| 팔로워 / 팔로잉 목록 | `GET /api/users/{id}/followers`, `GET /api/users/{id}/following` |
| 회원 탈퇴(비밀번호 재확인) | `DELETE /api/users/me` |
| 피드 조회(`page`, `authorId`) / 작성(multipart: `content`, `image`) | `GET /api/feeds`, `POST /api/feeds` |
| 팔로우 / 언팔로우 | `POST /api/follows/{userId}`, `DELETE /api/follows/{userId}` |

업로드 이미지는 `/images/{uuid}.{ext}` 로 서빙된다.
