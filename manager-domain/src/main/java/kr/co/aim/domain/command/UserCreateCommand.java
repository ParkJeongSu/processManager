package kr.co.aim.domain.command;

import kr.co.aim.common.record.TransactionInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@RequiredArgsConstructor
@Builder
public class UserCreateCommand {
    private final TransactionInfo transactionInfo;
    private final String userId;
    private final Long authorityId;
    private final String userName;
    private final String password;
    private final String email;
    private final String phone1;
    private final String phone2;
    private final String refreshToken;
    private final String checkOutState;
    private final LocalDateTime checkOutTime;
    private final String checkOutUser;
    private final String dataState;
}
