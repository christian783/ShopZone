package com.shopzone.user.config;

import com.shopzone.user.domain.PrivilegeNames;
import com.shopzone.user.domain.Role;
import com.shopzone.user.domain.User;
import com.shopzone.user.repository.RoleRepository;
import com.shopzone.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties bootstrapProperties;

    public AdminBootstrap(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            BootstrapProperties bootstrapProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = bootstrapProperties.getAdminEmail();
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
            return;
        }
        Role adminRole = roleRepository.findByName(PrivilegeNames.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing; Flyway seed required"));
        User admin = new User();
        admin.setEmail(email.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(bootstrapProperties.getAdminPassword()));
        admin.setFullName(bootstrapProperties.getAdminName());
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        log.info("Bootstrapped admin user {}", email);
    }
}
