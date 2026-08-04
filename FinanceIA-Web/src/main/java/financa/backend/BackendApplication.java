package financa.backend;

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BackendApplication {
	@Bean
	Random random() {
		return new Random();
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}