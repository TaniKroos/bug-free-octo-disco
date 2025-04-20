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
        addIfNotExists(BusinessType.HEALTHCARE, 1.3, 1.3, 0.20, 1.2, 1.0, 1.1, 1.1, 1.0, 1.3, 1.2, 1.3, 1.3);
        addIfNotExists(BusinessType.OUTLET, 1.1, 1.1, 0.18, 1.0, 1.2, 1.0, 1.0, 1.1, 1.0, 1.0, 1.2, 1.1);
        addIfNotExists(BusinessType.MANUFACTURING, 1.25, 1.25, 0.22, 1.3, 1.3, 1.2, 1.2, 1.4, 1.5, 1.2, 1.3, 1.0);
        addIfNotExists(BusinessType.LOGISTICS, 1.2, 1.1, 0.19, 1.1, 1.1, 1.1, 1.1, 1.2, 1.3, 1.1, 1.2, 1.0);
        addIfNotExists(BusinessType.BEAUTY_AND_WELLNESS, 1.15, 1.0, 0.17, 1.0, 1.0, 1.0, 1.0, 1.0, 1.1, 1.0, 1.2, 1.2);
    }

    private void addIfNotExists(
            BusinessType type,
            double businessRisk,
            double dataExposure,
            double baseTaxRate,
            double locationRiskFactor,
            double floorRiskFactor,
            double cyberComplianceFactor,
            double constructionTypeFactor,
            double theftProtectionFactor,
            double occupancyRiskFactor,
            double liabilityExposureFactor,
            double employeeRiskFactor,
            double clientInteractionFactor
    ) {
        repository.findByBusinessType(type).ifPresentOrElse(
                existing -> {},
                () -> {
                    BusinessRiskFactor factor = BusinessRiskFactor.builder()
                            .businessType(type)
                            .businessRisk(businessRisk)
                            .dataExposure(dataExposure)

                            .baseTaxRate(baseTaxRate)
                            .locationRiskFactor(locationRiskFactor)
                            .floorRiskFactor(floorRiskFactor)

                            .cyberComplianceFactor(cyberComplianceFactor)

                            .constructionTypeFactor(constructionTypeFactor)
                            .theftProtectionFactor(theftProtectionFactor)
                            .occupancyRiskFactor(occupancyRiskFactor)

                            .liabilityExposureFactor(liabilityExposureFactor)
                            .employeeRiskFactor(employeeRiskFactor)
                            .clientInteractionFactor(clientInteractionFactor)
                            .build();

                    repository.save(factor);
                }
        );
    }
}
