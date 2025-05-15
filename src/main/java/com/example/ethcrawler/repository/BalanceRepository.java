package com.example.ethcrawler.repository;

import com.example.ethcrawler.model.BalanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BalanceRepository extends JpaRepository<BalanceRecord, Long> {
    Optional<BalanceRecord> findByAddressAndBlockNumber(String address, Long blockNumber);
}