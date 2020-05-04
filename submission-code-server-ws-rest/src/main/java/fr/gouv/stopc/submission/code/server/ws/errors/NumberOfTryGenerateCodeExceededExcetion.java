package fr.gouv.stopc.submission.code.server.ws.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class NumberOfTryGenerateCodeExceededExcetion extends Exception {

    @Value("${generation.code.num.of.tries}")
    private long NUMBER_OF_TRY_IN_CASE_OF_ERROR;

    private final String message = String.format("Number of tries exceeded. %s were authorized.", NUMBER_OF_TRY_IN_CASE_OF_ERROR);

    @Override
    public String getMessage() {
        log.error(message);
        return message;
    }
}
