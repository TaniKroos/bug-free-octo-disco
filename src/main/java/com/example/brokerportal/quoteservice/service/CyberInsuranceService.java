package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;

public interface CyberInsuranceService {
    CyberInsuranceDTO createCyberInsurance(Long quoteId, CyberInsuranceDTO dto);
    CyberInsuranceDTO updateCyberInsurance(Long quoteId, CyberInsuranceDTO dto);
    CyberInsuranceDTO getCyberInsuranceByQuoteId(Long quoteId);
    void softDeleteCyberInsurance(Long quoteId);
}
