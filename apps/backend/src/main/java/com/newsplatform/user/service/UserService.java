package com.newsplatform.user.service;

import com.newsplatform.user.dto.UserResponse;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUser(UUID userId) {
        return UserResponse.from(requireUser(userId));
    }

    @Transactional(readOnly = true)
    public User requireUser(UUID userId) {
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
