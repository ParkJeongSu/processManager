package kr.co.aim.common.error;

public class EntityExistException extends RuntimeException{
    // 기본 생성자
    public EntityExistException() {
        super();
    }

    // 메시지만 받는 생성자
    public EntityExistException(String message) {
        super(message);
    }

    // 메시지와 원인 예외를 함께 받는 생성자
    public EntityExistException(String message, Throwable cause) {
        super(message, cause);
    }

    // 원인 예외만 받는 생성자
    public EntityExistException(Throwable cause) {
        super(cause);
    }

    public EntityExistException(Class<?> entityClass, Object id) {
        super(String.format("'%s' 엔티티를 ID '%s'(으)로 이미 존재합니다.", entityClass.getSimpleName(), id.toString()));
    }
}
