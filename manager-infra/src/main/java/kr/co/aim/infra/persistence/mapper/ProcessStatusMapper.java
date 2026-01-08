package kr.co.aim.infra.persistence.mapper;

import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.infra.persistence.entity.ProcessStatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProcessStatusMapper {

    ProcessStatus toDomain(ProcessStatusEntity entity);

    ProcessStatusEntity toEntity(ProcessStatus domain);
}