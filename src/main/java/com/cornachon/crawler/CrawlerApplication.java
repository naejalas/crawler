package com.cornachon.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrawlerApplication implements ApplicationRunner {

	@Autowired
	private Extractor extractor;



	public static void main(String[] args) {
		SpringApplication.run(CrawlerApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		extractor.start();

	}
}
