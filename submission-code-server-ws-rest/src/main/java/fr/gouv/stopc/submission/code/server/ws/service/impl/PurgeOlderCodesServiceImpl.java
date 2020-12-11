package fr.gouv.stopc.submission.code.server.ws.service.impl;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.service.IPurgeOlderCodesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.OffsetDateTime;

@Slf4j
@Transactional
@Component
public class PurgeOlderCodesServiceImpl implements IPurgeOlderCodesService {

    private ISubmissionCodeService submissionCodeService;

    @Inject
    public PurgeOlderCodesServiceImpl(ISubmissionCodeService submissionCodeService) {
        this.submissionCodeService = submissionCodeService;
    }

    @Override
    @Scheduled(cron = "${cron.purge.older}")
    @Async
    public void deleteExpiredCodes() {

        OffsetDateTime dateEndValidity = OffsetDateTime.now();
        long nbDeleted = this.submissionCodeService.deleteExpiredCodes(CodeTypeEnum.LONG, dateEndValidity);
        log.info("{} have been deleted", nbDeleted);
    }
}
