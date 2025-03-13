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



        return entity;
    }
}
