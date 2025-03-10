package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.EmployeeInsuranceDTO;

public interface EmployeeInsuranceService {
    EmployeeInsuranceDTO createOrUpdate(Long quoteInsuranceId, EmployeeInsuranceDTO dto);

}
