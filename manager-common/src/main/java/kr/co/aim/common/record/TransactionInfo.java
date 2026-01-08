package kr.co.aim.common.record;

import java.time.LocalDateTime;
import java.util.Date;

public record TransactionInfo(
        String eventName,

        LocalDateTime eventTime,
        String eventUser,
        String eventComment
) {
    public static TransactionInfo now(String eventName, String user, String comment) {
        return new TransactionInfo(eventName, LocalDateTime.now(), user, comment);
    }
    public static TransactionInfo now(String eventName, String user, String comment,LocalDateTime date){
        return new TransactionInfo(eventName, date, user, comment);
    }
}