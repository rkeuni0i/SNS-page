-- ============================================================
--  mysns 스키마 (최적화 버전)
--
--  기존 스키마 대비 변경점
--  ------------------------------------------------------------
--  1) [보안] user.password(VARCHAR 32, 평문) 컬럼을 제거하고
--     password_hash + salt 로 분리했다.
--     PBKDF2WithHmacSHA256(256bit) 결과를 Base64로 인코딩하면 44자,
--     16바이트 salt는 24자가 되므로 각각 여유를 두고 잡았다.
--
--  2) [정합성] feed.id 에 user.id 를 참조하는 외래키를 추가했다.
--     기존에는 제약이 전혀 없어, 탈퇴한 회원의 피드가 고아 레코드로
--     남거나 존재하지 않는 아이디로 피드가 삽입될 수 있었다.
--
--  3) [성능] 조회 패턴에 맞춰 인덱스를 추가했다.
--     - main.jsp   : ORDER BY ts DESC  -> idx_feed_ts
--     - userList   : ORDER BY ts DESC  -> idx_user_ts
--       인덱스가 없으면 매 요청마다 풀 스캔 후 filesort가 발생한다.
--     - 마이피드(특정 회원의 글을 최신순으로) 조회를 위해
--       (id, ts) 복합 인덱스를 둔다. 복합 인덱스는 선행 컬럼(id)으로
--       범위를 좁힌 뒤 ts 순서를 그대로 활용하므로 정렬 비용이 사라진다.
--
--  4) [호환성] utf8 -> utf8mb4 로 변경했다. MySQL의 'utf8'은 실제로는
--     3바이트까지만 저장하는 반쪽 UTF-8이라 이모지(4바이트)를 넣으면
--     저장에 실패한다. SNS 특성상 이모지는 필수에 가깝다.
--
--  5) [엔진] 외래키와 트랜잭션이 필요하므로 InnoDB를 명시했다.
-- ============================================================

-- ############################################################
-- ##  [먼저 읽어주세요] 기존 mysns DB가 이미 있다면 반드시 삭제할 것
-- ############################################################
-- 아래 문장들은 CREATE ... IF NOT EXISTS 이므로, 같은 이름의 DB/테이블이
-- 이미 있으면 **아무것도 바꾸지 않고 조용히 넘어갑니다.**
--
-- 예전 버전(평문 password 컬럼)으로 만든 mysns 가 남아 있는 상태에서
-- 이 파일을 실행하면 스키마가 갱신되지 않고, 이어서 data.sql 이
--     ERROR 1054 (42S22): Unknown column 'password_hash' in 'field list'
-- 로 실패합니다. 화면에는 옛 데이터가 그대로 남아 혼란스러워집니다.
--
-- 따라서 업그레이드 시에는 먼저 지우고 시작하세요:
--     DROP DATABASE IF EXISTS mysns;
--
-- 기존 회원 데이터를 살려야 한다면 파일 맨 아래 '마이그레이션' 주석을 참고하세요.
-- (평문 비밀번호는 해시로 되돌릴 수 없어 전원 재설정이 필요합니다.)
-- ############################################################

-- 이 파일은 UTF-8로 저장되어 있다. 클라이언트 문자셋이 utf8mb4가 아니면
-- 한글이 깨진 채로 저장되므로(예: '김시민' -> 'ê¹€ì‹œë¯¼'), 반드시 먼저 선언한다.
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS mysns
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mysns;

CREATE TABLE IF NOT EXISTS user (
    id            VARCHAR(128) NOT NULL COMMENT '로그인 아이디(이메일)',
    password_hash VARCHAR(88)  NOT NULL COMMENT 'PBKDF2WithHmacSHA256, Base64',
    salt          VARCHAR(24)  NOT NULL COMMENT '회원별 16바이트 salt, Base64',
    name          VARCHAR(32)  NOT NULL,
    ts            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    PRIMARY KEY (id),
    INDEX idx_user_ts (ts)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feed (
    no      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id      VARCHAR(128)    NOT NULL COMMENT '작성자, user.id 참조',
    content VARCHAR(4096)   NOT NULL,
    images  VARCHAR(255)    NULL COMMENT '서버가 생성한 UUID 파일명',
    ts      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    PRIMARY KEY (no),
    INDEX idx_feed_ts (ts),
    INDEX idx_feed_id_ts (id, ts),
    CONSTRAINT fk_feed_user
        FOREIGN KEY (id) REFERENCES user (id)
        ON DELETE CASCADE      -- 회원 탈퇴 시 해당 회원의 피드도 함께 정리
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- ------------------------------------------------------------
--  정규화 점검 및 반정규화 제안
-- ------------------------------------------------------------
--  현재 두 테이블은 모든 비키 컬럼이 기본키에 완전 함수 종속이고
--  이행적 종속이 없으므로 이미 제3정규형(3NF)을 만족한다.
--  컬럼 수가 적고 다중값 속성도 없어 추가 정규화의 실익은 없다.
--
--  다만 트래픽이 커지면 아래 지점의 반정규화를 고려할 만하다.
--
--   * user.feed_count 컬럼 추가
--     프로필 화면마다 SELECT COUNT(*) FROM feed WHERE id=? 를 수행하면
--     글이 많은 사용자일수록 비용이 커진다. 피드 작성/삭제 트랜잭션 안에서
--     UPDATE user SET feed_count = feed_count + 1 WHERE id = ?
--     처럼 원자적으로 증감시키면 조회 비용이 O(1)로 떨어진다.
--     (읽기 빈도 >> 쓰기 빈도일 때만 이득이라는 점에 유의)
--
--   * 좋아요/팔로우 기능을 추가한다면
--     follow(follower_id, followee_id) 복합 PK,
--     feed_like(feed_no, user_id) 복합 PK 로 N:M 관계를 별도 테이블로 두고,
--     '좋아요 수'처럼 자주 읽고 가끔 쓰는 값은 집계 컬럼으로 분리해
--     쓰기 경합 범위를 좁힌다.
-- ------------------------------------------------------------


-- ------------------------------------------------------------
--  기존 데이터가 이미 있는 경우의 마이그레이션 (참고)
-- ------------------------------------------------------------
--  기존 평문 비밀번호는 해시로 되돌릴 수 없으므로 그대로 옮길 수 없다.
--  실제 서비스라면 컬럼을 추가한 뒤 전원 비밀번호 재설정을 안내해야 한다.
--
--  ALTER TABLE user ADD COLUMN password_hash VARCHAR(88) NULL AFTER id;
--  ALTER TABLE user ADD COLUMN salt          VARCHAR(24) NULL AFTER password_hash;
--  -- (전원 비밀번호 재설정 후)
--  ALTER TABLE user DROP COLUMN password;
--  ALTER TABLE user MODIFY password_hash VARCHAR(88) NOT NULL;
--  ALTER TABLE user MODIFY salt          VARCHAR(24) NOT NULL;
-- ------------------------------------------------------------
