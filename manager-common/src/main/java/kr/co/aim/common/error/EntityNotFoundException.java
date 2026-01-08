package kr.co.aim.common.error;

public class EntityNotFoundException extends RuntimeException{
    // 기본 생성자
    public EntityNotFoundException() {
        super();
    }

    // 메시지만 받는 생성자
    public EntityNotFoundException(String message) {
        super(message);
    }

    // 메시지와 원인 예외를 함께 받는 생성자
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // 원인 예외만 받는 생성자
    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }

    public EntityNotFoundException(Class<?> entityClass, Object id) {
        super(String.format("'%s' 엔티티를 ID '%s'(으)로 찾을 수 없습니다.", entityClass.getSimpleName(), id.toString()));
    }
}
