package kr.co.aim.common.handler;

public interface MessageHandler<T> {
    Object handle(T message); // 메시지를 처리하는 메소드
    String getSupportedMessageName(); // 자신이 처리할 MessageName을 반환
}
