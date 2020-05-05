package fr.gouv.stopc.submission.code.server.database.service.impl;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public Iterable<SubmissionCode> saveAllCodes(List<SubmissionCodeDto> submissionCodeDtos) {
        if(submissionCodeDtos.isEmpty()) {
            return null;
        }
        ModelMapper modelMapper = new ModelMapper();
        List<SubmissionCode> submissionCodes = submissionCodeDtos.stream().map(tmp-> modelMapper.map(tmp, SubmissionCode.class)).collect(Collectors.toList());
       return submissionCodeRepository.saveAll(submissionCodes);
    }

    @Override
    public SubmissionCode saveCode(SubmissionCodeDto submissionCodeDto) {
        if (Objects.isNull(submissionCodeDto)) {
            return null;
        }
        ModelMapper modelMapper = new ModelMapper();
        SubmissionCode submissionCode = modelMapper.map(submissionCodeDto, SubmissionCode.class);
        return submissionCodeRepository.save(submissionCode);
    }

    @Override
    public boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto) {
        String code = submissionCodeDto.getCode();
        String type = submissionCodeDto.getType();
        SubmissionCode submissionCode = submissionCodeRepository.findByCodeAndType(code, type);
        if (Objects.isNull(submissionCode)){
            return false;
        }
        submissionCode.setDateUse(submissionCodeDto.getDateUse());
        submissionCode.setUsed(true);
        submissionCodeRepository.save(submissionCode);
        return true;
    }

    @Override
    public long lastLot() {
        String lot = submissionCodeRepository.lastLot();
         if(Objects.isNull(lot)) {
            return 0;
        }
         return Long.valueOf(lot);
    }

    @Override
    public long nextLot() {
        return this.lastLot() +1;
    }

    @Override
    public List<SubmissionCodeDto> getCodeUUIDv4CodesForCsv(String lot, String type) {
        List<SubmissionCode> submissionCodes = submissionCodeRepository.findAllByLotAndTypeEquals(Long.parseLong(lot), type);
        if (submissionCodes.isEmpty()){
            return null;
        }
        ModelMapper modelMapper = new ModelMapper();
        return submissionCodes.stream().map(tmp->modelMapper.map(tmp, SubmissionCodeDto.class)).collect(Collectors.toList());
    }

    @Override
    public long getNumberOfCodesForLotIdentifier(long lotIdentifier) {
        return this.submissionCodeRepository.countSubmissionCodeByLot(lotIdentifier);
    }

    @Override
    public Page<SubmissionCode> getSubmissionCodesFor(long lotIdentifier, int page, int elementsByPage) {
        return this.submissionCodeRepository
                .findAllByLot(lotIdentifier, PageRequest.of(page, elementsByPage));


    }
}
