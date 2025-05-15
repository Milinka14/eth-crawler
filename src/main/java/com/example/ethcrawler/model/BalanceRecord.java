package com.example.ethcrawler.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "balances")
public class BalanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "eth_balance", nullable = false, precision = 38, scale = 18)
    private BigDecimal ethBalance;

    @Column(name = "token_balances", columnDefinition = "JSON")
    private String tokenBalancesJson;

    @Column(name = "nft_balances", columnDefinition = "JSON")
    private String nftBalancesJson;

    @Transient
    private Map<String, BigDecimal> tokenBalances;

    @Transient
    private Map<String, List<String>> nftBalances;
    public BalanceRecord() {}

    public BalanceRecord(String address, Long blockNumber, BigDecimal ethBalance, Map<String, BigDecimal> tokenBalances, Map<String, List<String>> nftBalances) {
        this.address = address.toLowerCase();
        this.blockNumber = blockNumber;
        this.ethBalance = ethBalance;
        this.tokenBalances = tokenBalances;
        this.nftBalances = nftBalances;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address.toLowerCase();
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BigDecimal getEthBalance() {
        return ethBalance;
    }

    public void setEthBalance(BigDecimal ethBalance) {
        this.ethBalance = ethBalance;
    }

    public Map<String, BigDecimal> getTokenBalances() {
        return tokenBalances;
    }

    public void setTokenBalances(Map<String, BigDecimal> tokenBalances) {
        this.tokenBalances = tokenBalances;
    }

    public String getTokenBalancesJson() {
        return tokenBalancesJson;
    }

    public void setTokenBalancesJson(String tokenBalancesJson) {
        this.tokenBalancesJson = tokenBalancesJson;
    }

    public String getNftBalancesJson() {
        return nftBalancesJson;
    }

    public void setNftBalancesJson(String nftBalancesJson) {
        this.nftBalancesJson = nftBalancesJson;
    }

    public Map<String, List<String>> getNftBalances() {
        return nftBalances;
    }

    public void setNftBalances(Map<String, List<String>> nftBalances) {
        this.nftBalances = nftBalances;
    }
}