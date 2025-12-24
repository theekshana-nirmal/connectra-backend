package uwu.connectra.connectra_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ConnectraBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectraBackendApplication.class, args);
	}

}
