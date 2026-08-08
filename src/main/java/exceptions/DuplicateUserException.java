package exceptions;

/** 이미 존재하는 아이디로 회원가입을 시도할 때 발생. */
public class DuplicateUserException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateUserException(String userId) {
        super("이미 가입된 아이디입니다: " + userId);
    }
}
