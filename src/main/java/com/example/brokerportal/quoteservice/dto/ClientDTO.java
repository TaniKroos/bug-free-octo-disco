package com.example.brokerportal.quoteservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private String clientName;
    private String businessType;
    private String industryType;
    private String contactNumber;
    private String email;
    private String address;
}
