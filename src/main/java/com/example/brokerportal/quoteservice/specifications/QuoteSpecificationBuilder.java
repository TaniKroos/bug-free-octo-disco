package com.example.brokerportal.quoteservice.specifications;

import com.example.brokerportal.quoteservice.dto.QuoteSearchFilterDTO;
import com.example.brokerportal.quoteservice.entities.Quote;
import org.springframework.data.jpa.domain.Specification;

public class QuoteSpecificationBuilder {

    public static Specification<Quote> build(QuoteSearchFilterDTO filter, Long brokerId) {
        return QuoteSpecification.withFilters(filter, brokerId);
    }
}
