package fr.gouv.stopc.submission.code.server.ws.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.ws.errors.NumberOfTryGenerateCodeExceededExcetion;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface IFileService {

    /**
     * Method:
     * 1)generate the codes type UUIDv4 between dateFrom to dateTo
     * 2) export from database
     * 3) create one csv file each day between dateFrom to dateTo
     * 4) create file zip with csv files
     * @param numberCodeDay
     * @param lot
     * @param dateFrom
     * @param dateTo
     * @return
     */
     Optional<ByteArrayOutputStream> zipExport(String numberCodeDay, String lot, String dateFrom, String dateTo) throws Exception;


    /**
     * STEP - 1 [ PERSISTING ]
     * @param codePerDays code per days to be generated
     * @param lotIdentifier lot identifier that the series should take
     * @param from start date of the series of days code generation
     * @param to end date of the series of days code generation
     * @throws NumberOfTryGenerateCodeExceededExcetion
     */
     void persistUUIDv4CodesFor(String codePerDays, String lotIdentifier, OffsetDateTime from, OffsetDateTime to) throws NumberOfTryGenerateCodeExceededExcetion;

    /**
     * STEP 2 - [ PARSING DATA TO CSV files ]
     * @param submissionCodeDtos
     * @param dates
     * @return List of csv files named as AAAA-MM-DD.csv
     */
     List<File> codeAsCsvFiles(List<SubmissionCodeDto> submissionCodeDtos, List<@NotNull OffsetDateTime> dates) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException;


    /**
     * STEP 3 - [ Packaging files in a Zip ]
     * @param files csv files to be zipped.
     * @return ZipOutputStream instance containing csv files.
     */
     ByteArrayOutputStream packagingCsvFilesToZipFile(List<File> files) throws IOException;

    }
