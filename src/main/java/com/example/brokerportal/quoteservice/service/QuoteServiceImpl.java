package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.UserRepository;
import com.example.brokerportal.authservice.service.UserService;
import com.example.brokerportal.quoteservice.dto.ClientDTO;
import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Client;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.mapper.ClientMapper;
import com.example.brokerportal.quoteservice.mapper.QuoteMapper;
import com.example.brokerportal.quoteservice.repositories.ClientRepository;
import com.example.brokerportal.quoteservice.repositories.CyberInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService{
    private final QuoteRepository quoteRepository;
    private final ClientRepository  clientRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final QuoteInsuranceServiceImpl quoteInsuranceService;
    private final CyberInsuranceRepository cyberInsuranceRepository;
    @Override
    @Transactional
    public QuoteDTO createQuote(QuoteDTO quoteDTO){

        System.out.println("===> Received QuoteDTO in createQuote()");
        System.out.println("Status: " + quoteDTO.getStatus());
        System.out.println("Client: " + quoteDTO.getClient());
        System.out.println("Insurances: " + quoteDTO.getInsurances());

        Quote quote = QuoteMapper.toEntity(quoteDTO);
        quote.setCreatedAt(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());
        quote.setBroker(userService.getCurrentUser()); // ✅ correct place

        if (quoteDTO.getClient() != null) {
            Client client = ClientMapper.toEntity(quoteDTO.getClient());
            client = clientRepository.save(client); // ID is auto-generated here
            quote.setClient(client);
        }
        Quote saved = quoteRepository.save(quote);
        if(quoteDTO.getInsurances() != null && !quoteDTO.getInsurances().isEmpty()){
            quoteInsuranceService.mapAndAttachInsurancesToQuote(quote,quoteDTO.getInsurances());
        }

        return QuoteMapper.toDTO(saved);
    }

    @Override
    public QuoteDTO getQuoteById(Long id) {


        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote with this id:" + id + " doesn't exist in the database"));

        authorizeBrokerAccess(quote);
        if(quote.isDeleted()){
            throw new ResourceNotFoundException("QUote with this id has been marked soft deleted");
        }

        return QuoteMapper.toDTO(quote);
    }

    @Override
    @Transactional
    public QuoteDTO updateQuote(Long id, QuoteDTO updatedQuoteDto) {


        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote with this id: " + id + " doesn't exist"));



        authorizeBrokerAccess(quote);
        if(quote.isDeleted()){
            throw new ResourceNotFoundException("Quote with this id is deleted");
        }
        if (updatedQuoteDto.getClient() != null) {
            updateClientDetails(quote.getClient(), updatedQuoteDto.getClient());
        }

        if (updatedQuoteDto.getStatus() != null) {
            quote.setStatus(updatedQuoteDto.getStatus());
        }

        quote.setUpdatedAt(LocalDateTime.now());

        if (updatedQuoteDto.getInsurances() != null && !updatedQuoteDto.getInsurances().isEmpty()) {
            updateInsurancesSelection(quote, updatedQuoteDto.getInsurances());
        }

        Quote updatedQuote = quoteRepository.save(quote);
        return QuoteMapper.toDTO(updatedQuote);
    }

    public List<QuoteDTO> findByBrokerIdAndDeletedTrue(){
        Long userId = userService.getCurrentUser().getId();
        List<Quote> quotes = quoteRepository.findByBrokerIdAndDeletedTrue(userId);
        return quotes.stream().map(QuoteMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void softDeleteQuote(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + id));
        authorizeBrokerAccess(quote);

        quote.setDeleted(true);
        quote.setUpdatedAt(LocalDateTime.now());

        // 2. Soft delete all quote_insurance entities linked to this quote
        if (quote.getInsurances() != null && !quote.getInsurances().isEmpty()) {
            for (QuoteInsurance quoteInsurance : quote.getInsurances()) {
                quoteInsurance.setSelected(false); // soft delete quote_insurance

                // 3. Soft delete CYBER insurance if exists
                if ("CYBER".equalsIgnoreCase(quoteInsurance.getInsuranceType())
                        && quoteInsurance.getCyberInsurance() != null) {
                    quoteInsurance.getCyberInsurance().setDeleted(true);
                    cyberInsuranceRepository.save(quoteInsurance.getCyberInsurance());
                }

                // 🔸 Add similar logic here for PROPERTY, EMPLOYEE, etc., when those are implemented
            }
        }

        // 4. Save the quote (cascades QuoteInsurance soft delete if mapped with CascadeType.ALL)
        quoteRepository.save(quote);

    }

    @Override
    public List<QuoteDTO> getQuotesByBrokerId() {
        User broker = userService.getCurrentUser();
        Long brokerId = broker.getId();
        List<Quote> quotes = quoteRepository.findByBrokerIdAndDeletedFalse(brokerId);
        return quotes.stream().map(QuoteMapper::toDTO).collect(Collectors.toList());
    }

    // To authorize the broker
    private void authorizeBrokerAccess(Quote quote) {
        User user = userService.getCurrentUser();
        if (!quote.getBroker().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to access or modify this quote");
        }
    }

    // to handle soft deletion of insurances
    private void updateInsurancesSelection(Quote quote, List<QuoteInsuranceDTO> updatedInsurances) {
        Map<String, Boolean> selectionMap = updatedInsurances.stream()
                .collect(Collectors.toMap(QuoteInsuranceDTO::getInsuranceType, QuoteInsuranceDTO::isSelected));

        for (QuoteInsurance qi : quote.getInsurances()) {
            String insuranceType = qi.getInsuranceType();

            if (selectionMap.containsKey(insuranceType)) {
                boolean selected = selectionMap.get(insuranceType);
                qi.setSelected(selected);

                if ("CYBER".equalsIgnoreCase(insuranceType) && qi.getCyberInsurance() != null) {
                    if (selected && Boolean.TRUE.equals(qi.getCyberInsurance().getDeleted())) {
                        // Restore CyberInsurance
                        qi.getCyberInsurance().setDeleted(false);
                        cyberInsuranceRepository.save(qi.getCyberInsurance());
                    } else if (!selected && Boolean.FALSE.equals(qi.getCyberInsurance().getDeleted())) {
                        // Soft delete CyberInsurance
                        qi.getCyberInsurance().setDeleted(true);
                        cyberInsuranceRepository.save(qi.getCyberInsurance());
                    }
                }
            }
        }

        // Add new insurance types if they don’t exist
        updatedInsurances.forEach(dto -> {
            boolean alreadyPresent = quote.getInsurances().stream()
                    .anyMatch(q -> q.getInsuranceType().equalsIgnoreCase(dto.getInsuranceType()));
            if (!alreadyPresent) {
                QuoteInsurance newInsurance = new QuoteInsurance();
                newInsurance.setInsuranceType(dto.getInsuranceType());
                newInsurance.setSelected(dto.isSelected());
                newInsurance.setQuote(quote);
                quote.getInsurances().add(newInsurance);
            }
        });
    }


    private void updateClientDetails(Client client, ClientDTO updatedClientDto) {
        if (client == null) return;

        if (updatedClientDto.getClientName() != null) {
            client.setClientName(updatedClientDto.getClientName());
        }
        if (updatedClientDto.getEmail() != null) {
            client.setEmail(updatedClientDto.getEmail());
        }
        if (updatedClientDto.getContactNumber() != null) {
            client.setContactNumber(updatedClientDto.getContactNumber());
        }
        if (updatedClientDto.getAddress() != null) {
            client.setAddress(updatedClientDto.getAddress());
        }

        clientRepository.save(client); // ✅ Save updated client
    }

}
