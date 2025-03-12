package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.entities.PropertyInsurance;
import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.EmployeeInsurance;

public class QuoteInsuranceMapper {

    public static QuoteInsuranceDTO toDTO(QuoteInsurance entity) {
        if (entity == null) return null;

        return QuoteInsuranceDTO.builder()
                .id(entity.getId())
                .insuranceType(entity.getInsuranceType())
                .isSelected(entity.isSelected())
                .propertyInsurance(PropertyInsuranceMapper.toDTO(entity.getPropertyInsurance()))
                .cyberInsurance(CyberInsuranceMapper.toDTO(entity.getCyberInsurance()))
                .employeeInsurance(EmployeeInsuranceMapper.toDTO(entity.getEmployeeInsurance()))
                .build();
    }

    public static QuoteInsurance toEntity(QuoteInsuranceDTO dto) {
        if (dto == null) return null;

        QuoteInsurance entity = QuoteInsurance.builder()
                .id(dto.getId())
                .insuranceType(dto.getInsuranceType())
                .isSelected(dto.isSelected())
                .build();

        // Property Insurance
        if (dto.getPropertyInsurance() != null) {
            PropertyInsurance propertyInsurance = PropertyInsuranceMapper.toEntity(dto.getPropertyInsurance());
            propertyInsurance.setQuoteInsurance(entity); // 🔥 bidirectional fix
            entity.setPropertyInsurance(propertyInsurance);
        }

        // Cyber Insurance
        if (dto.getCyberInsurance() != null) {
            CyberInsurance cyberInsurance = CyberInsuranceMapper.toEntity(dto.getCyberInsurance());
            cyberInsurance.setQuoteInsurance(entity); // 🔥 bidirectional fix
            entity.setCyberInsurance(cyberInsurance);
        }

        // Employee Insurance
        if (dto.getEmployeeInsurance() != null) {
            EmployeeInsurance employeeInsurance = EmployeeInsuranceMapper.toEntity(dto.getEmployeeInsurance());
            employeeInsurance.setQuoteInsurance(entity); // 🔥 bidirectional fix
            entity.setEmployeeInsurance(employeeInsurance);
        }

        return entity;
    }
}
