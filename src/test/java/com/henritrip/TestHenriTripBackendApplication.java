package com.henritrip;

import org.springframework.boot.SpringApplication;

public class TestHenriTripBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(HenriTripBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
