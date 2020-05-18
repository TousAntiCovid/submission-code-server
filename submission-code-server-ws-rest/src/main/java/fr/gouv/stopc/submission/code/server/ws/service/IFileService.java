package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import org.springframework.scheduling.annotation.Async;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IFileService {

    /**
     * Method:
     * 1)generate the codes type UUIDv4 between dateFrom to dateTo
     * 2) export from database
     * 3) create one csv file each day between dateFrom to dateTo
     * 4) create file zip with csv files
     * @param numberCodeDay
     * @param lotObject
     * @param dateFrom
     * @param dateTo
     * @return
     */
    @Async
     Optional<ByteArrayOutputStream> zipExport(String numberCodeDay, Lot lotObject, String dateFrom, String dateTo)
             throws SubmissionCodeServerException;

    /**
     * STEP - 1 [ PERSISTING ]
     * @param codePerDays code per days to be generated
     * @param lotObject lot identifier that the series should take
     * @param from start date of the series of days code generation
     * @param to end date of the series of days code generation
     * @throws SubmissionCodeServerException
     */
     void persistUUIDv4CodesFor(String codePerDays, Lot lotObject, OffsetDateTime from, OffsetDateTime to)
             throws SubmissionCodeServerException;

    /**
     * STEP 2 - [ PARSING DATA TO CSV Data ]
     * @param submissionCodeDtos
     * @param dates
     * @return List of csv dataByFilename
     */
    Map<String, byte[]> codeAsCsvData(List<SubmissionCodeDto> submissionCodeDtos, List<@NotNull OffsetDateTime> dates)
            throws SubmissionCodeServerException;


    /**
     * STEP 3 - [ Packaging dataByFilename in a Zip ]
     * @param dataByFilename csv data to be zipped.
     * @return ZipOutputStream instance containing csv data.
     */
    ByteArrayOutputStream packagingCsvDataToZipFile(Map<String, byte[]> dataByFilename)
            throws SubmissionCodeServerException;

    }
