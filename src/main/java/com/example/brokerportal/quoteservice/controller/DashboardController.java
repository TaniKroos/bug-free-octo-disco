package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.service.UserService;
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
    private final UserService userService;


    @GetMapping("/data")
    public DashboardDataDTO getDashboardData() {
        User user = userService.getCurrentUser();

        return dashboardService.getDashboardDataForBroker(user.getId());
    }

}
