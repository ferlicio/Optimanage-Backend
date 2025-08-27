package com.AIT.Optimanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OptimanageApplication {

	public static void main(String[] args) {

		SpringApplication.run(OptimanageApplication.class, args);
	}

}
