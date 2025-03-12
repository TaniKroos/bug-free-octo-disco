package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Quote;

import java.util.List;

public interface QuoteInsuranceService {
    //QuoteInsuranceDTO addInsuranceToQuote(Long quoteId, QuoteInsuranceDTO dto);
    void deleteInsurancesByQuote(Quote quote);
    List<QuoteInsuranceDTO> mapAndAttachInsurancesToQuote(Quote quote,List<QuoteInsuranceDTO> insuranceDTOS);
}
