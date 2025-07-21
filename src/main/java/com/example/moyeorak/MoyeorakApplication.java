package com.example.moyeorak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // ✅ 이게 꼭 필요!
@SpringBootApplication
public class MoyeorakApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoyeorakApplication.class, args);
    }

}
