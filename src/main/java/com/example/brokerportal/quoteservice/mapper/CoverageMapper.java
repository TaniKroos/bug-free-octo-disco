package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.EmployeeInsurance;
import com.example.brokerportal.quoteservice.entities.PropertyInsurance;

public class CoverageMapper {

    public static CoverageDTO toDTO(Coverage coverage) {
        if (coverage == null) return null;

        return CoverageDTO.builder()
                .id(coverage.getId())
                .coverageType(coverage.getCoverageType())
                .coverageAmount(coverage.getCoverageAmount())
                .propertyInsuranceId(coverage.getPropertyInsurance() != null ? coverage.getPropertyInsurance().getId() : null)
                .cyberInsuranceId(coverage.getCyberInsurance() != null ? coverage.getCyberInsurance().getId() : null)
                .employeeInsuranceId(coverage.getEmployeeInsurance() != null ? coverage.getEmployeeInsurance().getId() : null)
                .build();
    }

    public static Coverage toEntity(
            CoverageDTO dto,
            PropertyInsurance propertyInsurance,
            CyberInsurance cyberInsurance,
            EmployeeInsurance employeeInsurance
    ) {
        if (dto == null) return null;

        return Coverage.builder()
                .id(dto.getId())
                .coverageType(dto.getCoverageType())
                .description(dto.getDescription())
                .coverageAmount(dto.getCoverageAmount())
                .propertyInsurance(propertyInsurance)
                .cyberInsurance(cyberInsurance)
                .employeeInsurance(employeeInsurance)
                .build();
    }
}
