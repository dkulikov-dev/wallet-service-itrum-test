package com.wallet.walletservice.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Недостаточно средств. Баланс: %s, запрошено: %s",
                currentBalance, requestedAmount));
    }
}
