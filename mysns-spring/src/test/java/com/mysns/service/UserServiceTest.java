package com.mysns.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mysns.domain.User;
import com.mysns.dto.SignUpRequest;
import com.mysns.exception.DuplicateUserException;
import com.mysns.exception.InvalidCredentialsException;
import com.mysns.exception.ValidationException;
import com.mysns.repository.UserRepository;
import com.mysns.util.PasswordUtil;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    private User bcryptUser(String rawPassword) {
        return new User("a@b.com", "에이", passwordEncoder.encode(rawPassword), null, null);
    }

    private User legacyUser(String rawPassword) {
        String salt = PasswordUtil.generateSalt();
        return new User("a@b.com", "에이", PasswordUtil.hash(rawPassword, salt), salt, null);
    }

    @Test
    void 회원가입은_평문이_아닌_BCrypt_해시를_저장하고_salt는_비운다() {
        when(userRepository.existsById("a@b.com")).thenReturn(false);

        userService.signUp(new SignUpRequest("a@b.com", "password1", "password1", "에이"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        User user = saved.getValue();
        assertTrue(user.passwordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches("password1", user.passwordHash()));
        assertNull(user.salt());
    }

    @Test
    void 비밀번호_확인이_다르면_가입할_수_없다() {
        assertThrows(ValidationException.class,
                () -> userService.signUp(new SignUpRequest("a@b.com", "password1", "password2", "에이")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void 이미_가입된_아이디면_가입할_수_없다() {
        when(userRepository.existsById("a@b.com")).thenReturn(true);
        assertThrows(DuplicateUserException.class,
                () -> userService.signUp(new SignUpRequest("a@b.com", "password1", "password1", "에이")));
    }

    @Test
    void 없는_아이디와_틀린_비밀번호는_같은_예외로_응답한다() {
        when(userRepository.findById("a@b.com")).thenReturn(Optional.of(bcryptUser("password1")));
        when(userRepository.findById("none@b.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.login("a@b.com", "wrong"));
        assertThrows(InvalidCredentialsException.class, () -> userService.login("none@b.com", "password1"));
        assertEquals("a@b.com", userService.login("a@b.com", "password1").id());
    }

    @Test
    void BCrypt_계정은_로그인해도_비밀번호를_다시_저장하지_않는다() {
        when(userRepository.findById("a@b.com")).thenReturn(Optional.of(bcryptUser("password1")));

        userService.login("a@b.com", "password1");

        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void 레거시_PBKDF2_계정은_로그인에_성공하면_BCrypt로_옮겨진다() {
        when(userRepository.findById("a@b.com")).thenReturn(Optional.of(legacyUser("password1")));

        userService.login("a@b.com", "password1");

        ArgumentCaptor<String> newHash = ArgumentCaptor.forClass(String.class);
        verify(userRepository).updatePassword(eq("a@b.com"), newHash.capture());
        assertTrue(passwordEncoder.matches("password1", newHash.getValue()));
    }

    @Test
    void 레거시_계정이_비밀번호를_틀리면_옮겨지지_않는다() {
        when(userRepository.findById("a@b.com")).thenReturn(Optional.of(legacyUser("password1")));

        assertThrows(InvalidCredentialsException.class, () -> userService.login("a@b.com", "wrong"));
        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void 비밀번호가_틀리면_탈퇴할_수_없다() {
        when(userRepository.findById("a@b.com")).thenReturn(Optional.of(bcryptUser("password1")));

        assertThrows(InvalidCredentialsException.class, () -> userService.withdraw("a@b.com", "wrong"));
        verify(userRepository, never()).deleteById(any());
    }
}
