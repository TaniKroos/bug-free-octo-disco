package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.authservice.dto.UserDTO;
import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.UserRepository;
import com.example.brokerportal.quoteservice.dto.DashboardDataDTO;
import com.example.brokerportal.quoteservice.dto.QuoteSummaryDTO;
import com.example.brokerportal.quoteservice.entities.Client;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository; // To fetch broker (user) data

    @Autowired
    private QuoteRepository quoteRepository; // To fetch quotes

    /**
     * Get all the data needed for the dashboard
     * @param brokerId - ID of the broker for which to fetch the dashboard data
     * @return DashboardDataDTO containing broker and quotes
     */
    public DashboardDataDTO getDashboardDataForBroker(Long brokerId) {
        // Fetch broker (user) data from the UserRepository
        User broker = userRepository.findById(brokerId).orElseThrow(() -> new RuntimeException("Broker not found"));
        System.out.println(broker.getEmail());
        UserDTO brokerDto = new UserDTO(broker);

        // Fetch quotes for the broker from the QuoteRepository
        //List<Quote> quotes = quoteRepository.findByBrokerIdAndDeletedTrue(brokerId);  // Assuming there's a method to fetch quotes by brokerId

        // Convert Quote entities to QuoteSummaryDTO
        List<QuoteSummaryDTO> quoteSummaries =  getDashboardQuoteSummaries(brokerId);
        List<Quote> allQuotes = quoteRepository.findByBrokerIdAndDeletedFalse(brokerId);
        Set<Long> uniqueActiveClientIds = allQuotes.stream()
                .map(Quote::getClient)
                .filter(Objects::nonNull)
                .map(Client::getId)
                .collect(Collectors.toSet());
        int totalQuotes = allQuotes.size();
        int totalActiveClients = uniqueActiveClientIds.size();
        // Return a DashboardDataDTO (you can add broker info or any other necessary data)
        return new DashboardDataDTO(brokerDto, quoteSummaries, totalQuotes, totalActiveClients);


        // Prepare DashboardDataDTO


    }

    public List<QuoteSummaryDTO> getDashboardQuoteSummaries(Long brokerId) {
        try {
            // Fetch quotes for the broker, ordered by creation date descending (latest first)
            List<Quote> quotes = quoteRepository.findByBrokerIdAndDeletedFalse(brokerId);

            // Sort the quotes by creation date, latest first
            quotes.sort(Comparator.comparing(Quote::getCreatedAt).reversed());

            // Limit to the first 4 quotes
            List<Quote> latestQuotes = quotes.stream().limit(4).collect(Collectors.toList());

            // Map the latest 4 quotes to QuoteSummaryDTOs
            return latestQuotes.stream()
                    .filter(Objects::nonNull)
                    .map(quote -> {
                        // Safely get client name
                        String clientName = Optional.ofNullable(quote.getClient())
                                .map(Client::getClientName)
                                .orElse("Unknown Client");

                        // Safely get insurance types
                        List<String> selectedInsuranceTypes = Optional.ofNullable(quote.getInsurances())
                                .orElse(Collections.emptyList())
                                .stream()
                                .filter(Objects::nonNull)
                                .filter(QuoteInsurance::isSelected)
                                .limit(4)
                                .map(QuoteInsurance::getInsuranceType)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        // Safely calculate premium
                        Double totalPremium = Optional.ofNullable(quote.getInsurances())
                                .orElse(Collections.emptyList())
                                .stream()
                                .filter(Objects::nonNull)
                                .mapToDouble(qi -> Optional.ofNullable(qi.getPremium())
                                        .map(Premium::getTotalPremium)
                                        .orElse(0.0))
                                .sum();

                        return new QuoteSummaryDTO(
                                quote.getId(),
                                clientName,
                                quote.getStatus(),
                                quote.getCreatedAt(),
                                selectedInsuranceTypes,
                                totalPremium
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching dashboard quotes for broker {}", brokerId, e);
            throw new RuntimeException("Failed to fetch dashboard data", e);
        }
    }

}
