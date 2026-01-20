package com.FreelancerUp.model.enums;

public enum CompanySize {
    SIZE_1_10("1-10"),
    SIZE_11_50("11-50"),
    SIZE_51_200("51-200"),
    SIZE_201_500("201-500"),
    SIZE_500_PLUS("500+");

    private final String displayValue;

    CompanySize(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
