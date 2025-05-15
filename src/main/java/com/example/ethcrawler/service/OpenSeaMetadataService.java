package com.example.ethcrawler.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenSeaMetadataService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${opensea.api.key}")
    private String apiKey;

    @Cacheable(value = "nftImages", key = "{#contractAddress, #tokenId}")
    public String getNFTImageUrl(String contractAddress, String tokenId) {
        String url = String.format(
                "https://api.opensea.io/api/v2/chain/ethereum/contract/%s/nfts/%s",
                contractAddress, tokenId
        );
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            JsonNode response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class).getBody();
            JsonNode nft = response.get("nft");
            if (nft == null || nft.get("image_url") == null || nft.get("image_url").isNull()) {
                System.err.println("No image_url found for contract: " + contractAddress + ", tokenId: " + tokenId);
                return null;
            }
            String imageUrl = nft.get("image_url").asText();
            return imageUrl.startsWith("ipfs://") ? "https://ipfs.io/ipfs/" + imageUrl.substring(7) : imageUrl;
        } catch (Exception e) {
            System.err.println("Failed to fetch image for contract: " + contractAddress + ", tokenId: " + tokenId + ": " + e.getMessage());
            return null;
        }
    }
}