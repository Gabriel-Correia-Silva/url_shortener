package com.shortener_url.shortener_url_java_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EntityScan("com.shortener_url.entities")
@EnableMongoRepositories(basePackages = "com.shortener_url.repository")
public class ShortenerUrlJavaSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortenerUrlJavaSpringApplication.class, args);
	}

}
