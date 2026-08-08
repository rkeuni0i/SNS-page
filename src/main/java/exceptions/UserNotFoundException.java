package exceptions;

/** 존재하지 않는 아이디로 로그인/조회/탈퇴를 시도할 때 발생. */
public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String userId) {
        super("존재하지 않는 아이디입니다: " + userId);
    }
}
