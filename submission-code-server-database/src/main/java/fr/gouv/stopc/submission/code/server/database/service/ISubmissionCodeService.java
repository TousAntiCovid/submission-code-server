package fr.gouv.stopc.submission.code.server.database.service;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ISubmissionCodeService {
      Optional<SubmissionCodeDto> getCodeValidity(String code, String type);

      Iterable<SubmissionCode> saveAllCodes(List<SubmissionCodeDto> submissionCodeDtos);

      SubmissionCode saveCode(SubmissionCodeDto submissionCodeDto);

      boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto);

      long lastLot();
      /**
       * Method calls lastLot and add 1 to give the next one available.
       * @return next lot.
       */
      long nextLot();

      /**
       * Return number of code for the given lot identifier.
        * @param lotIdentifier lot identifier in db
       * @return return number of code with the given lot identifier
       */
    long getNumberOfCodesForLotIdentifier(long lotIdentifier);

      /**
       * Get specific range of code rows
       * @param lotIdentifier lot identifier the codes should be matched
       * @param page page number
       * @param elementsByPage the row page the list ends.
       * @return list of code page row "page" elementsByPage rows "elementsByPage" e.g. : page = 10 and elementsByPage = 12 , size list is 3 and has only row 10, 11, 12
       */
      Page<SubmissionCode> getSubmissionCodesFor(long lotIdentifier, int page, int elementsByPage);
      List<SubmissionCodeDto> getCodeUUIDv4CodesForCsv(String lot, String type);
}