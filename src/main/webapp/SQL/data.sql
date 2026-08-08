SET NAMES utf8mb4;

USE mysns;

-- 아래 계정들의 로그인 비밀번호는 모두 test1234! 입니다.
INSERT INTO user(id, password_hash, salt, name) VALUES
  ('kim@abc.com', '9QMwasvtlj3o28xVM9qZQ80nTjZ96oQjil6ROOFMalQ=', 'rbifRiTWLA7pyays73DPlA==', '김시민'),
  ('lee@abc.com', '9DOzi8/m4nw3VU9ytckSe5l2NKjevQIVKMhWqA9Yq1k=', 'VFcGtWu7bcMSsbVhAfmBpQ==', '이순신'),
  ('kwon@abc.com', 'U0W6Muc6FrVZydNgPxK+GcMc2zDTd1nKx0GYtf7NC3I=', 'W1Ar/Emk87RTJ0mHotZ3Tw==', '권율');

INSERT INTO feed(id, content) VALUES
  ('kim@abc.com', 'Hello'),
  ('kwon@abc.com', 'Aloha');
