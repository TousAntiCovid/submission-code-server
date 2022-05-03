package fr.gouv.stopc.submission.code.server.business.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class LongCodeService {

    /**
     * generate and stringify a long code
     *
     * @return A randomly generated long code
     */
    public String generateCode() {
        return (UUID.randomUUID()).toString();
    }

    /**
     * @param size number of code to be generated
     * @return
     */
    public List<String> generateCodes(long size) {
        return Stream.generate(this::generateCode)
                .distinct()
                .limit(size)
                .collect(Collectors.toList());
    }
}
