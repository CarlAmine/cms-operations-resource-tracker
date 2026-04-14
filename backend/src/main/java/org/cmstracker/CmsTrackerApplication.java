package org.cmstracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CmsTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CmsTrackerApplication.class, args);
    }
}
