package com.threeboys.toneup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class ToneupApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToneupApplication.class, args);
	}

}
