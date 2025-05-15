package com.example.ethcrawler.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Balance {
    private BigDecimal eth;

    private Map<String, BigDecimal> tokens;

    private Map<String, List<String>> nfts;

    private Map<String, String> nftImageUrls;

    public Balance(BigDecimal eth, Map<String, BigDecimal> tokens, Map<String, List<String>> nfts) {
        this.eth = eth;
        this.tokens = tokens;
        this.nfts = nfts;
        this.nftImageUrls = new HashMap<>();
    }

    public BigDecimal getEth() {
        return eth;
    }

    public void setEth(BigDecimal eth) {
        this.eth = eth;
    }

    public Map<String, BigDecimal> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, BigDecimal> tokens) {
        this.tokens = tokens;
    }

    public Map<String, List<String>> getNfts() {
        return nfts;
    }

    public void setNfts(Map<String, List<String>> nfts) {
        this.nfts = nfts;
    }

    public Map<String, String> getNftImageUrls() {
        return nftImageUrls;
    }

    public void setNftImageUrls(Map<String, String> nftImageUrls) {
        this.nftImageUrls = nftImageUrls;
    }

    public void addNftImageUrl(String key, String imageUrl) {
        this.nftImageUrls.put(key, imageUrl);
    }
}