package kr.co.aim.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.aim.common.enums.MessageList;
import kr.co.aim.common.enums.ResultCode;
import kr.co.aim.common.enums.SystemName;
import kr.co.aim.common.format.ConnectionReplyBody;
import kr.co.aim.common.format.ConnectionRequestBody;
import kr.co.aim.common.format.request.BaseMessage;
import kr.co.aim.domain.repository.DB2ConnectionRepository;
import kr.co.aim.infra.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class ConnectionCheckService {

    private final DB2ConnectionRepository db2ConnectionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    // final을 유지하여 저장소 객체 자체의 변질을 방지합니다.
    private final AtomicBoolean isMantiConnected = new AtomicBoolean(false);
    private final AtomicBoolean isGalDbConnected = new AtomicBoolean(false);

    public void checkMantiConnection(BaseMessage<ConnectionReplyBody> message){
        String resultCode = message.getResultCode();

        if(StringUtils.isBlank(resultCode)){
            updateMantiStatus(false);
        }else if(StringUtils.equals( ResultCode.OK.getValue() , resultCode )){
            updateMantiStatus(true);
        }
        else if(StringUtils.equals( ResultCode.NG.getValue() , resultCode )){
            updateMantiStatus(false);
        }
        else{
            updateMantiStatus(false);
        }
    }


    // 스케줄러 등에 의해 호출될 상태 업데이트 메서드
    public void updateMantiStatus(boolean status) {
        isMantiConnected.set(status);
    }

    public void updateGalDbStatus(boolean status) {
        isGalDbConnected.set(status);
    }

    // 외부(Controller 등)에서 상태를 확인할 때 사용하는 메서드
    public boolean getMantiStatus() {
        return isMantiConnected.get();
    }

    public boolean getGalDbStatus() {
        return isGalDbConnected.get();
    }

    @Transactional(value = "db2TransactionManager",propagation = Propagation.REQUIRES_NEW)
    public void checkDb2Connection() {
        try {
            db2ConnectionRepository.checkConnection();
            updateGalDbStatus(true);
        } catch (Exception e) {
            updateGalDbStatus(false);
        }
    }

    // 동기 방식의 manti connectionCheck
    public void checkMantiConnection(){
        try {
            BaseMessage<ConnectionRequestBody> request = messageBuilder();

            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitConfig.EXCHANGE_DEAD,
                    RabbitConfig.ROUTING_DEAD,
                    request
            );

            if (ObjectUtils.isEmpty(reply)) {
                updateMantiStatus(false);
                return;
            }

            BaseMessage<ConnectionReplyBody> response;
            TypeReference<BaseMessage<ConnectionReplyBody>> typeRef = new TypeReference<>() {};

            // 2. 안전한 타입 변환 로직
            if (reply instanceof byte[]) {
                // 혹시나 컨버터가 작동 안 해서 raw 데이터가 온 경우
                response = objectMapper.readValue((byte[]) reply, typeRef);
            } else {
                // Jackson 컨버터가 이미 Map 등으로 변환해놓은 경우 (가장 확률 높음)
                // convertValue는 Map을 DTO 객체로 다시 매핑해주는 아주 똑똑한 메서드입니다.
                response = objectMapper.convertValue(reply, typeRef);
            }

            // 3. 비즈니스 로직 검증 (예: 응답 코드 확인)
            if ( ObjectUtils.isEmpty(response)  &&  StringUtils.equals(ResultCode.OK.getValue() ,response.getResultCode())) {
                updateMantiStatus(true);
            } else {
                updateMantiStatus(false);
            }

        }catch (Exception e) {
            updateMantiStatus(false);
        }
    }

    private BaseMessage<ConnectionRequestBody> messageBuilder(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        // 2. LocalDateTime을 문자열로 변환
        String transactionId = now.format(formatter);

        BaseMessage<ConnectionRequestBody> request = new BaseMessage<>();
        ConnectionRequestBody body = new ConnectionRequestBody();
        request.setEventTime(now.toString());
        request.setMessageFrom(SystemName.MNG.getValue());
        request.setMessageName(MessageList.CONNECTION.getMessageName());
        request.setMessageOwner(SystemName.MNG.getValue());
        request.setMessageTo(SystemName.MANTI.getValue());
        request.setResultCode(ResultCode.OK.getValue());
        request.setResultMessage("");
        request.setTransactionId(transactionId);
        request.setBody(body);
        return request;
    }


}
