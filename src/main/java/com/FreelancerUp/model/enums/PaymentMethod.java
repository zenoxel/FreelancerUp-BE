package com.FreelancerUp.model.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod {
    private String type; // CREDIT_CARD, PAYPAL, BANK_TRANSFER
    private String provider; // Visa, Mastercard, etc.
    private String lastFourDigits; // For cards
    private Boolean isDefault = false;
    private Boolean isActive = true;
}
