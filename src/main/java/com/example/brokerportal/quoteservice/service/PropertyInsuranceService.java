package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;

public interface PropertyInsuranceService {
    PropertyInsuranceDTO createOrUpdate(Long quoteInsuranceId, PropertyInsuranceDTO dto);

}
