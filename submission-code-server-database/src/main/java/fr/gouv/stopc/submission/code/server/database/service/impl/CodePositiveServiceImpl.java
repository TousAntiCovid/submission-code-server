package fr.gouv.stopc.submission.code.server.database.service.impl;

import fr.gouv.stopc.submission.code.server.database.entity.CodePositive;
import fr.gouv.stopc.submission.code.server.database.repository.CodePositiveRepository;
import fr.gouv.stopc.submission.code.server.database.service.ICodePositiveService;
import fr.gouv.stopc.submission.code.server.database.dto.CodePositiveDto;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Valid
public class CodePositiveServiceImpl implements ICodePositiveService {

    private CodePositiveRepository codePositiveRepository;

    @Inject
    public CodePositiveServiceImpl(CodePositiveRepository codePositiveRepository){
        this.codePositiveRepository=codePositiveRepository;
    }

    @Override
    public Optional<CodePositiveDto> getCodeValidity(String code, String type) {
        if(Strings.isBlank(code)){
            return Optional.empty();
        }
        CodePositive codePositive = codePositiveRepository.findByCodeAndType(code,type);
        if(Objects.isNull(codePositive)){
            return Optional.empty();
        }
        ModelMapper modelMapper = new ModelMapper();
        CodePositiveDto codePositiveDto = modelMapper.map(codePositive,CodePositiveDto.class);
        return Optional.of(codePositiveDto);
    }

    @Override
    public boolean saveAllCodeGenerateByBatch(List<CodePositiveDto> codePositiveDtos) {
        if(codePositiveDtos.isEmpty()) {
            return false;
        }
        ModelMapper modelMapper = new ModelMapper();
        List<CodePositive> codePositives = codePositiveDtos.stream().map(tmp-> modelMapper.map(tmp,CodePositive.class)).collect(Collectors.toList());
        codePositiveRepository.saveAll(codePositives);
        return true;
    }

    @Override
    public boolean saveCodeGenerate(CodePositiveDto codePositiveDto) {
        if (Objects.isNull(codePositiveDto)) {
            return false;
        }
        ModelMapper modelMapper = new ModelMapper();
        CodePositive codePositive = modelMapper.map(codePositiveDto, CodePositive.class);
        codePositiveRepository.save(codePositive);
        return true;
    }

    @Override
    public boolean updateCodeUsed(CodePositiveDto codePositiveDto) {
        String code = codePositiveDto.getCode();
        String type = codePositiveDto.getType();
        CodePositive codepositive = codePositiveRepository.findByCodeAndType(code, type);
        if (Objects.isNull(codepositive)){
            return false;
        }
        codepositive.setDateUse(codePositiveDto.getDateUse());
        codepositive.setUsed(true);
        codePositiveRepository.save(codepositive);
        return true;
    }

}
