package com.example.brokerportal.authservice.service;

import com.example.brokerportal.authservice.dto.UserDTO;
import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<UserDTO> getUserDTOByEmail(String email) {
        return userRepository.findByEmail(email).map(user ->
                new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName())        );
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
