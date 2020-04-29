package fr.gouv.stopc.submission.code.server.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages  = "fr.gouv.stopc")
@SpringBootApplication
public class SubmissionCodeServerClientApiWsRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubmissionCodeServerClientApiWsRestApplication.class, args);
	}

}
