package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.dto.ClientDTO;
import com.example.brokerportal.quoteservice.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getClientByEmail(@RequestParam(required = false) String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<ClientDTO> clients = clientService.findByEmail(email);
        return ResponseEntity.ok(clients);
    }

//    @PostMapping
//    public ResponseEntity<ClientDTO> createClient(@RequestBody  ClientDTO clientDTO) {
//        ClientDTO created = clientService.createClient(clientDTO);
//        return new ResponseEntity<>(created, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ClientDTO> updateClient(
//            @PathVariable Long id,
//            @RequestBody ClientDTO updatedData
//    ) {
//        ClientDTO updated = clientService.updateClient(id, updatedData);
//        return ResponseEntity.ok(updated);
//    }
}
