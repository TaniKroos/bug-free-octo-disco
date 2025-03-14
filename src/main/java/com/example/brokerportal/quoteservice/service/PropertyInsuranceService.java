package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;

import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;

public interface PropertyInsuranceService {

    PropertyInsuranceDTO createPropertyInsurance(Long quoteId, PropertyInsuranceDTO dto);

    PropertyInsuranceDTO updatePropertyInsurance(Long quoteId, PropertyInsuranceDTO dto);

    PropertyInsuranceDTO getPropertyInsuranceByQuoteId(Long quoteId);

    void softDeletePropertyInsurance(Long quoteId);
}
