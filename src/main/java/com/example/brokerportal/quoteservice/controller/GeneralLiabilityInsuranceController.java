package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.dto.GeneralLiabilityInsuranceDTO;
import com.example.brokerportal.common.errors.ErrorResponse;
import com.example.brokerportal.quoteservice.service.GeneralLiabilityInsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/general-liability-insurance")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GeneralLiabilityInsuranceController {

    private final GeneralLiabilityInsuranceService generalLiabilityInsuranceService;

    @GetMapping("/hi")
    public String checkGeneralLiability() {
        return "Hi from General Liability Insurance";
    }

    // Create General Liability Insurance with validation
    @PostMapping("/{quoteId}")
    public ResponseEntity<?> createGeneralLiabilityInsurance(
            @PathVariable Long quoteId,
            @Valid @RequestBody GeneralLiabilityInsuranceDTO dto,  // Add @Valid here to trigger validation
            BindingResult bindingResult
    ) {
        // If there are validation errors, return them as a JSON error response
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());

            ErrorResponse errorResponse = new ErrorResponse("Validation failed", errorMessages);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        log.info("Creating General Liability Insurance for quoteId: {}", quoteId);
        GeneralLiabilityInsuranceDTO created = generalLiabilityInsuranceService.createGeneralLiabilityInsurance(quoteId, dto);
        return ResponseEntity.ok(created);
    }

    // Update General Liability Insurance with validation
    @PutMapping("/{quoteId}")
    public ResponseEntity<?> updateGeneralLiabilityInsurance(
            @PathVariable Long quoteId,
            @Valid @RequestBody GeneralLiabilityInsuranceDTO dto,  // Add @Valid here to trigger validation
            BindingResult bindingResult
    ) {
        // If there are validation errors, return them as a JSON error response
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());

            ErrorResponse errorResponse = new ErrorResponse("Validation failed", errorMessages);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        GeneralLiabilityInsuranceDTO updated = generalLiabilityInsuranceService.updateGeneralLiabilityInsurance(quoteId, dto);
        return ResponseEntity.ok(updated);
    }

    // Get General Liability Insurance details by quote ID
    @GetMapping("/{quoteId}")
    public ResponseEntity<GeneralLiabilityInsuranceDTO> getGeneralLiabilityInsurance(@PathVariable Long quoteId) {
        GeneralLiabilityInsuranceDTO dto = generalLiabilityInsuranceService.getGeneralLiabilityInsuranceByQuoteId(quoteId);
        return ResponseEntity.ok(dto);
    }

    // Soft Delete General Liability Insurance
    @DeleteMapping("/{quoteId}")
    public ResponseEntity<Void> softDeleteGeneralLiabilityInsurance(@PathVariable Long quoteId) {
        generalLiabilityInsuranceService.softDeleteGeneralLiabilityInsurance(quoteId);
        return ResponseEntity.noContent().build();
    }
}
