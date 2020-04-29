package fr.gouv.stopc.submission.code.server.database.Service.impl;

import fr.gouv.stopc.submission.code.server.database.Entity.CodePositive;
import fr.gouv.stopc.submission.code.server.database.Repository.CodePositiveRepository;
import fr.gouv.stopc.submission.code.server.database.Service.ICodePositiveService;
import fr.gouv.stopc.submission.code.server.database.dto.CodePositiveDto;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Service
public class CodePositiveServiceImpl implements ICodePositiveService {

    private CodePositiveRepository codePositiveRepository;

    @Inject
    public CodePositiveServiceImpl(CodePositiveRepository codePositiveRepository){
        this.codePositiveRepository=codePositiveRepository;
    }

    @Override
    public Optional<CodePositive> getCodeValidity(String code) {
        CodePositive result = codePositiveRepository.findByCode(code);
        return Optional.of(result);
    }

    @Override
    public boolean saveAllCodeGenerateByBatch(List<CodePositiveDto> codePositiveDtos) {
        return false;
    }
}
