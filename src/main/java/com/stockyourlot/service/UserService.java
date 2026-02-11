package com.stockyourlot.service;

import com.stockyourlot.entity.Role;
import com.stockyourlot.entity.User;
import com.stockyourlot.repository.RoleRepository;
import com.stockyourlot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User setUserRoles(String email, List<String> roleNames) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));

        Set<Role> newRoles = new HashSet<>();
        for (String name : roleNames != null ? roleNames : List.<String>of()) {
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + name));
            newRoles.add(role);
        }

        user.setRoles(newRoles);
        return userRepository.save(user);
    }
}
