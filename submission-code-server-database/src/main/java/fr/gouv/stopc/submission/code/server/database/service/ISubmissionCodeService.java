package fr.gouv.stopc.submission.code.server.database.service;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;

import java.util.List;
import java.util.Optional;

public interface ISubmissionCodeService {
      Optional<SubmissionCodeDto> getCodeValidity(String code, String type);

      Iterable<SubmissionCode> saveAllCodeGenerateByBatch(List<SubmissionCodeDto> submissionCodeDtos);

      SubmissionCode saveCodeGenerate(SubmissionCodeDto submissionCodeDto);

      boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto);

}