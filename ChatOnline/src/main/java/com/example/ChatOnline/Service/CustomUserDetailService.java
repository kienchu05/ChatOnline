package com.example.ChatOnline.Service;

import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.example.ChatOnline.Repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
