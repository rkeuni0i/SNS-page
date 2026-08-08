package exceptions;

/**
 * 비밀번호가 일치하지 않을 때 발생.
 *
 * 메시지에 아이디를 포함하지 않는다. 로그인 실패 응답에서 "아이디 없음"과
 * "비밀번호 불일치"를 지나치게 구체적으로 구분해 노출하면 공격자가 가입된
 * 아이디 목록을 수집(user enumeration)할 수 있기 때문이다.
 */
public class InvalidCredentialsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException() {
        super("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
