package kr.co.aim.infra.persistence.mapper;

import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.domain.model.ProcessStatusHistory;
import kr.co.aim.infra.persistence.entity.ProcessStatusHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = { kr.co.aim.common.Utils.TsidUtils.class} // [핵심] 이 부분을 추가하세요!
)
public interface ProcessStatusHistoryMapper {

    ProcessStatusHistory toDomain(ProcessStatusHistoryEntity entity);

    ProcessStatusHistoryEntity toEntity(ProcessStatusHistory domain);

    @Mapping(target = "id", expression = "java(TsidUtils.nextId())") // [3] 자바 코드 호출!
    ProcessStatusHistory toHistoryEntity(ProcessStatus domain);
}