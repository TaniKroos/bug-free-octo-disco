package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;
import com.example.brokerportal.common.errors.ErrorResponse;
import com.example.brokerportal.quoteservice.service.PropertyInsuranceService;
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
@RequestMapping("/api/property-insurance")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PropertyInsuranceController {

    private final PropertyInsuranceService propertyInsuranceService;

    @GetMapping("/hi")
    public String checkPropertyInsurance() {
        return "Hi from Property Insurance";
    }

    // Create Property Insurance with validation
    @PostMapping("/{quoteId}")
    public ResponseEntity<?> createPropertyInsurance(
            @PathVariable Long quoteId,
            @Valid @RequestBody PropertyInsuranceDTO dto,  // Add @Valid here to trigger validation
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

        log.info("Creating Property Insurance for quoteId: {}", quoteId);
        PropertyInsuranceDTO created = propertyInsuranceService.createPropertyInsurance(quoteId, dto);
        return ResponseEntity.ok(created);
    }

    // Update Property Insurance with validation
    @PutMapping("/{quoteId}")
    public ResponseEntity<?> updatePropertyInsurance(
            @PathVariable Long quoteId,
            @Valid @RequestBody PropertyInsuranceDTO dto,  // Add @Valid here to trigger validation
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

        PropertyInsuranceDTO updated = propertyInsuranceService.updatePropertyInsurance(quoteId, dto);
        return ResponseEntity.ok(updated);
    }

    // Get Property Insurance details by quote ID
    @GetMapping("/{quoteId}")
    public ResponseEntity<PropertyInsuranceDTO> getPropertyInsurance(@PathVariable Long quoteId) {
        PropertyInsuranceDTO dto = propertyInsuranceService.getPropertyInsuranceByQuoteId(quoteId);
        return ResponseEntity.ok(dto);
    }

    // Soft Delete Property Insurance
    @DeleteMapping("/{quoteId}")
    public ResponseEntity<Void> softDeletePropertyInsurance(@PathVariable Long quoteId) {
        propertyInsuranceService.softDeletePropertyInsurance(quoteId);
        return ResponseEntity.noContent().build();
    }
}
