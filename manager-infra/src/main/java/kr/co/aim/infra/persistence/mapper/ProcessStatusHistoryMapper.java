package kr.co.aim.infra.persistence.mapper;

import kr.co.aim.domain.model.ProcessStatusHistory;
import kr.co.aim.infra.persistence.entity.ProcessStatusHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProcessStatusHistoryMapper {

    ProcessStatusHistory toDomain(ProcessStatusHistoryEntity entity);

    ProcessStatusHistoryEntity toEntity(ProcessStatusHistory domain);
}