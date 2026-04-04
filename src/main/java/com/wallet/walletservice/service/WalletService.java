package com.wallet.walletservice.service;

import com.wallet.walletservice.entity.Wallet;
import com.wallet.walletservice.exception.InsufficientFundsException;
import com.wallet.walletservice.exception.WalletNotFoundException;
import com.wallet.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {
    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private final WalletRepository repository;

    public void performOperation(UUID walletId, String operationType, BigDecimal amount) {

        // Добавляем контекст в MDC для всех логов в этом запросе
        MDC.put("walletId", walletId.toString());
        MDC.put("operationType", operationType);

        try {
            if ("DEPOSIT".equalsIgnoreCase(operationType)) {
                int updated = repository.deposit(walletId, amount);
                if (updated == 0) {
                    throw new WalletNotFoundException("Кошелёк не найден: " + walletId);
                }
                log.info("Выполнено пополнение на сумму {}", amount);

            } else if ("WITHDRAW".equalsIgnoreCase(operationType)) {
                int updated = repository.withdraw(walletId, amount);
                if (updated == 0) {
                    repository.findById(walletId)
                            .orElseThrow(() -> new WalletNotFoundException("Кошелёк не найден: " + walletId));
                    throw new InsufficientFundsException(BigDecimal.ZERO, amount);
                }
                log.info("Выполнено снятие на сумму {}", amount);
            } else {
                throw new IllegalArgumentException("Неизвестный тип операции: " + operationType);
            }
        } finally {
            // Очищаем wallet-специфичные данные после операции
            MDC.remove("walletId");
            MDC.remove("operationType");
        }
    }

    public BigDecimal getBalance(UUID walletId) {
        MDC.put("walletId", walletId.toString());
        try {
            BigDecimal balance = repository.findById(walletId)
                    .map(Wallet::getBalance)
                    .orElseThrow(() -> new WalletNotFoundException("Кошелёк не найден: " + walletId));

            log.info("Запрошен баланс кошелька");
            return balance;
        } finally {
            MDC.remove("walletId");
        }
    }
}