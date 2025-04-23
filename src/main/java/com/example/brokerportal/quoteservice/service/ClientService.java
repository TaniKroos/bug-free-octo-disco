package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.ClientDTO;

import java.util.List;

public interface ClientService {
    List<ClientDTO> findByEmail(String email);
//    ClientDTO createClient(ClientDTO dto);
//    ClientDTO updateClient(Long id, ClientDTO dto);
}
