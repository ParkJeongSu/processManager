package kr.co.aim.infra.persistence.mapper;

import kr.co.aim.common.dto.UserResponseDto;
import kr.co.aim.domain.model.User;
import kr.co.aim.infra.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User domain);

    UserResponseDto toResponseDto(User domain);
}