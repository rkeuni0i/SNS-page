# MySNS 로컬 실행 가이드

Eclipse + Tomcat 9 + MySQL 환경에서 직접 띄워 확인하는 절차입니다.
각 단계마다 "이렇게 나오면 성공" 기준을 적어두었으니 어디서 막혔는지 바로 알 수 있습니다.

---

## 0. 준비물 확인

| 항목 | 필요 버전 | 확인 방법 |
|---|---|---|
| JDK | **17** | 터미널에서 `java -version` |
| Eclipse | Java EE(웹 개발) 지원 버전 | 이미 사용 중 |
| Tomcat | **9.x** (10 아님) | 프로젝트가 `javax.*` 를 쓰므로 10은 호환 안 됨 |
| DB | **MariaDB 11.x** (이 PC에 설치됨) | `mysql --version` |
| Connector/J | **MariaDB** Connector/J 3.x | 아래 2단계에서 내려받음 |

> **Tomcat 10을 쓰면 안 되는 이유** — Tomcat 10부터 패키지가 `javax.servlet` → `jakarta.servlet` 로 바뀌었습니다. 이 프로젝트는 Servlet 4.0(`javax`) 기준이라 Tomcat 10에서는 서블릿을 아예 못 찾습니다.

---

## 1. 데이터베이스 만들기

> ### ⚠️ 기존 `mysns` DB가 있다면 반드시 먼저 삭제하세요
>
> `schema.sql` 은 `CREATE ... IF NOT EXISTS` 라서, 같은 이름의 DB가 이미 있으면
> **아무것도 바꾸지 않고 조용히 넘어갑니다.** 예전 스키마(평문 `password` 컬럼)가
> 남아 있으면 이어지는 `data.sql` 이 아래 에러로 실패하고, 화면에는 옛 데이터가
> 그대로 남아 원인을 찾기 어려워집니다.
>
> ```
> ERROR 1054 (42S22): Unknown column 'password_hash' in 'INSERT INTO'
> ```
>
> ```powershell
> mysql -u root -p -e "DROP DATABASE IF EXISTS mysns;"
> ```

### 1-1. 서버 문자셋 확인 (한글 깨짐 예방)

```powershell
mysql -u root -p -e "SHOW VARIABLES LIKE 'character_set_server';"
```

