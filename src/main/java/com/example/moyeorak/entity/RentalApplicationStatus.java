package com.example.moyeorak.entity;

public enum RentalApplicationStatus {
    PENDING("대기"),
    APPROVED("승인"),
    CANCELLED("취소");

    private final String displayName;

    RentalApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
