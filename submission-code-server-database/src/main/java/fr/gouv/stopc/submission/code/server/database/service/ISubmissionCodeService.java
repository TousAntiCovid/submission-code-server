package fr.gouv.stopc.submission.code.server.database.service;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;

import java.util.List;
import java.util.Optional;

public interface ISubmissionCodeService {
      Optional<SubmissionCodeDto> getCodeValidity(String code, String type);

      Iterable<SubmissionCode> saveAllCodes(List<SubmissionCodeDto> submissionCodeDtos);

      Optional<SubmissionCode> saveCode(SubmissionCodeDto submissionCodeDto);

      boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto);

      long lastLot();
      /**
       * Method calls lastLot and add 1 to give the next one available.
       * @return next lot.
       */
      long nextLot();

      List<SubmissionCodeDto> getCodeUUIDv4CodesForCsv(String lot, String type);

}