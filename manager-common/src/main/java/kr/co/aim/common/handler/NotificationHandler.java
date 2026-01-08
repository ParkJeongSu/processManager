package kr.co.aim.common.handler;

public interface NotificationHandler {
    void send(String to,String from, String subject, String content);
}
