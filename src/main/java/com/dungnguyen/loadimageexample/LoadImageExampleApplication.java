package com.dungnguyen.loadimageexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class LoadImageExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadImageExampleApplication.class, args);
    }

}
