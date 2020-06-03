package fr.gouv.stopc.submission.code.server.ws.service.kpiservice;

import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.database.service.impl.SubmissionCodeServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.impl.FileServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.service.impl.KPIService;
import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.internal.util.Assert;
import org.springframework.test.context.TestPropertySource;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class KPIServiceTest {

    @Mock
    private SubmissionCodeRepository submissionCodeRepositoryMock;

    @InjectMocks
    private KPIService kpiService;

    private static final String targetZoneId = "Europe/Paris";

    @BeforeEach
    public void init() {

        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void getCodeLongUsedForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate().plusDays(1L);

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "1")).thenReturn(2L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "2")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(fromDate), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(toDate), "2")).thenReturn(0L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
           result = kpiService.generateKPI(fromDate, toDate);
       }catch (SubmissionCodeServerException s){
           Assert.isTrue(false);
       }
       Assert.isTrue(result.get(0).getNbLongCodesUsed()!=0);
       Assert.isTrue(result.size()> 1);
    }

    @Test
    public void getCodeShortUsedForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate().plusDays(1L);

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "2")).thenReturn(2L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(fromDate), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(toDate), "2")).thenReturn(0L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
            result = kpiService.generateKPI(fromDate, toDate);
        }catch (SubmissionCodeServerException s){
            Assert.isTrue(false);
        }
        Assert.isTrue(result.get(0).getNbShortCodesUsed()!=0);
        Assert.isTrue(result.size()> 1);
    }

    @Test
    public void getCodeShortExpiredForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate().plusDays(1L);

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "2")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(fromDate), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(toDate), "2")).thenReturn(1L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
            result = kpiService.generateKPI(fromDate, toDate);
        }catch (SubmissionCodeServerException s){
            Assert.isTrue(false);
        }
        Assert.isTrue(result.get(1).getNbShortExpiredCodes()!=0);
        Assert.isTrue(result.size()> 1);

    }

    @Test
    public void getCodeLongExpiredForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate().plusDays(1L);

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(normaliseDateFrom(fromDate),normaliseDateTo(fromDate), "2")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(fromDate), "1")).thenReturn(1L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(normaliseDateTo(toDate), "2")).thenReturn(0L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
            result = kpiService.generateKPI(fromDate, toDate);
        }catch (SubmissionCodeServerException s){
            Assert.isTrue(false);
        }
        Assert.isTrue(result.get(1).getNbLongExpiredCodes()!=0);
        Assert.isTrue(result.size()> 1);

    }

    @Test
    public void validationDate() {
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate().minusDays(1L);
        Assertions.assertThrows(SubmissionCodeServerException.class, () -> {
            kpiService.generateKPI(fromDate, toDate);
        });
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
}
