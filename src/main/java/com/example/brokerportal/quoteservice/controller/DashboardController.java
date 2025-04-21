package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.dto.DashboardDataDTO;
import com.example.brokerportal.quoteservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {


    private final DashboardService dashboardService;

    /**
     * Endpoint to get the dashboard data for a broker.
     * @param brokerId - ID of the broker
     * @return DashboardDataDTO containing broker and quote data
     */
    @GetMapping("/data")
    public DashboardDataDTO getDashboardData(@RequestParam Long brokerId) {
        System.out.println(brokerId);
        return dashboardService.getDashboardDataForBroker(brokerId);
    }
}
