package com.mysns.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mysns.domain.User;
import com.mysns.dto.SignUpRequest;
import com.mysns.exception.DuplicateUserException;
import com.mysns.exception.InvalidCredentialsException;
import com.mysns.exception.UserNotFoundException;
import com.mysns.exception.ValidationException;
import com.mysns.repository.UserRepository;
import com.mysns.util.PasswordUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 없는 아이디로 로그인할 때도 BCrypt 비교를 한 번 수행해, 응답 시간으로 가입 여부를 추측하지 못하게 한다. */
    private final String dummyHash;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    public void signUp(SignUpRequest req) {
        if (!req.password().equals(req.passwordConfirm())) {
            throw new ValidationException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        // 선검사는 친절한 메시지를 위한 것이고, 동시 가입의 최종 방어선은 PK 제약이다
        // (UserRepository.save가 제약 위반을 DuplicateUserException으로 번역한다).
        if (userRepository.existsById(req.id())) {
            throw new DuplicateUserException(req.id());
        }
        userRepository.save(new User(req.id(), req.name(), passwordEncoder.encode(req.password()), null, null));
    }

    /**
     * 아이디가 없는 경우와 비밀번호가 틀린 경우를 같은 예외로 응답해,
     * 가입된 아이디를 알아내는 계정 열거(user enumeration)를 막는다.
     *
     * 기존 JSP 앱의 PBKDF2 계정은 로그인에 성공하는 순간 BCrypt로 다시 저장한다(점진적 마이그레이션).
     * 평문 비밀번호는 로그인 때만 알 수 있으므로, 일괄 변환이 불가능해 이 방식을 쓴다.
     */
    public User login(String id, String rawPassword) {
        Optional<User> found = userRepository.findById(id);
        if (found.isEmpty()) {
            passwordEncoder.matches(rawPassword, dummyHash);
            throw new InvalidCredentialsException();
        }
        User user = found.get();
        if (!matches(user, rawPassword)) {
            throw new InvalidCredentialsException();
        }
        if (isLegacy(user)) {
            userRepository.updatePassword(id, passwordEncoder.encode(rawPassword));
        }
        return user;
    }

    /** 호출부가 토큰의 아이디를 넘기고, 여기서 비밀번호를 다시 확인해 본인 확인을 강제한다. */
    public void withdraw(String id, String rawPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (!matches(user, rawPassword)) {
            throw new InvalidCredentialsException();
        }
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrderByCreatedAtDesc();
    }

    public User getById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    /** salt 컬럼이 채워져 있으면 BCrypt 이전(PBKDF2) 방식으로 저장된 비밀번호다. */
    private boolean isLegacy(User user) {
        return user.salt() != null;
    }

    private boolean matches(User user, String rawPassword) {
        return isLegacy(user)
                ? PasswordUtil.matches(rawPassword, user.salt(), user.passwordHash())
                : passwordEncoder.matches(rawPassword, user.passwordHash());
    }
}
