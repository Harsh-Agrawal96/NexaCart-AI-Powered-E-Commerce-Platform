package com.n11bootcamp.user_service.mapper;

import com.n11bootcamp.user_service.dto.response.UserResponse;
import com.n11bootcamp.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    UserResponse toResponse(User user);
}
