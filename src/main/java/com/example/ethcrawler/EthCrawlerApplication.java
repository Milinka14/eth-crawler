package com.example.ethcrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
//izbacen get transactions iz controlaera i dodatu i service(nije provjereno) !
public class EthCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EthCrawlerApplication.class, args);
	}

}
