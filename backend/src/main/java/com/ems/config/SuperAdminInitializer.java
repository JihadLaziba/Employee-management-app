package com.ems.config;

import com.ems.enums.Role;
import com.ems.entity.User;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminInitializer implements ApplicationRunner {

    private final AppProperties props;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (props.superAdmins() == null) {
            return;
        }
        for (AppProperties.SuperAdmin admin : props.superAdmins()) {
            if (admin.email() == null || admin.email().isBlank()) {
                continue;
            }
            if (userRepository.existsByEmailIgnoreCase(admin.email())) {
                log.info("Super admin already exists: {}", admin.email());
                continue;
            }
            userRepository.save(User.builder()
                    .name(admin.name())
                    .email(admin.email().trim().toLowerCase())
                    .password(passwordEncoder.encode(admin.password()))
                    .role(Role.SUPER_ADMIN)
                    .enabled(true)
                    .build());
            log.info("Super admin created: {}", admin.email());
        }
    }
}
