package com.wallet.walletservice.service;

import com.wallet.walletservice.entity.Wallet;
import com.wallet.walletservice.exception.InsufficientFundsException;
import com.wallet.walletservice.exception.WalletNotFoundException;
import com.wallet.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final WalletRepository repository;

    public void performOperation(UUID walletId, String operationType, BigDecimal amount) {
        if ("DEPOSIT".equalsIgnoreCase(operationType)) {
            int updated = repository.deposit(walletId, amount);
            if (updated == 0) {
                throw new WalletNotFoundException("Кошелёк не найден: " + walletId);
            }
        } else if ("WITHDRAW".equalsIgnoreCase(operationType)) {
            int updated = repository.withdraw(walletId, amount);
            if (updated == 0) {
                // Проверяем, существует ли кошелёк
                repository.findById(walletId)
                        .orElseThrow(() -> new WalletNotFoundException("Кошелёк не найден: " + walletId));
                throw new InsufficientFundsException(BigDecimal.ZERO, amount); // баланс не нужен, т.к. мы уже проверили
            }
        } else {
            throw new IllegalArgumentException("Неизвестный тип операции: " + operationType);
        }
    }

    public BigDecimal getBalance(UUID walletId) {
        return repository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new WalletNotFoundException("Кошелёк не найден: " + walletId));
    }
}