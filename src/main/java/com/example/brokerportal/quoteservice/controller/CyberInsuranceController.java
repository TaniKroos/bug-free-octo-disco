package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;
import com.example.brokerportal.common.errors.ErrorResponse;
import com.example.brokerportal.quoteservice.service.CyberInsuranceService;
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
@RequestMapping("/api/cyber-insurance")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CyberInsuranceController {

    private final CyberInsuranceService cyberInsuranceService;

    @GetMapping("/hi")
    public String checkCyberInsurance() {
        return "Hi from Cyber Insurance";
    }

    // Create Cyber Insurance with validation
    @PostMapping("/{quoteId}")
    public ResponseEntity<?> createCyberInsurance(
            @PathVariable Long quoteId,
            @Valid @RequestBody CyberInsuranceDTO dto,  // Add @Valid here to trigger validation
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

        log.info("Creating Cyber Insurance for quoteId: {}", quoteId);
        CyberInsuranceDTO created = cyberInsuranceService.createCyberInsurance(quoteId, dto);
        return ResponseEntity.ok(created);
    }

    // Update Cyber Insurance with validation
    @PutMapping("/{quoteId}")
    public ResponseEntity<?> updateCyberInsurance(
            @PathVariable Long quoteId,
            @Valid @RequestBody CyberInsuranceDTO dto,  // Add @Valid here to trigger validation
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

        CyberInsuranceDTO updated = cyberInsuranceService.updateCyberInsurance(quoteId, dto);
        return ResponseEntity.ok(updated);
    }

    // Get Cyber Insurance details by quote ID
    @GetMapping("/{quoteId}")
    public ResponseEntity<CyberInsuranceDTO> getCyberInsurance(@PathVariable Long quoteId) {
        CyberInsuranceDTO dto = cyberInsuranceService.getCyberInsuranceByQuoteId(quoteId);
        return ResponseEntity.ok(dto);
    }

    // Soft Delete Cyber Insurance
    @DeleteMapping("/{quoteId}")
    public ResponseEntity<Void> softDeleteCyberInsurance(@PathVariable Long quoteId) {
        cyberInsuranceService.softDeleteCyberInsurance(quoteId);
        return ResponseEntity.noContent().build();
    }
}
