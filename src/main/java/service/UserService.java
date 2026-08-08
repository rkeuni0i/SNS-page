package service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import domain.User;
import exceptions.DuplicateUserException;
import exceptions.InvalidCredentialsException;
import exceptions.UserNotFoundException;
import exceptions.ValidationException;
import repository.JdbcUserRepository;
import repository.UserRepository;
import util.PasswordEncoder;

/**
 * 기존 UserDAO는 SQL 실행, 자원 관리, 비즈니스 규칙을 한 클래스에서 모두
 * 처리해 SRP를 위반했다. 이제 Repository가 저장/조회만 담당하고,
 * 이 클래스가 "중복 가입 차단", "비밀번호 검증", "입력값 검증" 같은
 * 도메인 규칙을 담당한다.
 *
 * 또한 기존 login()은 0/1/2 매직 넘버를 반환해, 호출부가 그 의미를 외우고
 * 있어야 했고 숫자를 잘못 비교해도 컴파일러가 잡아주지 못했다.
 * 이제 실패는 의미가 분명한 예외 타입으로 표현한다.
 */
public class UserService {

    /** 지나치게 엄격한 정규식은 정상 주소를 막으므로, 최소한의 형태만 검사한다. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;

    public UserService() {
        this(new JdbcUserRepository());
    }

    /** 생성자 주입: 테스트에서 인메모리 Fake 저장소로 교체할 수 있다. */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void signUp(String id, String rawPassword, String passwordConfirm, String name) {
        validateSignUp(id, rawPassword, passwordConfirm, name);

        // 선검사는 사용자에게 친절한 메시지를 주기 위한 것이고,
        // 동시 가입 경쟁 조건의 최종 방어선은 DB의 PK 제약이다
        // (JdbcUserRepository.save가 제약 위반을 DuplicateUserException으로 번역한다).
        if (userRepository.existsById(id)) {
            throw new DuplicateUserException(id);
        }

        String salt = PasswordEncoder.generateSalt();
        String hash = PasswordEncoder.encode(rawPassword, salt);

        userRepository.save(User.builder()
                .id(id)
                .name(name)
                .passwordHash(hash)
                .salt(salt)
                .build());
    }

    /** 로그인 성공 시 User 반환, 실패 시 예외. */
    public User login(String id, String rawPassword) {
        requireText(id, "아이디를 입력해 주세요.");
        requireText(rawPassword, "비밀번호를 입력해 주세요.");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!PasswordEncoder.matches(rawPassword, user.getSalt(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    /**
     * 회원 탈퇴.
     *
     * 기존 withdraw.jsp는 요청 파라미터로 받은 아이디를 그대로 삭제해,
     * 로그인하지 않은 사람도 남의 계정을 지울 수 있는 심각한 결함이 있었다.
     * 이제 호출부가 세션의 아이디를 넘기도록 바꾸고, 여기서 비밀번호를
     * 한 번 더 확인해 본인 확인을 강제한다.
     */
    public void withdraw(String id, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!PasswordEncoder.matches(rawPassword, user.getSalt(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrderByCreatedAtDesc();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    // ---------- 검증 ----------

    private void validateSignUp(String id, String rawPassword, String passwordConfirm, String name) {
        requireText(id, "아이디를 입력해 주세요.");
        requireText(rawPassword, "비밀번호를 입력해 주세요.");
        requireText(name, "이름을 입력해 주세요.");

        if (!EMAIL.matcher(id).matches()) {
            throw new ValidationException("아이디는 이메일 주소 형식이어야 합니다.");
        }
        if (id.length() > 128) {
            throw new ValidationException("아이디가 너무 깁니다(최대 128자).");
        }
        if (name.length() > 32) {
            throw new ValidationException("이름이 너무 깁니다(최대 32자).");
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }
        // 기존 signup.html에는 확인용 입력칸(ps2)이 있었지만 서버에서 전혀 비교하지 않았다.
        if (!rawPassword.equals(passwordConfirm)) {
            throw new ValidationException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }
}
