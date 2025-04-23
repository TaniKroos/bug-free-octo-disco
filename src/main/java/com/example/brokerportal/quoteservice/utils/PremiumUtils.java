package com.example.brokerportal.quoteservice.utils;

import com.example.brokerportal.quoteservice.enums.BusinessType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PremiumUtils {
    private static final Map<String, BigDecimal> STATE_TAX_RATES = new HashMap<>();
    static {
        STATE_TAX_RATES.put("CALIFORNIA", BigDecimal.valueOf(0.075));
        STATE_TAX_RATES.put("TEXAS", BigDecimal.valueOf(0.0625));
        STATE_TAX_RATES.put("NEW_YORK", BigDecimal.valueOf(0.088));
        STATE_TAX_RATES.put("FLORIDA", BigDecimal.valueOf(0.06));
        STATE_TAX_RATES.put("ILLINOIS", BigDecimal.valueOf(0.062));
        STATE_TAX_RATES.put("OHIO", BigDecimal.valueOf(0.05));
        STATE_TAX_RATES.put("MASSACHUSETTS", BigDecimal.valueOf(0.0625));
        STATE_TAX_RATES.put("PENNSYLVANIA", BigDecimal.valueOf(0.06));
    }

    public static BigDecimal getTaxRate(String address) {
        String state = extractState(address);
        return STATE_TAX_RATES.getOrDefault(state, BigDecimal.valueOf(0.18)); // default 18%
    }

    public static String extractState(String address) {
        if (address == null || address.isBlank()) return null;
        String[] parts = address.split(",");
        if (parts.length < 3) return null;
        return parts[2].trim().toUpperCase();
    }
    public static BusinessType parse(String businessTypeStr) {
        try {
            return BusinessType.valueOf(businessTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid business type: " + businessTypeStr);
        }
    }
}
