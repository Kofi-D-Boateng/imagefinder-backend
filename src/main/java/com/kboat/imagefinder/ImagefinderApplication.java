package com.kboat.imagefinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication()
public class ImagefinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImagefinderApplication.class, args);
	}

}
