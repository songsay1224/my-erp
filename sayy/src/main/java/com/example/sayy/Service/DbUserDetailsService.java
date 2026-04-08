package com.example.sayy.Service;

import com.example.sayy.Entity.UserEntity;
import com.example.sayy.Mapper.UserMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    public DbUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userMapper.selectByUsername(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        String role = (userEntity.getRole() == null || userEntity.getRole().isBlank()) ? "USER" : userEntity.getRole();

        return User.withUsername(userEntity.getUsername())
                .password(userEntity.getPwd())
                .roles(role) // role="ADMIN" -> authority="ROLE_ADMIN"
                .disabled(!userEntity.isEnabled())
                .build();
    }
}


