package com.shopzone.user.user;

import com.shopzone.user.api.dto.CreateUserRequest;
import com.shopzone.user.api.dto.UpdateUserRequest;
import com.shopzone.user.api.dto.UserResponse;
import com.shopzone.user.web.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserAdminService {

    PageResponse<UserResponse> list(String email, Boolean enabled, Pageable pageable);

    UserResponse get(UUID id);

    UserResponse create(CreateUserRequest request);

    UserResponse update(UUID id, UpdateUserRequest request);

    void delete(UUID id);
}
