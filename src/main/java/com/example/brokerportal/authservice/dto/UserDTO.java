package com.example.brokerportal.authservice.dto;

import jakarta.validation.constraints.*;
import com.example.brokerportal.authservice.entities.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserDTO {

    private Long id;


    private String firstName;


    private String lastName;


    private String email;

    // Constructor to create UserDTO from User entity
    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }
}
