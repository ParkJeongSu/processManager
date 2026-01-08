package kr.co.aim.common.handler;

import org.springframework.amqp.core.Message;

public interface MessageWorker {
	/**
     * 회사 미들웨어에서 호출할 공통 처리 메서드
     */
    Object process(Message message);
}
