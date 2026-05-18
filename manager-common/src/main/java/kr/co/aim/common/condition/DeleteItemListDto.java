package kr.co.aim.common.condition;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class DeleteItemListDto {
    private List<Long> ids;
}