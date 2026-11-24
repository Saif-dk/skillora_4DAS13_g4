package tn.esprit.classe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ClasseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClasseServiceApplication.class, args);
    }

}
