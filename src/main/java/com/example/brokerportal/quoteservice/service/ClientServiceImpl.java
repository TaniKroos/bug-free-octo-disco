package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.UserRepository;
import com.example.brokerportal.quoteservice.dto.ClientDTO;
import com.example.brokerportal.quoteservice.entities.Client;
import com.example.brokerportal.quoteservice.mapper.ClientMapper;
import com.example.brokerportal.quoteservice.repositories.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final UserRepository userRepository;
    @Override
    public List<ClientDTO> findByEmail(String email) {
        List<Client> clients = clientRepository.findByEmailIgnoreCase(email);
        return clients.stream()
                .map(ClientMapper::toDTO)
                .collect(Collectors.toList());
    }



//    @Override
//    public ClientDTO createClient(ClientDTO dto) {
//        User broker = userRepository.findById(dto.getBrokerId())
//                .orElseThrow(() -> new EntityNotFoundException("Broker not found"));
//
//        Client client = ClientMapper.toEntity(dto, broker);
//        client.setId(null); // Ensure it's a new entity
//
//        Client saved = clientRepository.save(client);
//        return ClientMapper.toDTO(saved);
//    }


//    @Override
//    public ClientDTO updateClient(Long id, ClientDTO dto) {
//        Client existing = clientRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
//
//        existing.setClientName(dto.getClientName());
//        existing.setBusinessType(dto.getBusinessType());
//        existing.setIndustryType(dto.getIndustryType());
//        existing.setContactNumber(dto.getContactNumber());
//        existing.setEmail(dto.getEmail());
//        existing.setAddress(dto.getAddress());
//
//        // Optional: Update broker if dto.getBrokerId() is different
//        if (dto.getBrokerId() != null &&
//                (existing.getBroker() == null || !existing.getBroker().getId().equals(dto.getBrokerId()))) {
//            User broker = userRepository.findById(dto.getBrokerId())
//                    .orElseThrow(() -> new EntityNotFoundException("Broker not found"));
//            existing.setBroker(broker);
//        }
//
//        Client saved = clientRepository.save(existing);
//        return ClientMapper.toDTO(saved);
//    }

}

