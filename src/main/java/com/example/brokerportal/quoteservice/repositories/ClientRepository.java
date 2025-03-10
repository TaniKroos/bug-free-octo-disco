package com.example.brokerportal.quoteservice.repositories;



import com.example.brokerportal.quoteservice.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByBrokerId(Long brokerId); // Get clients of a broker
}

