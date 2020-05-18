package fr.gouv.stopc.submission.code.server.ws.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class NumberOfTryGenerateCodeExceededExcetion extends Exception {

    @Value("${generation.code.num.of.tries}")
    private long numberOfTryInCaseOfError;

    private final String message = String.format("Number of tries exceeded. %s were authorized.", numberOfTryInCaseOfError);

    @Override
    public String getMessage() {
        log.error(message);
        return message;
    }
}
