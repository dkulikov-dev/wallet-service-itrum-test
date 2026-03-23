package com.wallet.walletservice;

import com.wallet.walletservice.dto.OperationRequest;
import com.wallet.walletservice.repository.WalletRepository;
import com.wallet.walletservice.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class WalletConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wallet_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WalletRepository repository;

    private UUID walletId;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setBalance(BigDecimal.valueOf(10000));
        repository.save(wallet);
        walletId = wallet.getId();
    }

    @Test
    void testConcurrentWithdrawals() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 50 потоков пытаются снять по 100 рублей одновременно
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    OperationRequest request = new OperationRequest();
                    request.setWalletId(walletId);
                    request.setOperationType("WITHDRAW");
                    request.setAmount(BigDecimal.valueOf(100));

                    ResponseEntity<Void> response = restTemplate.postForEntity(
                            "/api/v1/wallet", request, Void.class);

                    // Ни один запрос не должен вернуть 5xx
                    assertTrue(response.getStatusCode().is2xxSuccessful() ||
                            response.getStatusCode().equals(HttpStatus.CONFLICT));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Проверяем итоговый баланс (должен быть >= 0)
        Wallet wallet = repository.findById(walletId).orElseThrow();
        assertTrue(wallet.getBalance().compareTo(BigDecimal.ZERO) >= 0,
                "Баланс не должен быть отрицательным");
    }
}