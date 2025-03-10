package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;

public interface QuoteInsuranceService {
    QuoteInsuranceDTO addInsuranceToQuote(Long quoteId, QuoteInsuranceDTO dto);
}
