package com.example.brokerportal.quoteservice.specifications;

import com.example.brokerportal.quoteservice.dto.QuoteSearchFilterDTO;
import com.example.brokerportal.quoteservice.entities.Quote;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class QuoteSpecification {
    public static Specification<Quote> withFilters(QuoteSearchFilterDTO filter, Long brokerId) {
        return (Root<Quote> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Match broker
            if (brokerId != null) {
                predicates.add(cb.equal(root.get("broker").get("id"), brokerId));
            }

            // Only non-deleted


            // Client name
            if (filter.getClientName() != null && !filter.getClientName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("client").get("clientName")),
                        "%" + filter.getClientName().toLowerCase() + "%"));
            }

            // Quote status
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Insurance type
            if (filter.getInsuranceType() != null && !filter.getInsuranceType().isEmpty()) {
                Join<Object, Object> insurances = root.join("insurances", JoinType.LEFT);
                predicates.add(cb.equal(insurances.get("insuranceType"), filter.getInsuranceType()));
            }

//            // Date range
//            if (filter.getCreatedFrom() != null) {
//                predicates.add(cb.greaterThanOrEqualTo(
//                        root.get("createdAt"), filter.getCreatedFrom().atStartOfDay()));
//            }
//
//            if (filter.getCreatedTo() != null) {
//                predicates.add(cb.lessThanOrEqualTo(
//                        root.get("createdAt"), filter.getCreatedTo().atTime(LocalTime.MAX)));
//            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
