package fr.gouv.stopc.submission.code.server.database.Service;

import fr.gouv.stopc.submission.code.server.database.Entity.CodePositive;
import fr.gouv.stopc.submission.code.server.database.dto.CodePositiveDto;

import java.util.List;
import java.util.Optional;

public interface ICodePositiveService {
      Optional<CodePositive> getCodeValidity(String code);
      boolean saveAllCodeGenerateByBatch(List<CodePositiveDto> codePositiveDtos);
      boolean saveCodeGenerate(CodePositiveDto codePositiveDto);
}
