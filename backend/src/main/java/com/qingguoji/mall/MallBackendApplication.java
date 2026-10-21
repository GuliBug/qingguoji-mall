package com.qingguoji.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MallBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MallBackendApplication.class, args);
	}

}
