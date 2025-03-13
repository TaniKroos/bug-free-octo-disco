package com.example.brokerportal.quoteservice.controller;


import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;
import com.example.brokerportal.quoteservice.service.CyberInsuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cyber-insurance")
@RequiredArgsConstructor
public class CyberInsuranceController {
    private final CyberInsuranceService cyberInsuranceService;

    @PostMapping("/{quoteId}")
    public ResponseEntity<CyberInsuranceDTO> createCyberInsurance(
            @PathVariable Long quoteId,
            @RequestBody CyberInsuranceDTO dto
    ) {
        CyberInsuranceDTO created = cyberInsuranceService.createCyberInsurance(quoteId, dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{quoteId}")
    public ResponseEntity<CyberInsuranceDTO> updateCyberInsurance(
            @PathVariable Long quoteId,
            @RequestBody CyberInsuranceDTO dto
    ) {
        CyberInsuranceDTO updated = cyberInsuranceService.updateCyberInsurance(quoteId, dto);
        return ResponseEntity.ok(updated);
    }
    @GetMapping("/{quoteId}")
    public ResponseEntity<CyberInsuranceDTO> getCyberInsurance(@PathVariable Long quoteId) {
        CyberInsuranceDTO dto = cyberInsuranceService.getCyberInsuranceByQuoteId(quoteId);
        return ResponseEntity.ok(dto);
    }

    // 🔸 Soft Delete Cyber Insurance
    @DeleteMapping("/{quoteId}")
    public ResponseEntity<Void> softDeleteCyberInsurance(@PathVariable Long quoteId) {
        cyberInsuranceService.softDeleteCyberInsurance(quoteId);
        return ResponseEntity.noContent().build();
    }
}
