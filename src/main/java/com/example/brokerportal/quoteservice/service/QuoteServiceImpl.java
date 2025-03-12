package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.UserRepository;
import com.example.brokerportal.authservice.service.UserService;
import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Client;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.mapper.ClientMapper;
import com.example.brokerportal.quoteservice.mapper.QuoteMapper;
import com.example.brokerportal.quoteservice.repositories.ClientRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
            Client client;
            if (quoteDTO.getClient().getId() != null) {
                // Existing client case
                client = clientRepository.findById(quoteDTO.getClient().getId())
                        .orElseThrow(() -> new RuntimeException("Client not found with id: " + quoteDTO.getClient().getId()));
            } else {
                // New client — create from DTO
                client = ClientMapper.toEntity(quoteDTO.getClient());
                client = clientRepository.save(client);
            }
            quote.setClient(client);
        }

        if(quoteDTO.getInsurances() != null && !quoteDTO.getInsurances().isEmpty()){
            quoteInsuranceService.mapAndAttachInsurancesToQuote(quote,quoteDTO.getInsurances());
        }
        Quote saved = quoteRepository.save(quote);
        return QuoteMapper.toDTO(saved);
    }

    @Override
    public QuoteDTO getQuoteById(Long id) {


        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote with this id:" + id + " doesn't exist in the database"));

        authorizeBrokerAccess(quote);


        return QuoteMapper.toDTO(quote);
    }

    @Override
    @Transactional
    public QuoteDTO updateQuote(Long id, QuoteDTO updatedQuoteDto) {


        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote with this id: " + id + " doesn't exist"));



        authorizeBrokerAccess(quote);


        if (updatedQuoteDto.getStatus() != null) {
            quote.setStatus(updatedQuoteDto.getStatus());
        }

        quote.setUpdatedAt(LocalDateTime.now());

        if (updatedQuoteDto.getInsurances() != null && !updatedQuoteDto.getInsurances().isEmpty()) {
           quoteInsuranceService.deleteInsurancesByQuote(quote);
           quoteInsuranceService.mapAndAttachInsurancesToQuote(quote,updatedQuoteDto.getInsurances());
        }

        Quote updatedQuote = quoteRepository.save(quote);
        return QuoteMapper.toDTO(updatedQuote);
    }

    @Override
    @Transactional
    public void softDeleteQuote(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + id));
        authorizeBrokerAccess(quote);

        quote.setDeleted(true);
        quote.setUpdatedAt(LocalDateTime.now());
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



}
