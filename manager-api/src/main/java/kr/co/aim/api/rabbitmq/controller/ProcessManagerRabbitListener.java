package kr.co.aim.api.rabbitmq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.aim.api.rabbitmq.controller.dispatcher.MessageDispatcher;
import kr.co.aim.common.Utils.JsonUtils;
import kr.co.aim.common.format.request.MessageHeader;
import kr.co.aim.common.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessManagerRabbitListener {

    private final MessageDispatcher messageDispatcher;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final JsonUtils jsonUtils;

    @RabbitListener(
            id = "manager-Listener",
            queues= "${custom.rabbitmq.queue.manager}"
    )
    public Object process(org.springframework.amqp.core.Message message) {

        try {
            // 1. 바디를 꺼내서 직접 String으로 변환
            String jsonString = new String(message.getBody(), StandardCharsets.UTF_8);

            jsonUtils.writePrettyJson(jsonString);

            String correlation = message.getMessageProperties().getCorrelationId();
            String reply = message.getMessageProperties().getReplyTo();

            log.info("correlation: {}",correlation);
            log.info("reply: {}",reply);

            MessageHeader messageHeader = objectMapper.readValue(jsonString, MessageHeader.class);
            //String messageName = messageHeader.getHeader().getMessageName();
            String messageName = messageHeader.getMessageName();
            log.info("messageName : {}", messageName);
            // 2. Dispatcher를 통해 적절한 핸들러 찾기
            MessageHandler<String> handler = messageDispatcher.getHandler(messageName);

            Object replyObject = null;
            if (handler != null) {
                // 3. 핸들러에게 작업 위임
                replyObject = handler.handle(jsonString);
            } else {
                log.warn("⚠️ No handler found for messageName: {}", messageName);
            }

            if (replyObject != null) {
                // 1. 응답 시 요청의 correlationId를 그대로 유지해야 함
                String correlationId = message.getMessageProperties().getCorrelationId();
                String replyTo = message.getMessageProperties().getReplyTo();

                if (replyTo != null) {
                    log.info("🚀 Replying to queue: {} with correlationId: {}", replyTo, correlationId);

                    // 2. replyTo 주소를 Routing Key로 사용 (Exchange는 기본 익스체인지 "" 사용)
                    rabbitTemplate.convertAndSend("", replyTo, replyObject, m -> {
                        m.getMessageProperties().setCorrelationId(correlationId);
                        return m;
                    });
                }
            }
        }
        catch (Exception e) {
            log.error("❌ [비동기 시스템 에러] 메시지 처리 중 오류가 발생하여 작업을 롤백합니다. (비동기이므로 응답 생략) 원인: {}", e.getMessage(), e);
        }

        return null;
    }
}
