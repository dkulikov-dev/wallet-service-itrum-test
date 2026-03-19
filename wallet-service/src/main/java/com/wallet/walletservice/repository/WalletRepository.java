package com.wallet.walletservice.repository;

import com.wallet.walletservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, java.util.UUID> {

    // Атомарное пополнение
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :id")
    int deposit(@Param("id") java.util.UUID id, @Param("amount") java.math.BigDecimal amount);

    // Атомарное снятие с проверкой баланса
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount " +
            "WHERE w.id = :id AND w.balance >= :amount")
    int withdraw(@Param("id") java.util.UUID id, @Param("amount") java.math.BigDecimal amount);
}