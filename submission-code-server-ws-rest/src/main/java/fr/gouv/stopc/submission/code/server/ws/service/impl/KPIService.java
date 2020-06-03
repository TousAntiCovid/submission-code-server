package fr.gouv.stopc.submission.code.server.ws.service.impl;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.IKPIService;
import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KPIService implements IKPIService {

    private SubmissionCodeRepository submissionCodeRepository;

    private static final String targetZoneId = "Europe/Paris";

    @Inject
    public KPIService (SubmissionCodeRepository submissionCodeRepository){
        this.submissionCodeRepository= submissionCodeRepository;
    }

    @Override
    public List<SubmissionCodeServerKpi> generateKPI(LocalDate dateFrom, LocalDate dateTo) throws SubmissionCodeServerException {
        if (!validationDate(dateFrom, dateTo)){
         throw new SubmissionCodeServerException(SubmissionCodeServerException.ExceptionEnum.INVALID_DATE);
        }
        List<SubmissionCodeServerKpi> submissionCodeServerKpis = new ArrayList<>();
        long diff= ChronoUnit.DAYS.between(dateFrom, dateTo);
        int days = (int) diff + 1;
        LocalDate loopDate = dateFrom;
        for(int i= 0; i<days; i++){
            long resultLong = submissionCodeRepository.countSubmissionCodeUsedByDate(normaliseDateFrom(loopDate), normaliseDateTo(loopDate), CodeTypeEnum.LONG.getTypeCode());
            long resultShort = submissionCodeRepository.countSubmissionCodeUsedByDate(normaliseDateFrom(loopDate), normaliseDateTo(loopDate),CodeTypeEnum.SHORT.getTypeCode());
            long resultLongExpire= calculateExpiredCode(loopDate,CodeTypeEnum.LONG.getTypeCode());
            long resultShortExpire= calculateExpiredCode(loopDate,CodeTypeEnum.SHORT.getTypeCode());
            SubmissionCodeServerKpi submissionCodeServerKpi = SubmissionCodeServerKpi.builder().date(loopDate).nbShortExpiredCodes(resultShortExpire).nbLongExpiredCodes(resultLongExpire).nbLongCodesUsed(resultLong).nbShortCodesUsed(resultShort).build();
            submissionCodeServerKpis.add(submissionCodeServerKpi);
            loopDate=loopDate.plusDays(1L);
        }
        return submissionCodeServerKpis;
    }

    private boolean validationDate(LocalDate dateFrom, LocalDate dateTo) {
        return dateFrom.isBefore(dateTo) || dateFrom.isEqual(dateTo);
    }

    private OffsetDateTime normaliseDateTo(LocalDate dateTmpTo) {
        LocalTime time= LocalTime.of(23,59,59,999);
        ZoneOffset zoneOffset= OffsetDateTime.now(ZoneId.of(this.targetZoneId)).getOffset();
        OffsetDateTime dateToZone = OffsetDateTime.of(dateTmpTo,time, zoneOffset);
        return dateToZone.withOffsetSameInstant(ZoneOffset.of("Z"));
    }

    private OffsetDateTime normaliseDateFrom(LocalDate dateFrom) {
        LocalTime time= LocalTime.MIN;
        LocalDateTime.of(dateFrom,time).atZone(ZoneId.systemDefault());
        ZoneOffset zoneOffset= OffsetDateTime.now(ZoneId.of(this.targetZoneId)).getOffset();
        OffsetDateTime dateBeginZone = OffsetDateTime.of(dateFrom,time, zoneOffset);
        return dateBeginZone.withOffsetSameInstant(ZoneOffset.of("Z"));
    }

    private long calculateExpiredCode(LocalDate localDate, String typeCode){
        LocalDate dateLeft = localDate.minusDays(1L);
        LocalDate dateRight= localDate;
        long resultExpireLeft = submissionCodeRepository.countSubmissionCodeExpiredDate(normaliseDateTo(dateLeft), typeCode);
        long resultExpireRight = submissionCodeRepository.countSubmissionCodeExpiredDate(normaliseDateTo(dateRight), typeCode);
        return (resultExpireRight - resultExpireLeft);

    }
}
