package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;

public interface CyberInsuranceService {
    CyberInsuranceDTO createOrUpdate(Long quoteInsuranceId, CyberInsuranceDTO dto);

}
