package com.greenaura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = {"com.greenaura"},
    exclude = {DataSourceAutoConfiguration.class}
)
public class GreenAuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenAuraApplication.class, args);
    }
}
