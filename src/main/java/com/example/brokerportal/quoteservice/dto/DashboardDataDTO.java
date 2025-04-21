package com.example.brokerportal.quoteservice.dto;

import com.example.brokerportal.authservice.dto.UserDTO;
import com.example.brokerportal.authservice.entities.User;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Getter
@Setter
public class DashboardDataDTO {

    private UserDTO broker;
    private List<QuoteSummaryDTO> quotes;
    private int totalQuotes;
    private int totalActiveClients;
}
