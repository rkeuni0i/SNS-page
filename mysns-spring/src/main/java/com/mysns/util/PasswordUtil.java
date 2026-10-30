package com.mysns.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 기존 JSP 앱이 만든 PBKDF2WithHmacSHA256 비밀번호(+ salt 컬럼)를 검증하기 위한 레거시 유틸.
 * 신규 가입과 비밀번호 저장은 BCrypt(Spring Security PasswordEncoder)가 담당하며,
 * 이 클래스는 PBKDF2 계정이 로그인할 때 한 번 검증한 뒤 BCrypt로 옮겨 가는 데만 쓰인다.
 * 모든 계정이 옮겨 간 뒤에는 삭제할 수 있다.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;               // bits
    private static final int SALT_BYTES = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String rawPassword, String saltBase64) {
        char[] password = rawPassword.toCharArray();
        PBEKeySpec spec = null;
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            byte[] hash = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("비밀번호 해시 생성에 실패했습니다.", e);
        } finally {
            if (spec != null) spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }

    public static boolean matches(String rawPassword, String saltBase64, String expectedHash) {
        if (saltBase64 == null || expectedHash == null) return false;
        // 타이밍 공격을 피하기 위해 상수 시간 비교를 쓴다.
        return MessageDigest.isEqual(
                hash(rawPassword, saltBase64).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
