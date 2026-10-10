package com.sba.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SbaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbaProjectApplication.class, args);
    }

}
