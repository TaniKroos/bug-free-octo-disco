package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.UserRepository;
import com.example.brokerportal.authservice.service.UserService;
import com.example.brokerportal.quoteservice.dto.*;
import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.AuditAction;
import com.example.brokerportal.quoteservice.enums.QuoteStatus;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.mapper.ClientMapper;
import com.example.brokerportal.quoteservice.mapper.QuoteMapper;
import com.example.brokerportal.quoteservice.repositories.*;
import com.example.brokerportal.quoteservice.specifications.QuoteSpecification;
import com.example.brokerportal.quoteservice.specifications.QuoteSpecificationBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
    private final PropertyInsuranceRepository propertyInsuranceRepository;
    private final GeneralLiabilityInsuranceRepository generalInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final AuditLogService auditLogService;
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
        quote.setBroker(userService.getCurrentUser()); //  correct place

        if (quoteDTO.getClient() != null) {
            Optional<Client> existingClientOpt = clientRepository
                    .findByClientNameAndEmailAndContactNumber(quoteDTO.getClient().getClientName(), quoteDTO.getClient().getEmail(), quoteDTO.getClient().getContactNumber());
            Client client = existingClientOpt.orElseGet(() -> {
                Client newClient = new Client();
                newClient.setClientName(quoteDTO.getClient().getClientName());
                newClient.setBusinessType(quoteDTO.getClient().getBusinessType());
                newClient.setIndustryType(quoteDTO.getClient().getIndustryType());
                newClient.setContactNumber(quoteDTO.getClient().getContactNumber());
                newClient.setEmail(quoteDTO.getClient().getEmail());
                newClient.setAddress(quoteDTO.getClient().getAddress());
                newClient.setBroker(userService.getCurrentUser());
                return clientRepository.save(newClient);
            });
            quote.setClient(client);
        }

        Quote saved = quoteRepository.save(quote);
        if(quoteDTO.getInsurances() != null && !quoteDTO.getInsurances().isEmpty()){
            quoteInsuranceService.mapAndAttachInsurancesToQuote(quote,quoteDTO.getInsurances());
        }
        String performedBy = userService.getCurrentUser().getEmail(); // or getUsername()
        String changedDetails = "Created Quote ID: " + saved.getId() +
                ", Status: " + saved.getStatus() ;


        auditLogService.logAction(AuditAction.QUOTE_CREATED, saved, changedDetails, performedBy);
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
        if (QuoteStatus.valueOf(quote.getStatus()).equals(QuoteStatus.BOUND)) {
            throw new IllegalStateException("Quote is already bound and cannot be modified.");
        }
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
        String performedBy = userService.getCurrentUser().getEmail(); // or getUsername()
        String changedDetails = "Update Quote ID: " + updatedQuote.getId() +
                ", Status: " + updatedQuote.getStatus() ;


        auditLogService.logAction(AuditAction.QUOTE_UPDATED, updatedQuote, changedDetails, performedBy);
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
        if (QuoteStatus.valueOf(quote.getStatus()).equals(QuoteStatus.BOUND)) {
            throw new IllegalStateException("Quote is already bound and cannot be modified.");
        }
        quote.setDeleted(true);
        quote.setUpdatedAt(LocalDateTime.now());


        if (quote.getInsurances() != null && !quote.getInsurances().isEmpty()) {
            for (QuoteInsurance quoteInsurance : quote.getInsurances()) {
                quoteInsurance.setWasSelectedBefore(quoteInsurance.isSelected());
                quoteInsurance.setSelected(false); // soft delete quote_insurance

                if ("CYBER".equalsIgnoreCase(quoteInsurance.getInsuranceType())
                        && quoteInsurance.getCyberInsurance() != null) {
                    quoteInsurance.getCyberInsurance().setDeleted(true);
                    cyberInsuranceRepository.save(quoteInsurance.getCyberInsurance());
                }
                if ("PROPERTY".equalsIgnoreCase(quoteInsurance.getInsuranceType())
                        && quoteInsurance.getPropertyInsurance() != null) {
                    quoteInsurance.getPropertyInsurance().setDeleted(true);
                    propertyInsuranceRepository.save(quoteInsurance.getPropertyInsurance());
                }
                if ("GENERAL".equalsIgnoreCase(quoteInsurance.getInsuranceType())
                        && quoteInsurance.getGeneralInsurance() != null) {
                    quoteInsurance.getGeneralInsurance().setDeleted(true);
                    generalInsuranceRepository.save(quoteInsurance.getGeneralInsurance());
                }

                if(quoteInsurance.getPremium() != null){
                    quoteInsurance.getPremium().setDeleted(true);
                }

            }
        }


        String performedBy = userService.getCurrentUser().getEmail(); // or getUsername()
        String changedDetails = "Soft Deleted Quote ID: " + quote.getId() +
                ", Status: " + quote.getStatus() ;


        auditLogService.logAction(AuditAction.QUOTE_SOFT_DELETED, quote, changedDetails, performedBy);
        quoteRepository.save(quote);

    }

    @Override
    public PagedResponseDTO<QuoteSummaryDTO> getQuotesByBrokerId(int page, int size) {
        User broker = userService.getCurrentUser();
        Long brokerId = broker.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Quote> quotePage = quoteRepository.findByBrokerIdAndDeletedFalse(brokerId, pageable);

        List<QuoteSummaryDTO> quoteSummaryDTOs = quotePage.getContent()
                .stream()
                .map(QuoteMapper::toSummaryDTO)
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                quoteSummaryDTOs,
                quotePage.getNumber(),
                quotePage.getSize(),
                quotePage.getTotalElements(),
                quotePage.getTotalPages(),
                quotePage.isLast()
        );
    }


    // To authorize the broker
    private void authorizeBrokerAccess(Quote quote) {
        User user = userService.getCurrentUser();
        if (!quote.getBroker().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to access or modify this quote");
        }
    }


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

                        qi.getCyberInsurance().setDeleted(false);
                        cyberInsuranceRepository.save(qi.getCyberInsurance());
                    } else if (!selected && Boolean.FALSE.equals(qi.getCyberInsurance().getDeleted())) {

                        qi.getCyberInsurance().setDeleted(true);
                        cyberInsuranceRepository.save(qi.getCyberInsurance());
                    }
                }
                if ("PROPERTY".equalsIgnoreCase(insuranceType) && qi.getPropertyInsurance() != null) {
                    if (selected && Boolean.TRUE.equals(qi.getPropertyInsurance().getDeleted())) {
                        qi.getPropertyInsurance().setDeleted(false);
                        propertyInsuranceRepository.save(qi.getPropertyInsurance());
                    } else if (!selected && Boolean.FALSE.equals(qi.getPropertyInsurance().getDeleted())) {
                        qi.getPropertyInsurance().setDeleted(true);
                        propertyInsuranceRepository.save(qi.getPropertyInsurance());
                    }
                }

                if ("GENERAL".equalsIgnoreCase(insuranceType) && qi.getGeneralInsurance() != null) {
                    if (selected && Boolean.TRUE.equals(qi.getGeneralInsurance().getDeleted())) {
                        qi.getGeneralInsurance().setDeleted(false);
                        generalInsuranceRepository.save(qi.getGeneralInsurance());
                    } else if (!selected && Boolean.FALSE.equals(qi.getGeneralInsurance().getDeleted())) {
                        qi.getGeneralInsurance().setDeleted(true);
                        generalInsuranceRepository.save(qi.getGeneralInsurance());
                    }
                }
        }
        }


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

        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void restoreQuote(Long id){
        log.error("Hi from restoreQuote");
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote with this id does not exist"));
        authorizeBrokerAccess(quote);

        quote.setDeleted(false);
        quote.setUpdatedAt(LocalDateTime.now());
        if(quote.getInsurances()!= null && !quote.getInsurances().isEmpty()){
            for (QuoteInsurance qi : quote.getInsurances()){
                String insuranceType = qi.getInsuranceType();
                switch (insuranceType.toUpperCase()) {
                    case "CYBER":
                        qi.getCyberInsurance().setDeleted(false);
                        cyberInsuranceRepository.save(qi.getCyberInsurance());
                        break;
                    case "PROPERTY":
                        qi.getPropertyInsurance().setDeleted(false);
                        propertyInsuranceRepository.save(qi.getPropertyInsurance());
                        break;
                    case "GENERAL":
                        qi.getGeneralInsurance().setDeleted(false);
                        generalInsuranceRepository.save(qi.getGeneralInsurance());
                        break;
                    default:
                        break;
                }
                // Restore premium
                if (qi.getPremium() != null) {
                    qi.getPremium().setDeleted(false);
                    premiumRepository.save(qi.getPremium());
                }
                qi.setSelected(Boolean.TRUE.equals(qi.getWasSelectedBefore()));
            }
        }
        String performedBy = userService.getCurrentUser().getEmail(); // or getUsername()
        String changedDetails = "Restore Quote ID: " + quote.getId() +
                ", Status: " + quote.getStatus() ;


        auditLogService.logAction(AuditAction.QUOTE_RESTORED, quote, changedDetails, performedBy);
        quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public void bindQuote(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + id));

        authorizeBrokerAccess(quote);
        if(quote.isDeleted()){
            throw new IllegalStateException("Quote is marked as deleted and cannot be bounded");
        }
        if (QuoteStatus.valueOf(quote.getStatus()).equals(QuoteStatus.BOUND)) {
            throw new IllegalStateException("Quote is already bound and cannot be modified.");
        }
        for(QuoteInsurance qi : quote.getInsurances()){
            if(qi.isSelected()){
                if ((qi.getPremium().getTotalPremium() == null || qi.getPremium().isDeleted())){
                    throw new IllegalStateException("Premium not generated for this qutoe yet");
                }
            }
        }

        quote.setStatus("BOUND");
        quote.setUpdatedAt(LocalDateTime.now());

        quoteRepository.save(quote);
        String performedBy = userService.getCurrentUser().getEmail(); // or getUsername()
        String changedDetails = "Quote Binded with Quote ID: " + quote.getId() +
                ", Status: " + quote.getStatus() ;


        auditLogService.logAction(AuditAction.QUOTE_BOUND, quote, changedDetails, performedBy);
        // Optional: trigger async confirmation email
        // asyncEventPublisher.sendBindConfirmation(quote);
    }



    // Search filter for quotes
    @Override
    public PagedResponseDTO<QuoteSummaryDTO> searchQuotesByBroker(
            QuoteSearchFilterDTO filter, int page, int size) {

        User broker = userService.getCurrentUser();
        Long brokerId = broker.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Quote> spec = QuoteSpecification.withFilters(filter, brokerId);

        Page<Quote> quotePage = quoteRepository.findAll(spec, pageable);

        List<QuoteSummaryDTO> content = quotePage.getContent().stream()
                .map(QuoteMapper::toSummaryDTO)
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                content,
                quotePage.getNumber(),
                quotePage.getSize(),
                quotePage.getTotalElements(),
                quotePage.getTotalPages(),
                quotePage.isLast()
        );
    }

}


























