package kr.co.aim.common.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public void writePrettyJson(String jsonString) {
        // --- JSON 예쁘게 로그 찍기 ---
        try {
            Object jsonObject = objectMapper.readValue(jsonString, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            log.info("\n=== [Message Body] ===\n{}", prettyJson);
        } catch (Exception e) {
            log.warn("Failed to pretty print JSON, logging raw string: {}", jsonString);
        }
        // ----------------------------
    }

    public void writePrettyJson(Object jsonObject) {
        // 입력값이 null인 경우 예외 처리
        if (jsonObject == null) {
            log.warn("Input object is null. Cannot print JSON.");
            return;
        }

        try {
            // 객체를 바로 Pretty Print 문자열로 변환
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonObject);

            log.info("\n=== [Message Body] ===\n{}", prettyJson);
        } catch (Exception e) {
            // 변환 실패 시 toString() 등을 활용해 최소한의 정보라도 출력
            log.warn("Failed to pretty print Object: {}", jsonObject.toString());
        }
    }

    // AMQP 메시지 전용 오버로딩
    public void writePrettyJson(org.springframework.amqp.core.Message message) {
        if (message == null || message.getBody() == null) {
            log.warn("AMQP Message or Body is null.");
            return;
        }

        try {
            // 1. byte[]를 String으로 변환
            String jsonString = new String(message.getBody(), StandardCharsets.UTF_8);

            // 2. String을 Object로 읽어서 (이미 만들어둔 메서드 호출)
            writePrettyJson(jsonString);

        } catch (Exception e) {
            log.warn("Failed to process AMQP message body for pretty print.");
        }
    }
}