package com.example.brokerportal.quoteservice.utils;

import com.example.brokerportal.quoteservice.entities.Premium;

public interface InsurancePremiumHolder {
    void setPremium(Premium premium);
    Premium getPremium();
}
