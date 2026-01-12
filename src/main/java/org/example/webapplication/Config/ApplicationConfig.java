package org.example.webapplication.Config;

import org.example.webapplication.Entity.User;
import org.example.webapplication.Entity.Role_Permission.Role;
import org.example.webapplication.Repository.RoleRepository;
import org.example.webapplication.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class ApplicationConfig {
    BCryptPasswordEncoder passwordEndcoder = new BCryptPasswordEncoder();


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Bean
    ApplicationRunner applicationRunner(){
        return args -> {
            System.out.println(LocalDate.now());


            Role adminRole = roleRepository.findById("R_ADMIN")
                    .orElseGet(() -> {
                        Role r = Role.builder()
                                .name("R_ADMIN")
                                .description("Administrator role")
                                .build();
                        return roleRepository.save(r);
                    });
            if(!userRepository.existsByUsername("admin")){
                User user = User.builder()
                        .username("admin")
                        .password(passwordEndcoder.encode("admin"))
                        .roles(List.of(adminRole))
                        .build();


                userRepository.save(user);
            }
        };
    }
}
