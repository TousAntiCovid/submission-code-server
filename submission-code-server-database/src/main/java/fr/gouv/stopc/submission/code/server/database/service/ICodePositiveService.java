package fr.gouv.stopc.submission.code.server.database.service;

import fr.gouv.stopc.submission.code.server.database.entity.CodePositive;
import fr.gouv.stopc.submission.code.server.database.dto.CodePositiveDto;

import java.util.List;
import java.util.Optional;

public interface ICodePositiveService {
      Optional<CodePositiveDto> getCodeValidity(String code,String type);
      boolean saveAllCodeGenerateByBatch(List<CodePositiveDto> codePositiveDtos);
      boolean saveCodeGenerate(CodePositiveDto codePositiveDto);
      boolean updateCodeUsed(CodePositiveDto codePositiveDto);
}