`latin1` 이나 `euckr` 로 나오면 `my.ini` (보통 `C:\Program Files\MariaDB 11.3\data\my.ini`)를
관리자 권한 메모장으로 열어 아래를 추가하고 서비스를 재시작하세요.

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4
```

```powershell
Restart-Service MariaDB      # 관리자 PowerShell
```

### 1-2. 스키마와 데이터 넣기

프로젝트 폴더로 이동한 뒤 실행합니다. **PowerShell을 쓰는지 cmd를 쓰는지에 따라 명령이 다릅니다.**

**PowerShell (파란 창) — `<` 리디렉션을 지원하지 않음**

```powershell
cd C:\Users\2003g\Documents\project\MySNS
mysql -u root -p -e "SOURCE src/main/webapp/SQL/schema.sql"
mysql -u root -p -e "SOURCE src/main/webapp/SQL/data.sql"
```

`SOURCE` 안의 경로는 **슬래시(`/`)** 로 씁니다. 역슬래시는 이스케이프 문자로 해석됩니다.
`'<' 연산자는 나중에 사용하도록 예약되어 있습니다` 오류가 났다면 PowerShell을 쓰고 계신 겁니다.

**cmd (검은 창)**

```bat
cd C:\Users\2003g\Documents\project\MySNS
mysql -u root -p < src\main\webapp\SQL\schema.sql
mysql -u root -p < src\main\webapp\SQL\data.sql
```

PowerShell에서 cmd 문법을 그대로 쓰고 싶다면 `cmd /c "..."` 로 감싸도 됩니다.

> **주의** — PowerShell에서 `Get-Content data.sql | mysql -u root -p` 는 쓰지 마세요.
> Windows PowerShell 5.1은 파이프로 넘길 때 텍스트를 시스템 코드페이지(CP949)로 다시 인코딩하므로
> **한글 이름이 깨진 채 저장됩니다.** 위의 `SOURCE` 나 `cmd /c` 방식은 파일을 바이트 그대로 읽어 안전합니다.

**확인**

```sql
mysql -u root -p
USE mysns;
SHOW TABLES;              -- feed, user 두 개가 나와야 함
SELECT id, name FROM user; -- 김시민 / 이순신 / 권율 이 안 깨지고 나와야 함
```

이름이 `ê¹€ì‹œë¯¼` 처럼 깨져 보이면 문자셋 문제입니다. 다만 두 SQL 파일 첫 줄에 `SET NAMES utf8mb4;` 를 넣어두었기 때문에 정상적으로 나올 겁니다. 혹시 깨진다면 이렇게 다시 실행하세요.

```powershell
mysql -u root -p --default-character-set=utf8mb4 -e "SOURCE src/main/webapp/SQL/data.sql"
```

> **테스트 계정** — `kim@abc.com` / `lee@abc.com` / `kwon@abc.com`, 비밀번호는 셋 다 `test1234!`
> 비밀번호를 해시로 저장하도록 바꿨기 때문에, **예전에 쓰던 `111` 비밀번호로는 로그인되지 않습니다.**

---

## 2. JDBC 드라이버 배치 (가장 자주 막히는 지점)

1. **MariaDB** Connector/J를 내려받습니다 → https://mariadb.com/downloads/connectors/ 에서 **Java 8+ connector** 선택. `mariadb-java-client-3.x.x.jar` 가 나옵니다.
   (MySQL Connector/J는 MariaDB 11.x 와 핸드셰이크 호환 문제가 있어 권장하지 않습니다.)
2. 이 JAR을 **Tomcat 설치 폴더의 `lib` 안에** 넣습니다.

```
C:\...\apache-tomcat-9.0.xx\lib\mariadb-java-client-3.x.x.jar
```

> **주의** — 프로젝트의 `WEB-INF/lib` 에 넣으면 **안 됩니다.** `context.xml`의 DataSource는 Tomcat(컨테이너)이 직접 관리하는 리소스라, 웹앱 클래스로더가 아니라 컨테이너 클래스로더가 드라이버를 볼 수 있어야 합니다. 여기에 잘못 넣으면 `ClassNotFoundException` 이 납니다.

Eclipse에서 Tomcat 설치 위치를 모르겠다면: **Window → Preferences → Server → Runtime Environments** 에서 Apache Tomcat v9.0 선택 후 경로 확인.

3. 드라이버를 넣은 뒤에는 **Tomcat을 반드시 재시작**해야 인식됩니다.

---

## 3. DB 접속 정보 맞추기

`src/main/webapp/META-INF/context.xml` 을 열어 `username` / `password` 를 본인 MySQL 계정으로 수정합니다. 현재는 `root` / `1111` 로 되어 있습니다.

```xml
username="root"
password="본인_비밀번호"
```

나머지 설정(드라이버 클래스, UTF-8 인코딩, 커넥션 검증)은 이미 맞춰 두었으니 건드리지 않아도 됩니다.

---

## 4. Eclipse에서 실행

1. **`_to_delete` 폴더를 삭제**합니다. 예전 `dao` 패키지와 오래된 `.class` 파일이 들어 있어 그냥 두면 헷갈립니다. (제가 직접 삭제할 권한이 없어 이 폴더로 옮겨두었습니다.)
2. 프로젝트 우클릭 → **Refresh** (F5)
3. 메뉴 **Project → Clean...** → 프로젝트 선택 → Clean
   - `Problems` 탭에 빨간 에러가 없어야 합니다.
4. 프로젝트 우클릭 → **Run As → Run on Server** → Tomcat v9.0 선택 → Finish

**확인** — Console 탭에 아래와 비슷한 줄이 보이면 성공입니다.

```
정보: Server startup in [xxxx] milliseconds
```

브라우저에서 접속:

```
http://localhost:8080/MySNS/
```

> 컨텍스트 경로는 Eclipse 프로젝트명(`MySNS`)을 따릅니다. 대소문자가 다르면 404가 납니다.
> `index.html` 이 자동으로 `main.jsp` 로 보내고, 로그인 전이므로 `login.html` 로 다시 이동합니다.

---

## 5. 직접 해보는 테스트 체크리스트

제가 클라우드에서 검증한 항목들입니다. 같은 순서로 눌러보시면 됩니다.

### 기본 흐름
- [ ] `login.html` 에서 `kim@abc.com` / `test1234!` 로 로그인 → 피드 목록이 보인다
- [ ] 글쓰기 → 내용 입력하고 사진 한 장 첨부 → 업로드 → 피드 최상단에 뜬다
- [ ] 한글과 이모지(🎉)를 섞어 써도 안 깨진다
- [ ] 여러 줄로 쓰면 줄바꿈이 유지된다
- [ ] 로그아웃 → 주소창에 `main.jsp` 직접 입력 → 로그인 화면으로 튕긴다

### 보안 (이번에 고친 부분)
- [ ] 글 내용에 `<script>alert('테스트')</script>` 를 넣고 등록
      → **경고창이 뜨지 않고** 글자 그대로 화면에 보이면 정상 (XSS 방어)
- [ ] 로그아웃 상태에서 `withdraw.jsp` 주소를 직접 입력
      → 로그인 화면으로 튕긴다 (예전에는 남의 계정을 지울 수 있었음)
- [ ] 탈퇴 화면에서 **틀린 비밀번호** 입력 → 거부되고 계정이 남아있다
- [ ] 확장자를 `.txt` 나 `.jsp` 로 바꾼 파일을 사진으로 업로드 → 거부된다

### 회원가입 검증
- [ ] 비밀번호와 비밀번호(확인)를 **다르게** 입력 → 거부된다 (예전에는 검사조차 안 했음)
- [ ] 8자 미만 비밀번호 → 거부된다
- [ ] 이메일 형식이 아닌 아이디(`hello`) → 거부된다
- [ ] 이미 있는 아이디 → 거부된다

### 데이터 정합성
- [ ] 글을 2~3개 쓴 계정으로 탈퇴 → MySQL에서 확인

```sql
SELECT COUNT(*) FROM feed WHERE id='탈퇴한아이디';  -- 0 이어야 함 (FK CASCADE)
```

---

## 6. 자주 막히는 지점

### `Name [jdbc/mysns] is not bound in this Context`
`context.xml` 을 Tomcat이 못 읽은 경우입니다.
- 파일 위치가 `src/main/webapp/META-INF/context.xml` 이 맞는지 확인 (`WEB-INF` 아님)
- 서버에서 프로젝트를 제거했다가 다시 추가: Servers 탭 → Tomcat 우클릭 → **Add and Remove**
- Servers 탭 → Tomcat 우클릭 → **Clean...** 후 재시작

### `ClassNotFoundException: org.mariadb.jdbc.Driver`
드라이버 JAR이 Tomcat의 `lib` 폴더에 없습니다. 2단계를 다시 확인하고 **Tomcat을 재시작**하세요.
MySQL 8 을 쓰신다면 `context.xml` 하단의 주석 처리된 MySQL용 Resource 로 교체하세요.

### `Access denied for user 'root'@'localhost'`
`context.xml` 의 `password` 가 실제 MySQL 비밀번호와 다릅니다.

### `Unknown database 'mysns'`
1단계의 `schema.sql` 을 실행하지 않았습니다.

### 로그인이 안 됨 (비밀번호가 맞는데도)
비밀번호 저장 방식이 평문 → PBKDF2 해시로 바뀌었습니다. 예전 데이터가 남아 있다면 DB를 새로 만드세요.

```sql
DROP DATABASE mysns;
```
그 후 1단계를 다시 실행합니다. 테스트 계정 비밀번호는 `test1234!` 입니다.

### 404 Not Found
- URL의 컨텍스트 경로가 프로젝트명과 정확히 일치하는지 (`/MySNS/`, 대소문자 구분)
- Servers 탭에서 프로젝트가 실제로 배포되어 있는지

### 포트 8080 충돌
```
Several ports (8080) required by Tomcat v9.0 Server are already in use.
```
다른 Tomcat이 이미 떠 있습니다. 기존 서버를 정지하거나, Servers 탭에서 서버를 더블클릭해 포트를 8081 등으로 변경하세요.

### 사진이 안 보임 (엑스박스)
`images` 폴더는 첫 업로드 때 자동 생성됩니다. Eclipse는 배포본을 별도 폴더에 두므로 프로젝트 폴더가 아니라 배포 위치에 생깁니다. 브라우저 개발자도구(F12) → Network 탭에서 이미지 요청이 404인지 확인해 보세요. 404라면 서버를 Clean 후 재배포하면 해결되는 경우가 많습니다.

### 한글이 `???` 로 저장됨
`context.xml` 의 URL에 `characterEncoding=UTF-8` 이 들어있는지 확인하세요 (이미 넣어두었습니다). 그래도 안 되면 MySQL 서버 설정(`my.ini`)에 아래를 추가하고 재시작합니다.

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
```

---

## 7. 그래도 안 되면

Eclipse **Console** 탭의 빨간 에러 메시지 전체를 복사해서 보내주세요. 스택 트레이스 맨 아래쪽의 `Caused by:` 부분이 특히 중요합니다. 함께 원인을 찾아드리겠습니다.
