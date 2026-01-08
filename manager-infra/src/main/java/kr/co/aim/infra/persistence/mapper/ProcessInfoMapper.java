package kr.co.aim.infra.persistence.mapper;

import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.infra.persistence.entity.ProcessInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProcessInfoMapper {

    ProcessInfo toDomain(ProcessInfoEntity entity);

    ProcessInfoEntity toEntity(ProcessInfo domain);
}