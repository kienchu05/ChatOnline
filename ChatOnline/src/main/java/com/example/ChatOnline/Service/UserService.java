package com.example.ChatOnline.Service;

import com.example.ChatOnline.DTO.Request.CreateUserRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.CreateUserResponse;
import com.example.ChatOnline.DTO.Response.PageResponse;
import com.example.ChatOnline.DTO.Response.UserDetailResponse;
import com.example.ChatOnline.Entity.Role;
import com.example.ChatOnline.Entity.User;
import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.example.ChatOnline.Repository.RoleRepository;
import com.example.ChatOnline.Repository.UserRepository;
import jdk.jshell.spi.ExecutionControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final RoleRepository roleRepository;
    private  final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        if(userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .email(createUserRequest.getEmail())
                .username(createUserRequest.getUsername())
                .password(passwordEncoder.encode(createUserRequest.getPassword()))
                .build();

        Role role = roleRepository.findByName("USER_ROLE")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER_ROLE").build()));
        user.addRole(role);

        userRepository.save(user);

        return CreateUserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public UserDetailResponse myInfo(String userId) {
        // Tìm user theo userId từ JWT token
        return userRepository.findById(userId)
                .map(user -> UserDetailResponse.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .build())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public PageResponse<UserDetailResponse> searchUsers(String keyword, int page, int size){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        String userId = authentication.getName();

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<User> users;
        if(keyword == null || keyword.isBlank()){
            users = userRepository.findAll(pageable);
        }else{
            users = userRepository.searchUsers(keyword, pageable);
        }

        List<UserDetailResponse> content = users.getContent()
                .stream().filter(user -> !user.getId().equals(userId))
                .map(user -> UserDetailResponse.builder()
                        .email(user.getEmail())
                        .userId(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();

        return PageResponse.<UserDetailResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .content(content)
                .build();
    }
}
