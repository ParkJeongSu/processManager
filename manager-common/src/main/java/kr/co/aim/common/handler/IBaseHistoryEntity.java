package kr.co.aim.common.handler;

import java.time.LocalDateTime;
import java.util.Date;
public interface IBaseHistoryEntity {
    Long getId();
    String getEventName();
    LocalDateTime getEventTime();
    String getEventUser();
    String getEventComment();
}
