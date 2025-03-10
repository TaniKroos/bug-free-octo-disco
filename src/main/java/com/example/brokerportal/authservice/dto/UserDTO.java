package com.example.brokerportal.authservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
//    public UserDTO(String firstName, String lastName, String email, Long id) {
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.email = email;
//        this.id = id;
//    }

}
