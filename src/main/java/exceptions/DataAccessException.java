package exceptions;

/**
 * 체크 예외인 SQLException/NamingException을 상위 계층까지 그대로 전파하지 않기 위한
 * 언체크 래퍼 예외. 서비스/뷰 계층이 JDBC의 존재를 몰라도 되게 만든다(추상화 누수 방지).
 *
 * 패키지명을 'exception'이 아닌 'exceptions'로 둔 이유:
 * JSP의 에러 페이지에는 'exception'이라는 이름의 암시적 객체가 존재한다. 자바의
 * 이름 가림(obscuring) 규칙상 변수명이 패키지명을 가리므로, 패키지가 'exception'이면
 * error.jsp 안에서 'exception.DataAccessException'이 패키지가 아닌 변수로 해석되어
 * 컴파일 오류가 난다. 이름을 복수형으로 바꿔 충돌을 원천 차단했다.
 */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
