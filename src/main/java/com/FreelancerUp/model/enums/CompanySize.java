package com.FreelancerUp.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }

    @JsonCreator
    public static CompanySize fromDisplayValue(String value) {
        for (CompanySize size : CompanySize.values()) {
            if (size.displayValue.equals(value)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown CompanySize: " + value);
    }
}
