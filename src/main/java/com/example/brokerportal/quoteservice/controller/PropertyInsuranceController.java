package com.example.brokerportal.quoteservice.controller;



import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;
import com.example.brokerportal.quoteservice.service.PropertyInsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/property-insurance")
@RequiredArgsConstructor
public class PropertyInsuranceController {

    private final PropertyInsuranceService propertyInsuranceService;


    @GetMapping("/hi")
    public String checkprop(){
        return " hi from prop";
    }
    @PostMapping("/{quoteId}")
    public ResponseEntity<PropertyInsuranceDTO> createPropertyInsurance(
            @PathVariable Long quoteId,
            @RequestBody PropertyInsuranceDTO dto
    ) {

        log.error("Hi from create Property");
        System.out.println("Hi from create Property");
        PropertyInsuranceDTO created = propertyInsuranceService.createPropertyInsurance(quoteId, dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{quoteId}")
    public ResponseEntity<PropertyInsuranceDTO> updatePropertyInsurance(
            @PathVariable Long quoteId,
            @RequestBody PropertyInsuranceDTO dto
    ) {
        PropertyInsuranceDTO updated = propertyInsuranceService.updatePropertyInsurance(quoteId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<PropertyInsuranceDTO> getPropertyInsurance(@PathVariable Long quoteId) {
        PropertyInsuranceDTO dto = propertyInsuranceService.getPropertyInsuranceByQuoteId(quoteId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{quoteId}")
    public ResponseEntity<Void> softDeletePropertyInsurance(@PathVariable Long quoteId) {
        propertyInsuranceService.softDeletePropertyInsurance(quoteId);
        return ResponseEntity.noContent().build();
    }
}

