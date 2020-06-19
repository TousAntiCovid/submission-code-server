package fr.gouv.stopc.submission.code.server.ws.service.impl;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.IKpiService;
import fr.gouv.stopc.submission.code.server.ws.utils.FormatDatesKPI;
import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KpiServiceImpl implements IKpiService {

    @Value("${stop.covid.qr.code.targetzone}")
    private String targetZoneId;

    private SubmissionCodeRepository submissionCodeRepository;

    @Autowired
    public KpiServiceImpl (SubmissionCodeRepository submissionCodeRepository){
        this.submissionCodeRepository= submissionCodeRepository;
    }

    /**
     * Create the KPI for each day from the dateFrom until dateTo. KPI of expired ou used codes (long and short).
     * @param dateFrom
     * @param dateTo
     * @return
     * @throws SubmissionCodeServerException
     */
    @Override
    public List<SubmissionCodeServerKpi> generateKPI(LocalDate dateFrom, LocalDate dateTo) throws SubmissionCodeServerException {
        if (!validationDate(dateFrom, dateTo)){
         throw new SubmissionCodeServerException(SubmissionCodeServerException.ExceptionEnum.INVALID_DATE);
        }
        List<SubmissionCodeServerKpi> submissionCodeServerKpis = new ArrayList<>();
        LocalDate loopDate;
        for(loopDate = dateFrom; loopDate.isBefore(dateTo) || loopDate.isEqual(dateTo); loopDate=loopDate.plusDays(1L)){
            long resultLong = submissionCodeRepository.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(loopDate, targetZoneId), FormatDatesKPI.normaliseDateTo(loopDate,targetZoneId), CodeTypeEnum.LONG.getTypeCode());
            long resultShort = submissionCodeRepository.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(loopDate,targetZoneId), FormatDatesKPI.normaliseDateTo(loopDate, targetZoneId),CodeTypeEnum.SHORT.getTypeCode());
            long resultLongExpire= calculateExpiredCode(loopDate,CodeTypeEnum.LONG.getTypeCode());
            long resultShortExpire= calculateExpiredCode(loopDate,CodeTypeEnum.SHORT.getTypeCode());
            SubmissionCodeServerKpi submissionCodeServerKpi = SubmissionCodeServerKpi.builder().date(loopDate).nbShortExpiredCodes(resultShortExpire).nbLongExpiredCodes(resultLongExpire).nbLongCodesUsed(resultLong).nbShortCodesUsed(resultShort).build();
            submissionCodeServerKpis.add(submissionCodeServerKpi);
        }
        return submissionCodeServerKpis;
    }

    private boolean validationDate(LocalDate dateFrom, LocalDate dateTo) {
        return dateFrom.isBefore(dateTo) || dateFrom.isEqual(dateTo);
    }

    /**
     * The method creates KPI of expired code, the method searches the expired code for localDate. First it searches the number of expired codes until localDate-1,
     * then it searches the number of expired codes until localDate. The result is the subtraction of these counts.
     * @param localDate
     * @param typeCode
     * @return
     */
    private long calculateExpiredCode(LocalDate localDate, String typeCode){
        LocalDate dateLeft = localDate.minusDays(1L);
        LocalDate dateRight= localDate;
        long resultExpireLeft = submissionCodeRepository.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(dateLeft, targetZoneId), typeCode);
        long resultExpireRight = submissionCodeRepository.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(dateRight, targetZoneId), typeCode);
        return (resultExpireRight - resultExpireLeft);

    }
}
