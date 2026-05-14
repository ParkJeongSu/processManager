package kr.co.aim.infra.persistence.mapper;

import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.infra.persistence.entity.PurgeConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PurgeConfigMapper {

    PurgeConfig toDomain(PurgeConfigEntity entity);

    PurgeConfigEntity toEntity(PurgeConfig domain);
}