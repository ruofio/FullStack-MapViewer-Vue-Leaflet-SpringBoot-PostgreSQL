package com.example.RuofMapviewrProjectBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.RuofMapviewrProjectBackend")  // Make sure this points to your service classes' package
public class RuofMapviewrProjectBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuofMapviewrProjectBackendApplication.class, args);
	}

}
