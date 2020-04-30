package fr.gouv.stopc.submission.code.server.database.service;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;

import java.util.List;
import java.util.Optional;

public interface ISubmissionCodeService {
      Optional<SubmissionCodeDto> getCodeValidity(String code, String type);

      boolean saveAllCodeGenerateByBatch(List<SubmissionCodeDto> submissionCodeDtos);

      boolean saveCodeGenerate(SubmissionCodeDto submissionCodeDto);

      boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto);

}