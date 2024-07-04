package org.hung.kstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.hung.kstream","org.hung.stock"})
public class KStreamApplication {
    
    static public void main(String[] args) {
        SpringApplication.run(KStreamApplication.class,args);
    }
}
