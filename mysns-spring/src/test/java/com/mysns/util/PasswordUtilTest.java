package com.mysns.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void 같은_비밀번호도_salt가_다르면_해시가_다르다() {
        String hash1 = PasswordUtil.hash("test1234!", PasswordUtil.generateSalt());
        String hash2 = PasswordUtil.hash("test1234!", PasswordUtil.generateSalt());
        assertNotEquals(hash1, hash2);
        assertEquals(44, hash1.length()); // schema.sql의 password_hash VARCHAR(88) 안에 들어간다
    }

    @Test
    void 올바른_비밀번호만_일치한다() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("test1234!", salt);
        assertTrue(PasswordUtil.matches("test1234!", salt, hash));
        assertFalse(PasswordUtil.matches("wrong-password", salt, hash));
        assertFalse(PasswordUtil.matches("test1234!", null, hash));
    }

    /** db/data.sql의 시드 계정(kim@abc.com)은 기존 JSP 앱이 만든 해시다. 이관 후에도 로그인돼야 한다. */
    @Test
    void 기존_JSP_앱이_만든_해시와_호환된다() {
        assertTrue(PasswordUtil.matches("test1234!",
                "rbifRiTWLA7pyays73DPlA==", "9QMwasvtlj3o28xVM9qZQ80nTjZ96oQjil6ROOFMalQ="));
    }
}
