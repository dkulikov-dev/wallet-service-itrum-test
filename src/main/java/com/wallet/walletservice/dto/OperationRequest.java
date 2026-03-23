package com.wallet.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OperationRequest {
    @NotNull
    private UUID walletId;

    @NotNull
    private String operationType; // "DEPOSIT" или "WITHDRAW"

    @NotNull
    @Positive
    private BigDecimal amount;
}