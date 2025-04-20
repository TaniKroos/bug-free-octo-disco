package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.quoteservice.dto.ClientDTO;
import com.example.brokerportal.quoteservice.entities.Client;

public class ClientMapper {

    public static ClientDTO toDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .clientName(client.getClientName())
                .businessType(client.getBusinessType())
                .industryType(client.getIndustryType())
                .contactNumber(client.getContactNumber())
                .email(client.getEmail())
                .address(client.getAddress())
                .brokerId(client.getBroker() != null ? client.getBroker().getId() : null)
                .build();
    }

    public static Client toEntity(ClientDTO dto, User broker) {
        Client client = new Client();
        client.setId(dto.getId());
        client.setClientName(dto.getClientName());
        client.setBusinessType(dto.getBusinessType());
        client.setIndustryType(dto.getIndustryType());
        client.setContactNumber(dto.getContactNumber());
        client.setEmail(dto.getEmail());
        client.setAddress(dto.getAddress());
        client.setBroker(broker);
        return client;
    }
}

