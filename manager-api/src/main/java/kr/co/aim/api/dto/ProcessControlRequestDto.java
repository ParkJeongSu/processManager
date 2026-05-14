package kr.co.aim.api.dto;


import kr.co.aim.common.vo.ProcessControlRequestVo;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@AllArgsConstructor
public class ProcessControlRequestDto {
    private String userId;
    private String eventName;
    private LocalDateTime eventTime;
    private String eventUser;
    private String eventComment;

    public static ProcessControlRequestVo toVo(ProcessControlRequestDto dto) {
        return ProcessControlRequestVo
                .builder()
                .userId(dto.getUserId())
                .eventName(dto.getEventName())
                .eventTime(dto.getEventTime())
                .eventUser(dto.getEventUser())
                .eventComment(dto.getEventComment())
                .build();
    }
}