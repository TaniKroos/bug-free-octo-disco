package com.example.brokerportal.quoteservice.repositories;



import com.example.brokerportal.quoteservice.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByBrokerId(Long brokerId); // Get clients of a broker
    List<Client> findByEmailIgnoreCase(String email);
    Optional<Client> findByClientNameAndEmailAndContactNumber(String clientName, String email, String contactNumber);
}

