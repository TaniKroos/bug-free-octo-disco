package com.example.brokerportal.quoteservice.specifications;

import com.example.brokerportal.quoteservice.entities.Quote;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class QuoteSpecification {
    public static Specification<Quote> hasClientName(String clientName) {
        return (root, query, criteriaBuilder) ->
                clientName == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("client").get("clientName")), "%" + clientName.toLowerCase() + "%");
    }

    public static Specification<Quote> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }
    public static Specification<Quote> hasBrokerId(Long brokerId) {
        return (root, query, criteriaBuilder) ->
                brokerId == null ? null : criteriaBuilder.equal(root.get("broker").get("id"), brokerId);
    }
    public static Specification<Quote> hasInsuranceType(String insuranceType) {
        return (root, query, criteriaBuilder) -> {
            if (insuranceType == null) return null;
            Join<?, ?> insurances = root.join("insurances", JoinType.LEFT);
            return criteriaBuilder.equal(insurances.get("insuranceType"), insuranceType);
        };
    }


}
