package com.example.ethcrawler.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class Transaction {
    private String type;
    private String from;
    private String to;
    private BigDecimal amount;
    private String timestamp;
    private Long blockNumber;
    private BigDecimal gasUsed;
    private BigDecimal gasPrice;
    private String isError;
    private String input;
    private String contractAddress;
    private String tokenName;
    private String tokenId;

    public Transaction(String type, String from, String to, BigDecimal amount,
                       String timestamp, String blockNumber, String gasUsed,
                       String gasPrice, String isError, String input, String contractAddress, String tokenName, String tokenId) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.timestamp = timestamp;
        this.blockNumber = blockNumber != null ? Long.parseLong(blockNumber) : null;
        this.gasUsed = gasUsed != null ? new BigDecimal(gasUsed) : BigDecimal.ZERO;
        this.gasPrice = gasPrice != null ? new BigDecimal(gasPrice) : BigDecimal.ZERO;
        this.isError = isError;
        this.input = input;
        this.contractAddress = contractAddress != null ?
                contractAddress.toLowerCase() : "";
        this.tokenName = tokenName;
        this.tokenId = tokenId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BigDecimal getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigDecimal gasUsed) {
        this.gasUsed = gasUsed;
    }

    public BigDecimal getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigDecimal gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getIsError() {
        return isError;
    }

    public void setIsError(String isError) {
        this.isError = isError;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    private String formatTimestampFromEtherscan(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestamp * 1000));
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}