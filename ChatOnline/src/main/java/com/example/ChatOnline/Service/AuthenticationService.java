package com.example.ChatOnline.Service;

import com.example.ChatOnline.DTO.Request.LoginRequest;
import com.example.ChatOnline.DTO.Response.LoginResponse;
import com.example.ChatOnline.Entity.User;
import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.example.ChatOnline.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest loginRequest){
        log.info("User request body login : ");
        log.info("Login response body : {}", loginRequest);

        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        User user = (User) authentication.getPrincipal();

        if(user == null){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(user.getId(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return LoginResponse.builder()
                .message("Login successfully !")
                .status(HttpStatus.OK.value())
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
