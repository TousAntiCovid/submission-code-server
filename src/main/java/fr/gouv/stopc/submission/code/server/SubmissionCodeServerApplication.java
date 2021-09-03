package fr.gouv.stopc.submission.code.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

import java.util.TimeZone;

@ComponentScan(basePackages = "fr.gouv.stopc")
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SubmissionCodeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubmissionCodeServerApplication.class, args);
    }

    @PostConstruct
    void started() {
        // set JVM timezone as UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
