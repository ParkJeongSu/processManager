package kr.co.aim.api.rabbitmq.controller.dispatcher;


import jakarta.annotation.PostConstruct;
import kr.co.aim.common.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (DI)
public class MessageDispatcher {

    // Spring이 MessageHandler 인터페이스를 구현한 모든 빈을 여기에 주입해 줍니다.
    private final List<MessageHandler<String>> handlers;
    private Map<String, MessageHandler<String>> handlerMap;


    // 의존성 주입이 완료된 후, 리스트를 맵으로 변환하여 초기화
    @PostConstruct
    public void init() {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(MessageHandler::getSupportedMessageName, Function.identity()));
    }

    // messageName에 맞는 핸들러를 찾아 반환
    public MessageHandler<String> getHandler(String messageName) {
        return handlerMap.get(messageName);
    }

}
