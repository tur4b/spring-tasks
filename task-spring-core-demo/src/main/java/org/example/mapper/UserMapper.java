package org.example.mapper;

import org.example.dto.request.UserCreateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }

    public User toEntity(UserCreateRequest createRequest) {
        User user = new User();
        user.setFirstName(createRequest.firstName());
        user.setLastName(createRequest.lastName());
        return user;
    }
}
