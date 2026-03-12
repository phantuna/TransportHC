package org.example.webapplication.service.cache;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.entity.User;
import org.example.webapplication.repository.user.UserRepository;
import org.example.webapplication.service.UserService;
import org.example.webapplication.service.mapper.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final UserRepository userRepository;
    private final UserMapper  userMapper;
    @Cacheable(
            value = "users_list",
            key = "'page:' + #page + ':size:' + #size"
    )
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> pageResult =
                userRepository.findAllByRole_Id("R_DRIVER", pageable);

        return PageResponse.<UserResponse>builder()
                .content(pageResult.getContent()
                        .stream()
                        .map(userMapper::toResponse)
                        .toList())
                .page(page)
                .size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }
}
