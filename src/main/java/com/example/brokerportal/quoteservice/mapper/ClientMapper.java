package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.ClientDTO;
import com.example.brokerportal.quoteservice.entities.Client;

public class ClientMapper {

    public static ClientDTO toDTO(Client client) {
        if (client == null) return null;

        return ClientDTO.builder()
                .id(client.getId())
                .clientName(client.getClientName())
                .email(client.getEmail())
                .contactNumber(client.getContactNumber())
                .address(client.getAddress())
                .build();
    }

    public static Client toEntity(ClientDTO dto) {
        if (dto == null) return null;

        return Client.builder()
                .clientName(dto.getClientName())
                .email(dto.getEmail())
                .contactNumber(dto.getContactNumber())
                .address(dto.getAddress())
                .build();
    }
}
