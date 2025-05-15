package com.example.ethcrawler.service;

import com.example.ethcrawler.model.*;
import com.example.ethcrawler.repository.BalanceRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EtherscanService {

    @Value("${etherscan.api.key}")
    private String apiKey;

    @Value("${etherscan.api.url}")
    private String apiUrl;

    private final BalanceRepository balanceRepository;

    @Autowired
    private OpenSeaMetadataService openSeaMetadataService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int PAGE_SIZE = 1000;
    private static final long API_DELAY_MS = 250;
    private static final int MAX_RETRIES = 1;

    @Autowired
    public EtherscanService(BalanceRepository balanceRepository, OpenSeaMetadataService openSeaMetadataService) {
        this.balanceRepository = balanceRepository;
        this.openSeaMetadataService = openSeaMetadataService;
        loadTrustedTokens();
    }

    private static final Map<String, TokenInfo> TRUSTED_TOKENS = new HashMap<>();

    private static class TokenInfo {
        String name;
        String symbol;
        int decimals;

        TokenInfo(String name, String symbol, int decimals) {
            this.name = name;
            this.symbol = symbol;
            this.decimals = decimals;
        }
    }

    private void loadTrustedTokens() {
        String[] urls = {
                "https://tokens.coingecko.com/ethereum/all.json",
                "https://tokens.1inch.eth.link"
        };

        TRUSTED_TOKENS.clear();
        int totalLoaded = 0;

        for (String url : urls) {
            try {
                String json = restTemplate.getForObject(url, String.class);
                if (json != null) {
                    Map<String, Object> tokenList = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                    List<Map<String, Object>> tokens = (List<Map<String, Object>>) tokenList.get("tokens");
                    for (Map<String, Object> token : tokens) {
                        int chainId = (int) token.get("chainId");
                        if (chainId != 1) continue;
                        String address = ((String) token.get("address")).toLowerCase();
                        String name = (String) token.get("name");
                        String symbol = (String) token.get("symbol");
                        int decimals = token.get("decimals") instanceof Integer ? (int) token.get("decimals") : 18;
                        TRUSTED_TOKENS.putIfAbsent(address, new TokenInfo(name, symbol, decimals));
                    }
                    totalLoaded = TRUSTED_TOKENS.size();
                    System.out.println("Loaded tokens from: " + url);
                } else {
                    System.err.println("Null response from: " + url + "; skipping");
                }
            } catch (Exception e) {
                System.err.println("Failed to load from: " + url + " -> " + e.getMessage());
            }
        }
        System.out.println("Total trusted tokens loaded: " + totalLoaded);
    }

    @Cacheable(value = "recentTxs", key = "{#address, #startBlock, #endBlock, #fetchNormal, #fetchInternal, #fetchTokens, #fetchNFTs, #sortOrder}")
    public List<Transaction> getTransactions(String address, Long startBlock, Long endBlock,
                                             boolean fetchNormal, boolean fetchInternal, boolean fetchTokens, boolean fetchNFTs,
                                             String sortOrder) {
        List<Transaction> transactions = new ArrayList<>();

        boolean hasMoreNormal = fetchNormal;
        boolean hasMoreInternal = fetchInternal;
        boolean hasMoreTokens = fetchTokens;
        boolean hasMoreNFTs = fetchNFTs;

        String startBlockNormal = startBlock != null ? startBlock.toString() : "0";
        String startBlockInternal = startBlock != null ? startBlock.toString() : "0";
        String startBlockToken = startBlock != null ? startBlock.toString() : "0";
        String startBlockNFT = startBlock != null ? startBlock.toString() : "0";
        String endBlockStr = endBlock != null ? endBlock.toString() : "99999999";

        long lastBlockNormal = 0;
        long lastBlockInternal = 0;
        long lastBlockToken = 0;
        long lastBlockNFT = 0;

        while (hasMoreNormal || hasMoreInternal || hasMoreTokens) {
            int cnt = 0;

            // Fetch normal transactions
            if (hasMoreNormal) {
                String ethUrl = String.format("%s?chainid=1&module=account&action=txlist&address=%s&startblock=%s&endblock=%s&page=1&offset=%d&sort=%s&apikey=%s",
                        apiUrl, address, startBlockNormal, endBlockStr, PAGE_SIZE,sortOrder, apiKey);
                try {
                    String ethJson = fetchWithRetry(ethUrl, "normal", startBlockNormal);
                    if (ethJson == null) {
                        hasMoreNormal = false;
                        continue;
                    }
                    EtherscanResponse<EtherscanTransaction> ethResponse = objectMapper.readValue(
                            ethJson, new TypeReference<EtherscanResponse<EtherscanTransaction>>() {});
                    if (ethResponse != null && "1".equals(ethResponse.getStatus())) {
                        List<EtherscanTransaction> ethTxs = ethResponse.getResult();
                        if (ethTxs != null && !ethTxs.isEmpty()) {
                            lastBlockNormal = Long.parseLong(ethTxs.get(ethTxs.size() - 1).getBlockNumber());
                            for (EtherscanTransaction tx : ethTxs) {
                                if (ethTxs.size() == PAGE_SIZE && Long.parseLong(tx.getBlockNumber()) == lastBlockNormal) {
                                    continue;
                                }
                                transactions.add(new Transaction(
                                        "ETH",
                                        tx.getFrom(),
                                        tx.getTo(),
                                        new BigDecimal(tx.getValue()).divide(BigDecimal.TEN.pow(18)),
                                        formatTimestamp(Long.parseLong(tx.getTimeStamp())),
                                        tx.getBlockNumber(),
                                        tx.getGasUsed(),
                                        tx.getGasPrice(),
                                        tx.getIsError(),
                                        tx.getInput(),
                                        tx.getContractAddress(),
                                        tx.getTokenName(),
                                        null
                                ));
                                cnt++;
                            }
                            System.out.println("Fetched " + cnt + " normal transactions, startBlock=" + startBlockNormal + ", lastBlock=" + lastBlockNormal);
                            startBlockNormal = String.valueOf(lastBlockNormal);
                            hasMoreNormal = ethTxs.size() == PAGE_SIZE;
                        } else {
                            hasMoreNormal = false;
                            System.out.println("No normal transactions for startBlock=" + startBlockNormal);
                        }
                    } else {
                        hasMoreNormal = false;
                        System.err.println("API error for normal transactions at startBlock=" + startBlockNormal + ": " + (ethResponse != null ? ethResponse.getMessage() : "Null response"));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to fetch normal transactions at startBlock=" + startBlockNormal + ": " + e.getMessage());
                    hasMoreNormal = false;
                }
            }
            System.out.println("Total normal transactions in batch: " + cnt);
            cnt = 0;

            // Fetch internal transactions
            if (hasMoreInternal) {
                String internalUrl = String.format("%s?chainid=1&module=account&action=txlistinternal&address=%s&startblock=%s&endblock=%s&page=1&offset=%d&sort=%s&apikey=%s",
                        apiUrl, address, startBlockInternal, endBlockStr, PAGE_SIZE, sortOrder,apiKey);
                try {
                    String internalJson = fetchWithRetry(internalUrl, "internal", startBlockInternal);
                    if (internalJson == null) {
                        hasMoreInternal = false;
                        continue;
                    }
                    EtherscanResponse<EtherscanTransaction> internalResponse = objectMapper.readValue(
                            internalJson, new TypeReference<EtherscanResponse<EtherscanTransaction>>() {});
                    if (internalResponse != null && "1".equals(internalResponse.getStatus())) {
                        List<EtherscanTransaction> internalTxs = internalResponse.getResult();
                        if (internalTxs != null && !internalTxs.isEmpty()) {
                            lastBlockInternal = Long.parseLong(internalTxs.get(internalTxs.size() - 1).getBlockNumber());
                            for (EtherscanTransaction tx : internalTxs) {
                                if (internalTxs.size() == PAGE_SIZE && Long.parseLong(tx.getBlockNumber()) == lastBlockInternal) {
                                    continue;
                                }
                                transactions.add(new Transaction(
                                        "ETH (internal)",
                                        tx.getFrom(),
                                        tx.getTo(),
                                        new BigDecimal(tx.getValue()).divide(BigDecimal.TEN.pow(18)),
                                        formatTimestamp(Long.parseLong(tx.getTimeStamp())),
                                        tx.getBlockNumber(),
                                        "0",
                                        "0",
                                        tx.getIsError(),
                                        tx.getInput(),
                                        tx.getContractAddress(),
                                        tx.getTokenName(),
                                        null
                                ));
                                cnt++;
                            }
                            System.out.println("Fetched " + cnt + " internal transactions, startBlock=" + startBlockInternal + ", lastBlock=" + lastBlockInternal);
                            startBlockInternal = String.valueOf(lastBlockInternal);
                            hasMoreInternal = internalTxs.size() == PAGE_SIZE;
                        } else {
                            hasMoreInternal = false;
                            System.out.println("No internal transactions for startBlock=" + startBlockInternal);
                        }
                    } else {
                        hasMoreInternal = false;
                        System.err.println("API error for internal transactions at startBlock=" + startBlockInternal + ": " + (internalResponse != null ? internalResponse.getMessage() : "Null response"));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to fetch internal transactions at startBlock=" + startBlockInternal + ": " + e.getMessage());
                    hasMoreInternal = false;
                }
            }
            System.out.println("Total internal transactions in batch: " + cnt);
            cnt = 0;

            // Fetch token transactions
            if (hasMoreTokens) {
                String tokenUrl = String.format("%s?chainid=1&module=account&action=tokentx&address=%s&startblock=%s&endblock=%s&page=1&offset=%d&sort=asc&apikey=%s",
                        apiUrl, address, startBlockToken, endBlockStr, PAGE_SIZE, apiKey);
                try {
                    String tokenJson = fetchWithRetry(tokenUrl, "token", startBlockToken);
                    if (tokenJson == null) {
                        hasMoreTokens = false;
                        continue;
                    }
                    EtherscanResponse<EtherscanTransaction> tokenResponse = objectMapper.readValue(
                            tokenJson, new TypeReference<EtherscanResponse<EtherscanTransaction>>() {});
                    if (tokenResponse != null && "1".equals(tokenResponse.getStatus())) {
                        List<EtherscanTransaction> tokenTxs = tokenResponse.getResult();
                        if (tokenTxs != null && !tokenTxs.isEmpty()) {
                            lastBlockToken = Long.parseLong(tokenTxs.get(tokenTxs.size() - 1).getBlockNumber());
                            for (EtherscanTransaction tx : tokenTxs) {
                                if (tokenTxs.size() == PAGE_SIZE && Long.parseLong(tx.getBlockNumber()) == lastBlockToken) {
                                    continue;
                                }
                                String contractAddress = tx.getContractAddress().toLowerCase();
                                if (!TRUSTED_TOKENS.containsKey(contractAddress)) {
                                    System.out.println("Ignoring untrusted token: " + tx.getTokenName() + " (" + contractAddress + ")");
                                    continue;
                                }
                                TokenInfo tokenInfo = TRUSTED_TOKENS.get(contractAddress);
                                BigDecimal value = new BigDecimal(tx.getValue());
                                if (value.compareTo(BigDecimal.ZERO) > 0) {
                                    int decimals = tokenInfo.decimals;
                                    transactions.add(new Transaction(
                                            "Token (" + tokenInfo.symbol + ")",
                                            tx.getFrom(),
                                            tx.getTo(),
                                            value.divide(BigDecimal.TEN.pow(decimals)),
                                            formatTimestamp(Long.parseLong(tx.getTimeStamp())),
                                            tx.getBlockNumber(),
                                            tx.getGasUsed(),
                                            tx.getGasPrice(),
                                            "0",
                                            tx.getInput(),
                                            contractAddress,
                                            tokenInfo.name,
                                            null
                                    ));
                                    cnt++;
                                }
                            }
                            System.out.println("Fetched " + cnt + " token transactions, startBlock=" + startBlockToken + ", lastBlock=" + lastBlockToken);
                            startBlockToken = String.valueOf(lastBlockToken);
                            hasMoreTokens = tokenTxs.size() == PAGE_SIZE;
                        } else {
                            hasMoreTokens = false;
                            System.out.println("No token transactions for startBlock=" + startBlockToken);
                        }
                    } else {
                        hasMoreTokens = false;
                        System.err.println("API error for token transactions at startBlock=" + startBlockToken + ": " + (tokenResponse != null ? tokenResponse.getMessage() : "Null response"));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to fetch token transactions at startBlock=" + startBlockToken + ": " + e.getMessage());
                    hasMoreTokens = false;
                }
            }
            System.out.println("Total token transactions in batch: " + cnt);
            cnt = 0;

            // Fetch ERC721 token transactions
            if (hasMoreNFTs) {
                String nftUrl = String.format("%s?chainid=1&module=account&action=tokennfttx&address=%s&startblock=%s&endblock=%s&page=1&offset=%d&sort=asc&apikey=%s",
                        apiUrl, address, startBlockNFT, endBlockStr, PAGE_SIZE, apiKey);
                try {
                    String nftJson = fetchWithRetry(nftUrl, "nft", startBlockNFT);
                    if (nftJson == null) {
                        hasMoreNFTs = false;
                        continue;
                    }
                    EtherscanResponse<EtherscanTransaction> nftResponse = objectMapper.readValue(
                            nftJson, new TypeReference<EtherscanResponse<EtherscanTransaction>>() {});
                    if (nftResponse != null && "1".equals(nftResponse.getStatus())) {
                        List<EtherscanTransaction> nftTxs = nftResponse.getResult();
                        if (nftTxs != null && !nftTxs.isEmpty()) {
                            lastBlockNFT = Long.parseLong(nftTxs.get(nftTxs.size() - 1).getBlockNumber());
                            for (EtherscanTransaction tx : nftTxs) {
                                if (nftTxs.size() == PAGE_SIZE && Long.parseLong(tx.getBlockNumber()) == lastBlockNFT) {
                                    continue;
                                }
                                String contractAddress = tx.getContractAddress().toLowerCase();
//                                if (!TRUSTED_TOKENS.containsKey(contractAddress) || !TRUSTED_TOKENS.get(contractAddress).isERC721) {
//                                    System.out.println("Ignoring untrusted or non-ERC721 token: " + tx.getTokenName() + " (" + contractAddress + ")");
//                                    continue;
//                                }
                                //TokenInfo tokenInfo = TRUSTED_TOKENS.get(contractAddress);
                                String tokenId = tx.getTokenID();
                                if (tokenId != null && !tokenId.isEmpty()) {
                                    transactions.add(new Transaction(
                                            "NFT (" + tokenId + ")",
                                            tx.getFrom(),
                                            tx.getTo(),
                                            BigDecimal.ONE,
                                            formatTimestamp(Long.parseLong(tx.getTimeStamp())),
                                            tx.getBlockNumber(),
                                            tx.getGasUsed(),
                                            tx.getGasPrice(),
                                            "0",
                                            tx.getInput(),
                                            contractAddress,
                                            tx.getTokenName(),
                                            tokenId
                                    ));
                                    cnt++;
                                }
                            }
                            System.out.println("Fetched " + cnt + " ERC721 transactions, startBlock=" + startBlockNFT + ", lastBlock=" + lastBlockNFT);
                            startBlockNFT = String.valueOf(lastBlockNFT);
                            hasMoreNFTs = nftTxs.size() == PAGE_SIZE;
                        } else {
                            hasMoreNFTs = false;
                            System.out.println("No ERC721 transactions for startBlock=" + startBlockNFT);
                        }
                    } else {
                        hasMoreNFTs = false;
                        System.err.println("API error for ERC721 transactions at startBlock=" + startBlockNFT + ": " + (nftResponse != null ? nftResponse.getMessage() : "Null response"));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to fetch ERC721 transactions at startBlock=" + startBlockNFT + ": " + e.getMessage());
                    hasMoreNFTs = false;
                }
            }
            System.out.println("Total ERC721 transactions in batch: " + cnt);

            try {
                Thread.sleep(API_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Total transactions fetched: " + transactions.size());

        transactions.sort(Comparator.comparingLong(Transaction::getBlockNumber));

        return transactions;
    }

    @Transactional
    public Balance calculateBalance(String address, Long endBlock) {

        Optional<BalanceRecord> exactRecordOpt = balanceRepository.findByAddressAndBlockNumber(address.toLowerCase(), endBlock);
        if (exactRecordOpt.isPresent()) {
            BalanceRecord record = exactRecordOpt.get();
            try {
                Map<String, BigDecimal> tokenBalances = objectMapper.readValue(
                        record.getTokenBalancesJson(), new TypeReference<Map<String, BigDecimal>>() {});
                Map<String, List<String>> nftBalances = objectMapper.readValue(
                        record.getNftBalancesJson(), new TypeReference<Map<String, List<String>>>() {});
                Balance balance = new Balance(record.getEthBalance(), tokenBalances, nftBalances);
                // Fetch image URLs for cached NFTs
                nftBalances.forEach((key, properties) -> {
                    String tokenId = properties.get(1);
                    String contractAddress = key.substring(tokenId.length());
                    String imageUrl = openSeaMetadataService.getNFTImageUrl(contractAddress, tokenId);
                    // Or use: openSeaMetadataService.getNFTImageUrlV1(contractAddress, tokenId);
                    if (imageUrl != null) {
                        balance.addNftImageUrl(key, imageUrl);
                    }
                });
                System.out.println("Found existing balance for " + address + " at block " + endBlock);
                return balance;
            } catch (Exception e) {
                System.err.println("Failed to parse balances JSON: " + e.getMessage());
            }
        }

        BigDecimal ethBalance = calculateEthBalanceAtBlock(address, endBlock);

        Map<String, BigDecimal> tokenBalances = new HashMap<>();
        Map<String, List<String>> nftBalances = new HashMap<>();

        List<Transaction> txsToProcess = getTransactions(address, 0L, endBlock, false, false, true, true, "asc");

        for (Transaction tx : txsToProcess) {
            if (endBlock != null && tx.getBlockNumber() != null && tx.getBlockNumber() > endBlock) {
                continue;
            }

            if (tx.getType().startsWith("Token")) {
                String contractAddress = tx.getContractAddress().toLowerCase();
                if (!TRUSTED_TOKENS.containsKey(contractAddress)) continue;
                TokenInfo tokenInfo = TRUSTED_TOKENS.get(contractAddress);
                String symbol = tokenInfo.symbol;
                tokenBalances.putIfAbsent(symbol, BigDecimal.ZERO);
                if (address.equalsIgnoreCase(tx.getFrom())) {
                    tokenBalances.put(symbol, tokenBalances.get(symbol).subtract(tx.getAmount()));
                }
                if (address.equalsIgnoreCase(tx.getTo())) {
                    tokenBalances.put(symbol, tokenBalances.get(symbol).add(tx.getAmount()));
                }
            } else if (tx.getType().startsWith("NFT")) {
                String contractAddress = tx.getContractAddress().toLowerCase();
                String tokenId = tx.getTokenId();
                if (tokenId == null || tokenId.isEmpty()) continue;
                String key = tokenId + contractAddress;
                if (address.equalsIgnoreCase(tx.getFrom())) {
                    nftBalances.remove(key);
                } else if (address.equalsIgnoreCase(tx.getTo())) {
                    List<String> properties = new ArrayList<>();
                    properties.add(tx.getTokenName());
                    properties.add(tokenId);
                    nftBalances.put(key, properties);
                }
            }
        }

        Balance balance = new Balance(ethBalance, tokenBalances, nftBalances);
        nftBalances.forEach((key, properties) -> {
            String tokenId = properties.get(1);
            String contractAddress = key.substring(tokenId.length());
            String imageUrl = openSeaMetadataService.getNFTImageUrl(contractAddress, tokenId);
            if (imageUrl != null) {
                balance.addNftImageUrl(key, imageUrl);
            }
        });

        try {
            String tokenBalancesJson = objectMapper.writeValueAsString(tokenBalances);
            String nftBalancesJson = objectMapper.writeValueAsString(nftBalances);
            BalanceRecord newRecord = new BalanceRecord(address.toLowerCase(), endBlock, ethBalance, tokenBalances, nftBalances);
            newRecord.setTokenBalancesJson(tokenBalancesJson);
            newRecord.setNftBalancesJson(nftBalancesJson);
            balanceRepository.save(newRecord);
            System.out.println("Saved new balance for " + address + " at block " + endBlock);
        } catch (Exception e) {
            System.err.println("Failed to save balance: " + e.getMessage());
        }

        return balance;
    }

    private BigDecimal calculateEthBalanceAtBlock(String address, Long endBlock) {

        BigDecimal balanceWei = fetchOnChainBalanceWei(address, null);
        BigDecimal balanceEth = balanceWei.divide(BigDecimal.TEN.pow(18));

        List<Transaction> txs = getTransactions(address, endBlock + 1, null, true, true, false, false, "desc");

        for (Transaction tx : txs) {
            if ("1".equals(tx.getIsError())) {
                if (address.equalsIgnoreCase(tx.getFrom())) {
                    balanceEth = balanceEth.add(calculateGasFee(tx));
                }
                continue;
            }
            if ("ETH".equals(tx.getType())) {
                if (address.equalsIgnoreCase(tx.getFrom()) && address.equalsIgnoreCase(tx.getTo())) {
                    balanceEth = balanceEth.add(calculateGasFee(tx));
                    System.out.println("Self-transfer, gas fee added");
                }
                else {
                    if (address.equalsIgnoreCase(tx.getFrom())) {
                        balanceEth = balanceEth.add(tx.getAmount()).add(calculateGasFee(tx));
                    }
                    if (address.equalsIgnoreCase(tx.getTo())) {
                        balanceEth = balanceEth.subtract(tx.getAmount());
                    }
                }
            } else if ("ETH (internal)".equals(tx.getType())) {
                if (address.equalsIgnoreCase(tx.getFrom())) {
                    balanceEth = balanceEth.add(tx.getAmount());
                }
                if (address.equalsIgnoreCase(tx.getTo())) {
                    balanceEth = balanceEth.subtract(tx.getAmount());
                }
            }
        }

        return balanceEth;
    }

    public BigDecimal fetchOnChainBalanceWei(String address, Long blockNumber) {
        String tag = blockNumber != null ? blockNumber.toString() : "latest";
        String url = String.format(
                "%s?chainid=1&module=account&action=balance&address=%s&tag=%s&apikey=%s",
                apiUrl, address, tag, apiKey
        );
        try {
            String respJson = restTemplate.getForObject(url, String.class);
            BalanceResponse resp = objectMapper.readValue(respJson, BalanceResponse.class);
            if ("1".equals(resp.getStatus())) {
                return new BigDecimal(resp.getResult());
            } else {
                System.err.println("API error: " + resp.getMessage());
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch balance for " + address + " at block " + blockNumber + ": " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }


    @Cacheable(value = "blockByTimestamp", key = "#timestamp")
    public Long getBlockByTimestamp(long timestamp) {
        String url = String.format("https://api.etherscan.io/api?module=block&action=getblocknobytime&timestamp=%d&closest=before&apikey=%s",
                timestamp, apiKey);
        try {
            BlockResponse response = restTemplate.getForObject(url, BlockResponse.class);
            if (response != null && "1".equals(response.getStatus())) {
                return Long.parseLong(response.getResult());
            }
            System.err.println("Block API error: " + (response != null ? response.getMessage() : "Null response"));
        } catch (Exception e) {
            System.err.println("Failed to fetch block by timestamp: " + e.getMessage());
        }
        return null;
    }

    private String fetchWithRetry(String url, String type, String startBlock) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String json = restTemplate.getForObject(url, String.class);
                if (json != null) {
                    return json;
                }
                System.err.println("Null response for " + type + " transactions at startBlock=" + startBlock + ", attempt " + (attempt + 1));
            } catch (Exception e) {
                System.err.println("Error fetching " + type + " transactions at startBlock=" + startBlock + ", attempt " + (attempt + 1) + ": " + e.getMessage());
            }
            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(API_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    private BigDecimal calculateGasFee(Transaction tx) {
        if (tx.getGasUsed() == null || tx.getGasPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal fee = tx.getGasUsed().multiply(tx.getGasPrice()).divide(BigDecimal.TEN.pow(18));
        System.out.println("Gas fee: " + fee);
        return fee;
    }
    private NFTMetadata fetchNFTMetadata(String contractAddress, String tokenId) {
        String url = String.format("https://api.opensea.io/api/v1/asset/%s/%s", contractAddress, tokenId);
        try {
            String json = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(json, NFTMetadata.class);
        } catch (Exception e) {
            System.err.println("Failed to fetch NFT metadata: " + e.getMessage());
            return new NFTMetadata(); // Return empty metadata
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Force UTC timezone
        return sdf.format(new Date(timestamp * 1000));
    }
}