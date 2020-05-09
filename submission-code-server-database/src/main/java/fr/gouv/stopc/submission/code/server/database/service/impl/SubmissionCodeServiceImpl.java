package fr.gouv.stopc.submission.code.server.database.service.impl;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.database.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Collections;
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
        return this.saveAllCodes(submissionCodeDtos, new Lot());
    }

    @Override
    public Iterable<SubmissionCode> saveAllCodes(List<SubmissionCodeDto> submissionCodeDtos, Lot lot) {
        if(submissionCodeDtos.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ModelMapper modelMapper = new ModelMapper();
        List<SubmissionCode> submissionCodes = submissionCodeDtos.stream()
                .map(tmp -> {
                    final SubmissionCode sc = modelMapper.map(tmp, SubmissionCode.class);
                    sc.setLotkey(lot);
                    return sc;
                })
                .collect(Collectors.toList());
       return submissionCodeRepository.saveAll(submissionCodes);
    }

    @Override
    public Optional<SubmissionCode> saveCode(SubmissionCodeDto submissionCodeDto) {
        if (Objects.isNull(submissionCodeDto)) {
            return Optional.empty();
        }
        ModelMapper modelMapper = new ModelMapper();
        SubmissionCode submissionCode = modelMapper.map(submissionCodeDto, SubmissionCode.class);
        return Optional.of(submissionCodeRepository.save(submissionCode));
    }

    @Override
    public Optional<SubmissionCode> saveCode(SubmissionCodeDto submissionCodeDto, Lot lot) {
        if (Objects.isNull(submissionCodeDto)) {
            return Optional.empty();
        }
        ModelMapper modelMapper = new ModelMapper();

        SubmissionCode submissionCode = modelMapper.map(submissionCodeDto, SubmissionCode.class);
        submissionCode.setLotkey(lot);

        return Optional.of(submissionCodeRepository.save(submissionCode));
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
    public List<SubmissionCodeDto> getCodeUUIDv4CodesForCsv(String lot, String type) {
        List<SubmissionCode> submissionCodes = submissionCodeRepository.findAllByLotkey_IdAndTypeEquals(Long.parseLong(lot), type);
        if (CollectionUtils.isEmpty(submissionCodes)){
            return Collections.emptyList();
        }
        ModelMapper modelMapper = new ModelMapper();
        return submissionCodes.stream().map(tmp->modelMapper.map(tmp, SubmissionCodeDto.class)).collect(Collectors.toList());
    }

    @Override
    public long getNumberOfCodesForLotIdentifier(long lotIdentifier) {
        return this.submissionCodeRepository.countSubmissionCodeByLotkey_Id(lotIdentifier);
    }

    @Override
    public Page<SubmissionCode> getSubmissionCodesFor(long lotIdentifier, int page, int elementsByPage) {
        return this.submissionCodeRepository
                .findAllByLotkey_Id(lotIdentifier, PageRequest.of(page, elementsByPage));


    }



}
