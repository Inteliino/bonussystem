package lv.janis.bonussystem;

import lv.janis.bonussystem.model.AppUser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BonussystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BonussystemApplication.class, args);
    }

    @Bean
    CommandLineRunner createAdmin(AppUserRepository appUserRepository) {
        return args -> {
            AppUser admin = appUserRepository.findByUsername("admin").orElse(null);

            if (admin == null) {
                admin = new AppUser();
                admin.setUsername("admin");
            }

            admin.setPassword("admin");
            admin.setRole("ADMIN");

            appUserRepository.save(admin);
        };
    }
}