package org.example.webapplication.service;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final UserRepository userRepository;

    public User getUser(
            List<PermissionKey> key,
            PermissionType type
    ) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean allowed = user.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(p ->
                        key.contains(p.getPermission_key()) &&
                                p.getPermission_type() == type
                );

        if (!allowed) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        return user;
    }
}
