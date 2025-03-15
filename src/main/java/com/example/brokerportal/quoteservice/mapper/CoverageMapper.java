package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.entities.*;

import java.util.*;
import java.util.stream.Collectors;


public class CoverageMapper {
     public static CoverageDTO toDTO(Coverage coverage) {
        if (coverage == null) return null;

        return CoverageDTO.builder()
                .id(coverage.getId())
                .coverageType(coverage.getCoverageType())
                .coverageAmount(coverage.getCoverageAmount())
                .quoteInsuranceId(coverage.getQuoteInsurance() != null ? coverage.getQuoteInsurance().getId() : null)
                .build();
    }

    public static Coverage toEntity(CoverageDTO dto, QuoteInsurance quoteInsurance) {
        if (dto == null) return null;

        Coverage coverage = new Coverage();
        coverage.setId(dto.getId());
        coverage.setCoverageType(dto.getCoverageType());
        coverage.setCoverageAmount(dto.getCoverageAmount());
        coverage.setDescription(dto.getDescription());
        coverage.setQuoteInsurance(quoteInsurance); // ✅ Only this link now
        return coverage;
    }
//    public static List<Coverage> updateCoverageList(List<Coverage> existingCoverages, List<CoverageDTO> incomingDtos, QuoteInsurance parentQuoteInsurance) {
//        Map<Long, Coverage> existingMap = existingCoverages.stream()
//                .filter(c -> c.getId() != null)
//                .collect(Collectors.toMap(Coverage::getId, c -> c));
//
//        Set<Long> incomingIds = new HashSet<>();
//        List<Coverage> finalList = new ArrayList<>();
//
//        for (CoverageDTO dto : incomingDtos) {
//            if (dto.getId() != null && existingMap.containsKey(dto.getId())) {
//                Coverage existing = existingMap.get(dto.getId());
//                existing.setCoverageType(dto.getCoverageType());
//                existing.setCoverageAmount(dto.getCoverageAmount());
//                existing.setDescription(dto.getDescription());
//                finalList.add(existing);
//                incomingIds.add(dto.getId());
//            } else {
//                Coverage newCov = CoverageMapper.toEntity(dto, parentQuoteInsurance);                finalList.add(newCov);
//            }
//        }
//
//        // Remove orphaned ones (not in incoming)
//        existingCoverages.removeIf(cov -> cov.getId() != null && !incomingIds.contains(cov.getId()));
//
//        return finalList;
//    }
    public static void updateCoveragesInQuoteInsurance(List<CoverageDTO> coverageDTOs, QuoteInsurance quoteInsurance) {
        if (coverageDTOs == null) return;

        Map<Long, Coverage> existingMap = quoteInsurance.getCoverages().stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Coverage::getId, c -> c));

        Set<Long> incomingIds = new HashSet<>();

        for (CoverageDTO dto : coverageDTOs) {
            if (dto.getId() != null && existingMap.containsKey(dto.getId())) {
                Coverage existing = existingMap.get(dto.getId());
                existing.setCoverageType(dto.getCoverageType());
                existing.setCoverageAmount(dto.getCoverageAmount());
                existing.setDescription(dto.getDescription());
                incomingIds.add(dto.getId());
            } else {
                Coverage newCov = toEntity(dto, quoteInsurance);
                quoteInsurance.getCoverages().add(newCov);
            }
        }

        // Remove stale coverages
        Iterator<Coverage> iterator = quoteInsurance.getCoverages().iterator();
        while (iterator.hasNext()) {
            Coverage coverage = iterator.next();
            if (coverage.getId() != null && !incomingIds.contains(coverage.getId())) {
                iterator.remove();
            }
        }
    }

}
