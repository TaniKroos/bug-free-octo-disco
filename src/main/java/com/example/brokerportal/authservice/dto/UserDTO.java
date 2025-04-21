package com.example.brokerportal.authservice.dto;

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
    public UserDTO(User broker) {
        this.id = broker.getId();
        this.firstName = broker.getFirstName();
        this.lastName = broker.getLastName();
        this.email = broker.getEmail();
    }

}
