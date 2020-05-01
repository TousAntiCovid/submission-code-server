package fr.gouv.stopc.submission.code.server.database.service.impl;

import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Valid
public class SubmissionCodeServiceImpl implements ISubmissionCodeService {

    private SubmissionCodeRepository submissionCodeRepository;

    @Inject
    public SubmissionCodeServiceImpl(SubmissionCodeRepository submissionCodeRepository){
        this.submissionCodeRepository = submissionCodeRepository;
    }

    @Override
    public Optional<SubmissionCodeDto> getCodeValidity(String code, String type) {
        if(Strings.isBlank(code)){
            return Optional.empty();
        }
        SubmissionCode submissionCode = submissionCodeRepository.findByCodeAndType(code,type);
        if(Objects.isNull(submissionCode)){
            return Optional.empty();
        }
        ModelMapper modelMapper = new ModelMapper();
        SubmissionCodeDto submissionCodeDto = modelMapper.map(submissionCode, SubmissionCodeDto.class);
        return Optional.of(submissionCodeDto);
    }

    @Override
    public boolean saveAllCodeGenerateByBatch(List<SubmissionCodeDto> submissionCodeDtos) {
        if(submissionCodeDtos.isEmpty()) {
            return false;
        }
        ModelMapper modelMapper = new ModelMapper();
        List<SubmissionCode> submissionCodes = submissionCodeDtos.stream().map(tmp-> modelMapper.map(tmp, SubmissionCode.class)).collect(Collectors.toList());
        submissionCodeRepository.saveAll(submissionCodes);
        return true;
    }

    @Override
    public boolean saveCodeGenerate(SubmissionCodeDto submissionCodeDto) {
        if (Objects.isNull(submissionCodeDto)) {
            return false;
        }
        ModelMapper modelMapper = new ModelMapper();
        SubmissionCode submissionCode = modelMapper.map(submissionCodeDto, SubmissionCode.class);
        submissionCodeRepository.save(submissionCode);
        return true;
    }

    @Override
    public boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto) {
        String code = submissionCodeDto.getCode();
        String type = submissionCodeDto.getType();
        SubmissionCode codepositive = submissionCodeRepository.findByCodeAndType(code, type);
        if (Objects.isNull(codepositive)){
            return false;
        }
        codepositive.setDateUse(submissionCodeDto.getDateUse());
        codepositive.setUsed(true);
        submissionCodeRepository.save(codepositive);
        return true;
    }

}
