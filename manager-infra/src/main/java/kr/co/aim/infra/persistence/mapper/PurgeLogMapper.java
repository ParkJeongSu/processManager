package kr.co.aim.infra.persistence.mapper;

import kr.co.aim.domain.model.PurgeLog;
import kr.co.aim.infra.persistence.entity.PurgeLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PurgeLogMapper {

    PurgeLog toDomain(PurgeLogEntity entity);

    PurgeLogEntity toEntity(PurgeLog domain);
}