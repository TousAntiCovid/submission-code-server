package fr.gouv.stopc.submission.code.server.ws.service.kpiservice;

import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.ws.SubmissionCodeServerClientApiWsRestApplication;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.impl.KPIService;
import fr.gouv.stopc.submission.code.server.ws.utils.FormatDatesKPI;
import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@SpringBootTest(classes = {
        SubmissionCodeServerClientApiWsRestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KPIServiceTest {

    @Value("${stop.covid.qr.code.targetzone}")
    private String targetZoneId;

    @MockBean
    private SubmissionCodeRepository submissionCodeRepositoryMock;

    @Autowired
    private KPIService kpiService;

    @Test
    public void getCodeLongUsedForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate();

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "1")).thenReturn(2L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "2")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(toDate, targetZoneId), "2")).thenReturn(0L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
           result = kpiService.generateKPI(fromDate, toDate);
       }catch (SubmissionCodeServerException s){
           Assert.fail();
       }
       Assert.assertTrue(result.get(0).getNbLongCodesUsed()!=0);
       Assert.assertTrue(result.size() == 1);
    }

    @Test
    public void getCodeShortUsedForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate();

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "2")).thenReturn(2L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(toDate, targetZoneId), "2")).thenReturn(0L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
            result = kpiService.generateKPI(fromDate, toDate);
        }catch (SubmissionCodeServerException s){
            Assert.fail();
        }
        Assert.assertTrue(result.get(0).getNbShortCodesUsed()!=0);
        Assert.assertTrue(result.size() == 1);
    }

    @Test
    public void getCodeShortExpiredForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate();

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "2")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(toDate, targetZoneId), "2")).thenReturn(1L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
            result = kpiService.generateKPI(fromDate, toDate);
        }catch (SubmissionCodeServerException s){
            Assert.fail();
        }
        Assert.assertTrue(result.get(0).getNbShortExpiredCodes()!=0);
        Assert.assertTrue(result.size()== 1);

    }

    @Test
    public void getCodeLongExpiredForDay(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate();

        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "1")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(fromDate, targetZoneId),FormatDatesKPI.normaliseDateTo(fromDate, targetZoneId), "2")).thenReturn(0L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(fromDate,targetZoneId), "1")).thenReturn(1L);
        when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(toDate,targetZoneId), "2")).thenReturn(0L);
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
            result = kpiService.generateKPI(fromDate, toDate);
        }catch (SubmissionCodeServerException s){
            Assert.fail();
        }
        Assert.assertTrue(result.get(0).getNbLongExpiredCodes()!=0);
        Assert.assertTrue(result.size() == 1);

    }

    @Test
    public void validationDate() {
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate().minusDays(1L);
        Assertions.assertThrows(SubmissionCodeServerException.class, () -> {
            kpiService.generateKPI(fromDate, toDate);
        });
    }

    @Test
    public void getKPIForWeek(){
        LocalDate fromDate = OffsetDateTime.now().toLocalDate();
        LocalDate toDate = OffsetDateTime.now().toLocalDate().plusDays(6L);
        int countLongUsed=1;
        int countShortUsed=3;
        int countLongExpired=2;
        int countShortExpired=1;
        int i=1;
        for(LocalDate loopDate= fromDate; loopDate.isBefore(toDate) || loopDate.isEqual(toDate); loopDate=loopDate.plusDays(1L)) {
            when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(loopDate, targetZoneId), FormatDatesKPI.normaliseDateTo(loopDate, targetZoneId), "1")).thenReturn(Long.valueOf(countLongUsed*i));
            when(submissionCodeRepositoryMock.countSubmissionCodeUsedByDate(FormatDatesKPI.normaliseDateFrom(loopDate, targetZoneId), FormatDatesKPI.normaliseDateTo(loopDate, targetZoneId), "2")).thenReturn(Long.valueOf(countShortUsed*i));
            when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(loopDate, targetZoneId), "1")).thenReturn(Long.valueOf(i*(i+1)));
            when(submissionCodeRepositoryMock.countSubmissionCodeExpiredDate(FormatDatesKPI.normaliseDateTo(loopDate, targetZoneId), "2")).thenReturn(Long.valueOf(i*(i+2)));
        i=i+1;
        }
        List<SubmissionCodeServerKpi> result = new ArrayList<>();
        try{
            result = kpiService.generateKPI(fromDate, toDate);
        }catch (SubmissionCodeServerException s){
            Assert.fail();
        }
        Assert.assertTrue(result.size()== 7);

    }


}
