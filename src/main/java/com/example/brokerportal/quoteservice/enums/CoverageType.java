package com.example.brokerportal.quoteservice.enums;

public enum CoverageType {
    DATA_BREACH,
    RANSOMWARE,
    SYSTEM_FAILURE,
    THIRD_PARTY_LIABILITY,
    BUSINESS_INTERRUPTION,
    NETWORK_SECURITY,
    PRIVACY_REGULATION,
    SOCIAL_ENGINEERING,
    INCIDENT_RESPONSE,
    CLOUD_OUTAGE;

    public static CoverageType fromStringSafe(String value) {
        if (value == null) return null;
        try {
            return CoverageType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // or throw custom exception if you prefer to fail fast
        }
    }

}