package com.example.ethcrawler.controller;

import com.example.ethcrawler.model.Balance;
import com.example.ethcrawler.model.Transaction;
import com.example.ethcrawler.service.EtherscanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/")
public class TransactionController {

    @Autowired
    private EtherscanService etherscanService;

    @PostMapping("transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@RequestBody TransactionRequest request) {

        String address = request.getAddress();
        Long startBlock = request.getStartBlock();

        if (address == null || startBlock == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Transaction> transactions = etherscanService.getTransactions(address, startBlock, null,true,true,true,true,"asc");

        return ResponseEntity.ok(transactions);
    }

    @PostMapping("balance")
    public ResponseEntity<Balance> getBalance(@RequestBody BalanceRequest request) {
        System.out.println("*---------------------------------------------------------*");
        String address = request.getAddress();
        String date = request.getDate();

        if (address == null || date == null || date.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            long timestamp = sdf.parse(date).getTime() / 1000;
            Long endBlock = etherscanService.getBlockByTimestamp(timestamp);
            if (endBlock == null) {
                return ResponseEntity.badRequest().body(null);
            }

            Balance balance = etherscanService.calculateBalance(address, endBlock);
            System.out.println(balance.getEth().toString());
            return ResponseEntity.ok(balance);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
class TransactionRequest {
    private String address;
    private Long startBlock;

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(Long startBlock) {
        this.startBlock = startBlock;
    }
}

class BalanceRequest {
    private String address;
    private String date;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}









