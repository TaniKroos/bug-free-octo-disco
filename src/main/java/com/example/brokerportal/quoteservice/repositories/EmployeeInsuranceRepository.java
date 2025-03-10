package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.EmployeeInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeInsuranceRepository extends JpaRepository<EmployeeInsurance, Long> {
}
