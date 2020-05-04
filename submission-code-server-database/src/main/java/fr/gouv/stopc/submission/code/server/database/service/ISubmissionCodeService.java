package fr.gouv.stopc.submission.code.server.database.service;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;

import java.util.List;
import java.util.Optional;

public interface ISubmissionCodeService {
      Optional<SubmissionCodeDto> getCodeValidity(String code, String type);

      Iterable<SubmissionCode> saveAllCodes(List<SubmissionCodeDto> submissionCodeDtos);

      SubmissionCode saveCode(SubmissionCodeDto submissionCodeDto);

      boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto);

      long lastLot();

      /**
       * get all the codes that have been generated previously
       * @return list of codes not uses in bd
       */
      List<SubmissionCode> getAvailableUUIDv4Codes();

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
}