package kr.co.aim.domain.command;

import kr.co.aim.common.record.TransactionInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class UserUpdateCommand {
    private final TransactionInfo transactionInfo;
    private final Long authorityId;
    private final String userName;
    private final String password;
    private final String email;
    private final String phone1;
    private final String phone2;
}
