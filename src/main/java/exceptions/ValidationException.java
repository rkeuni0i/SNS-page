package exceptions;

/** 입력값이 비어 있거나 형식이 올바르지 않을 때 발생. */
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
