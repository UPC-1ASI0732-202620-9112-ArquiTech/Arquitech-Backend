package com.acme.arquitech.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaAuditing // Enables JPA auditing for @CreatedDate and @LastModifiedDate
public class ArquitechBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArquitechBackEndApplication.class, args);
    }

}
