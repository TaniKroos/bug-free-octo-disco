package com.example.brokerportal.quoteservice.repositories;


import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.EmployeeInsurance;
import com.example.brokerportal.quoteservice.entities.PropertyInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverageRepository extends JpaRepository<Coverage, Long> {
    // List<Coverage> findByQuoteInsuranceId(Long quoteInsuranceId);
    List<Coverage> findByPropertyInsurance(PropertyInsurance propertyInsurance);
    List<Coverage> findByCyberInsurance(CyberInsurance cyberInsurance);
    List<Coverage> findByEmployeeInsurance(EmployeeInsurance employeeInsurance);

}
