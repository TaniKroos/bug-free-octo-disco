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
    @Override
    @Transactional
    public QuoteDTO createQuote(QuoteDTO quoteDTO){
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

        List<QuoteInsurance> quoteInsurances = new ArrayList<>();
        if(quoteDTO.getInsurances() != null){
            for(QuoteInsuranceDTO insuranceDTO: quoteDTO.getInsurances()){
                QuoteInsurance quoteInsurance = new QuoteInsurance();
                quoteInsurance.setInsuranceType(insuranceDTO.getInsuranceType());
                quoteInsurance.setSelected(insuranceDTO.isSelected());
                quoteInsurance.setQuote(quote);
                quoteInsurances.add(quoteInsurance);
            }
        }
        quote.setInsurances(quoteInsurances);
        quoteInsuranceRepository.saveAll(quoteInsurances);
        Quote saved = quoteRepository.save(quote);
        return QuoteMapper.toDTO(saved);
    }

    @Override
    public QuoteDTO getQuoteById(Long id){
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote with this id:" + id + "doesn't exist in the database"));
        return QuoteMapper.toDTO(quote);
    }

    @Override
    @Transactional
    public QuoteDTO updateQuote(Long id,QuoteDTO updadtedQuoteDto){
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotee with this id: " + id + "Doesn't exist"));
        if (updadtedQuoteDto.getStatus() != null) {
            quote.setStatus(updadtedQuoteDto.getStatus());
        }

        quote.setUpdatedAt(LocalDateTime.now());
        if (updadtedQuoteDto.getInsurances() != null && !updadtedQuoteDto.getInsurances().isEmpty()) {

            quoteInsuranceRepository.deleteAllByQuote(quote);


            List<QuoteInsurance> updatedInsurances = new ArrayList<>();
            for (QuoteInsuranceDTO insuranceDTO : updadtedQuoteDto.getInsurances()) {
                QuoteInsurance quoteInsurance = new QuoteInsurance();
                quoteInsurance.setInsuranceType(insuranceDTO.getInsuranceType());
                quoteInsurance.setSelected(insuranceDTO.isSelected());
                quoteInsurance.setQuote(quote);
                updatedInsurances.add(quoteInsurance);
            }


            quote.setInsurances(updatedInsurances);
            quoteInsuranceRepository.saveAll(updatedInsurances);
        }
        Quote updatedQuote = quoteRepository.save(quote);
        return QuoteMapper.toDTO(updatedQuote);
    }
    @Override
    @Transactional
    public void softDeleteQuote(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + id));
        quote.setDeleted(true);
        quote.setUpdatedAt(LocalDateTime.now());
        quoteRepository.save(quote);
    }

    @Override
    public List<QuoteDTO> getQuotesByBrokerId(Long brokerId) {
        List<Quote> quotes = quoteRepository.findByBrokerIdAndDeletedFalse(brokerId);
        return quotes.stream().map(QuoteMapper::toDTO).collect(Collectors.toList());
    }



}
