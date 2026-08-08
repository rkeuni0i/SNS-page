package tools;

import util.PasswordEncoder;

/**
 * 시드 데이터 생성기.
 *
 * 비밀번호를 해시로 저장하도록 바꾼 뒤로는, data.sql에 비밀번호를 직접
 * 적어 넣을 수 없다(해시는 salt에 따라 매번 달라지기 때문). 이 클래스를
 * 실행하면 실제로 로그인 가능한 INSERT 문을 콘솔에 출력해 준다.
 *
 * 실행 방법 (이클립스에서 Run As > Java Application, 또는 터미널에서):
 *     javac -d build/classes src/main/java/util/PasswordEncoder.java \
 *           src/main/java/tools/SeedDataGenerator.java
 *     java -cp build/classes tools.SeedDataGenerator
 *
 * 출력된 SQL을 복사해 MySQL에서 실행하면 된다.
 */
public final class SeedDataGenerator {

    private static final String[][] SEED_USERS = {
            // {id, 평문 비밀번호, 이름}
            {"kim@abc.com",  "test1234!", "김시민"},
            {"lee@abc.com",  "test1234!", "이순신"},
            {"kwon@abc.com", "test1234!", "권율"},
    };

    public static void main(String[] args) {
        // 이 스크립트에는 한글 이름이 들어간다. 클라이언트 문자셋이 utf8mb4가 아니면
        // 한글이 깨진 채 저장되므로(예: '김시민' -> 'ê¹€ì‹œë¯¼') 파일 스스로 선언하게 한다.
        System.out.println("SET NAMES utf8mb4;");
        System.out.println();
        System.out.println("USE mysns;");
        System.out.println();
        System.out.println("-- 아래 계정들의 로그인 비밀번호는 모두 test1234! 입니다.");
        System.out.println("INSERT INTO user(id, password_hash, salt, name) VALUES");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SEED_USERS.length; i++) {
            String id = SEED_USERS[i][0];
            String rawPassword = SEED_USERS[i][1];
            String name = SEED_USERS[i][2];

            String salt = PasswordEncoder.generateSalt();
            String hash = PasswordEncoder.encode(rawPassword, salt);

            sb.append("  ('").append(id).append("', '")
              .append(hash).append("', '")
              .append(salt).append("', '")
              .append(name).append("')")
              .append(i == SEED_USERS.length - 1 ? ";" : ",")
              .append(System.lineSeparator());
        }
        System.out.print(sb);

        System.out.println();
        System.out.println("INSERT INTO feed(id, content) VALUES");
        System.out.println("  ('kim@abc.com', 'Hello'),");
        System.out.println("  ('kwon@abc.com', 'Aloha');");
    }
}
