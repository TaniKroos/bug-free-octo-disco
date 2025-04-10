package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.PagedResponseDTO;
import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.dto.QuoteSearchFilterDTO;
import com.example.brokerportal.quoteservice.dto.QuoteSummaryDTO;

import java.util.List;

public interface QuoteService {
    QuoteDTO createQuote(QuoteDTO quoteDTO);
    QuoteDTO updateQuote(Long id, QuoteDTO quoteDTO);
    PagedResponseDTO<QuoteSummaryDTO> getQuotesByBrokerId(  int page, int size);
    QuoteDTO getQuoteById(Long id);
    void softDeleteQuote(Long id);
    List<QuoteDTO> findByBrokerIdAndDeletedTrue();
    void restoreQuote(Long id);
    void bindQuote(Long id);
    PagedResponseDTO<QuoteSummaryDTO> searchQuotesByBroker(QuoteSearchFilterDTO filter, int page, int size);


}
