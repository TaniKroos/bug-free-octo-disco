package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.GeneralLiabilityInsuranceDTO;

public interface GeneralLiabilityInsuranceService {
    GeneralLiabilityInsuranceDTO createGeneralLiabilityInsurance(Long quoteId, GeneralLiabilityInsuranceDTO dto);
    GeneralLiabilityInsuranceDTO updateGeneralLiabilityInsurance(Long quoteId, GeneralLiabilityInsuranceDTO dto);
    GeneralLiabilityInsuranceDTO getGeneralLiabilityInsuranceByQuoteId(Long quoteId);
    void softDeleteGeneralLiabilityInsurance(Long quoteId);
}
