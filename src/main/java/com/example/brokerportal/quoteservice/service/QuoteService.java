package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.QuoteDTO;

import java.util.List;

public interface QuoteService {
    QuoteDTO createQuote(QuoteDTO quoteDTO);
    QuoteDTO updateQuote(Long id, QuoteDTO quoteDTO);
    List<QuoteDTO> getQuotesByBrokerId();
    QuoteDTO getQuoteById(Long id);
    void softDeleteQuote(Long id);


}
