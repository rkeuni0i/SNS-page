package util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 기존 코드는 비밀번호를 평문으로 저장하고 문자열로 비교했다
 * (UserDAO.login 의 `upass.equals(rs.getString("password"))`).
 * DB가 한 번 유출되면 전 회원의 비밀번호가 그대로 노출되는 구조였다.
 *
 * 외부 라이브러리(BCrypt 등)를 추가하지 않고 JDK 표준 javax.crypto 만으로
 * PBKDF2WithHmacSHA256 + 회원별 salt + 12만 회 반복을 적용했다.
 * 반복 횟수를 크게 잡는 이유는 공격자가 GPU로 대량 대입을 시도할 때의
 * 비용을 의도적으로 끌어올리기 위한 것이다(key stretching).
 */
public final class PasswordEncoder {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;               // bits
    private static final int SALT_BYTES = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /** SecureRandom은 스레드 세이프하므로 정적 공유가 안전하다. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordEncoder() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /** 회원마다 다른 salt를 생성한다. 같은 비밀번호라도 해시가 달라져 레인보우 테이블이 무력화된다. */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String encode(String rawPassword, String saltBase64) {
        char[] password = rawPassword.toCharArray();
        PBEKeySpec spec = null;
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("비밀번호 해시 생성에 실패했습니다.", e);
        } finally {
            // 평문 비밀번호가 힙에 남아있는 시간을 최소화한다.
            if (spec != null) spec.clearPassword();
            java.util.Arrays.fill(password, '\0');
        }
    }

    public static boolean matches(String rawPassword, String saltBase64, String expectedHash) {
        if (saltBase64 == null || expectedHash == null) return false;
        return constantTimeEquals(encode(rawPassword, saltBase64), expectedHash);
    }

    /**
     * 일반 equals는 첫 불일치 문자에서 즉시 반환하므로, 비교에 걸린 시간을 측정해
     * 해시를 한 글자씩 추측하는 타이밍 공격이 이론적으로 가능하다.
     * 길이에만 의존하고 내용에는 의존하지 않는 상수 시간 비교를 사용한다.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
