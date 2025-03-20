package com.example.brokerportal.quoteservice.specifications;

import com.example.brokerportal.quoteservice.dto.QuoteSearchFilterDTO;
import com.example.brokerportal.quoteservice.entities.Quote;
import org.springframework.data.jpa.domain.Specification;

public class QuoteSpecificationBuilder {

    public static Specification<Quote> build(QuoteSearchFilterDTO filter) {
        return Specification.where(QuoteSpecification.hasClientName(filter.getClientName()))
                .and(QuoteSpecification.hasStatus(filter.getStatus()))
                .and(QuoteSpecification.hasBrokerId(filter.getBrokerId()))
                .and(QuoteSpecification.hasInsuranceType(filter.getInsuranceType()));

    }
}
