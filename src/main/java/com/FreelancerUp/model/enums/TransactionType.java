package com.FreelancerUp.model.enums;

public enum TransactionType {
    CREDIT,          // Money added to wallet
    DEBIT,           // Money deducted from wallet
    ESCROW_HOLD,     // Money moved to escrow
    ESCROW_RELEASE,  // Money released from escrow
    REFUND           // Money refunded
}
