package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.dto.GeneralLiabilityInsuranceDTO;
import com.example.brokerportal.quoteservice.service.GeneralLiabilityInsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/general-liability-insurance")
@RequiredArgsConstructor
public class GeneralLiabilityInsuranceController {

    private final GeneralLiabilityInsuranceService generalLiabilityInsuranceService;

    @GetMapping("/hi")
    public String checkGeneralLiability() {
        return "Hi from General Liability Insurance";
    }

    @PostMapping("/{quoteId}")
    public ResponseEntity<GeneralLiabilityInsuranceDTO> createGeneralLiabilityInsurance(
            @PathVariable Long quoteId,
            @RequestBody GeneralLiabilityInsuranceDTO dto
    ) {
        log.info("Creating General Liability Insurance for quoteId: {}", quoteId);
        GeneralLiabilityInsuranceDTO created = generalLiabilityInsuranceService.createGeneralLiabilityInsurance(quoteId, dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{quoteId}")
    public ResponseEntity<GeneralLiabilityInsuranceDTO> updateGeneralLiabilityInsurance(
            @PathVariable Long quoteId,
            @RequestBody GeneralLiabilityInsuranceDTO dto
    ) {
        GeneralLiabilityInsuranceDTO updated = generalLiabilityInsuranceService.updateGeneralLiabilityInsurance(quoteId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<GeneralLiabilityInsuranceDTO> getGeneralLiabilityInsurance(@PathVariable Long quoteId) {
        GeneralLiabilityInsuranceDTO dto = generalLiabilityInsuranceService.getGeneralLiabilityInsuranceByQuoteId(quoteId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{quoteId}")
    public ResponseEntity<Void> softDeleteGeneralLiabilityInsurance(@PathVariable Long quoteId) {
        generalLiabilityInsuranceService.softDeleteGeneralLiabilityInsurance(quoteId);
        return ResponseEntity.noContent().build();
    }
}
