package fr.gouv.stopc.submission.code.server.commun.service.impl;

import fr.gouv.stopc.submission.code.server.commun.service.ILongCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class LongCodeServiceImpl implements ILongCodeService {


    public String generateCode() {
        log.info("Generating random UUIDv4 code");
        return (UUID.randomUUID()).toString();
    }

    public List<String> generateCodes(long size) {
        return Stream.generate(this::generateCode)
                .distinct()
                .limit(size)
                .collect(Collectors.toList());
    }
}
