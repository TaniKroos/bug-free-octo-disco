package com.example.brokerportal.config;

import com.example.brokerportal.quoteservice.entities.BusinessRiskFactor;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.repositories.BusinessRiskFactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessRiskFactorInitializer implements CommandLineRunner {


    private final BusinessRiskFactorRepository repository;

    @Override
    public void run(String... args) {
        addIfNotExists(BusinessType.HEALTHCARE, 1.3, 1.3);
        addIfNotExists(BusinessType.OUTLET, 1.1, 1.1);
        addIfNotExists(BusinessType.MANUFACTURING, 1.25, 1.25);
        addIfNotExists(BusinessType.LOGISTICS, 1.2, 1.1);
        addIfNotExists(BusinessType.BEAUTY_AND_WELLNESS, 1.15, 1.0);
    }

    private void addIfNotExists(BusinessType type, double businessRisk, double dataExposure) {
        repository.findByBusinessType(type).ifPresentOrElse(
                existing -> {}, // Already exists, do nothing
                () -> {
                    BusinessRiskFactor factor = new BusinessRiskFactor();
                    factor.setBusinessType(type);
                    factor.setBusinessRisk(businessRisk);
                    factor.setDataExposure(dataExposure);
                    repository.save(factor);
                }
        );
    }
}
