package fr.gouv.stopc.submission.code.server.business.service;

import fr.gouv.stopc.submission.code.server.business.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.data.entity.SubmissionCode;
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Valid
public class SubmissionCodeService {

    private SubmissionCodeRepository submissionCodeRepository;

    @Value("${generation.code.security.shortcode.hours}")
    private Integer securityTimeBetweenTwoUsagesOfShortCode;

    @Inject
    public SubmissionCodeService(SubmissionCodeRepository submissionCodeRepository) {
        this.submissionCodeRepository = submissionCodeRepository;
    }

    public Optional<SubmissionCodeDto> getCodeValidity(String code) {
        if (Strings.isBlank(code)) {
            return Optional.empty();
        }
        SubmissionCode submissionCode = submissionCodeRepository.findByCode(code);
        if (Objects.isNull(submissionCode)) {
            return Optional.empty();
        }
        ModelMapper modelMapper = new ModelMapper();
        SubmissionCodeDto submissionCodeDto = modelMapper.map(submissionCode, SubmissionCodeDto.class);
        return Optional.of(submissionCodeDto);
    }

    public Iterable<SubmissionCode> saveAllCodes(List<SubmissionCodeDto> submissionCodeDtos) {
        return this.saveAllCodes(submissionCodeDtos, new Lot());
    }

    public Iterable<SubmissionCode> saveAllCodes(List<SubmissionCodeDto> submissionCodeDtos, Lot lot) {
        if (submissionCodeDtos.isEmpty()) {
            return Collections.emptyList();
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

    public Optional<SubmissionCode> saveCode(SubmissionCodeDto submissionCodeDto) {
        if (Objects.isNull(submissionCodeDto)) {
            return Optional.empty();
        }
        ModelMapper modelMapper = new ModelMapper();
        SubmissionCode submissionCode = modelMapper.map(submissionCodeDto, SubmissionCode.class);
        return Optional.of(submissionCodeRepository.save(submissionCode));
    }

    public Optional<SubmissionCode> saveCode(SubmissionCodeDto submissionCodeDto, Lot lot) {
        if (Objects.isNull(submissionCodeDto)) {
            return Optional.empty();
        }
        ModelMapper modelMapper = new ModelMapper();

        SubmissionCode submissionCodeToSave = modelMapper.map(submissionCodeDto, SubmissionCode.class);
        submissionCodeToSave.setLotkey(lot);

        try {
            // try to save data
            return Optional.of(submissionCodeRepository.save(submissionCodeToSave));

        } catch (DataIntegrityViolationException divExcetion) {

            // if Unique code exists for short code try to update
            if (securityTimeBetweenTwoUsagesOfShortCode != null
                    && CodeTypeEnum.SHORT.isTypeOrTypeCodeOf(submissionCodeToSave.getType())) {
                SubmissionCode sc = this.submissionCodeRepository.findByCodeAndTypeAndAndDateEndValidityLessThan(
                        submissionCodeToSave.getCode(),
                        submissionCodeToSave.getType(),
                        submissionCodeToSave.getDateAvailable()
                                .minusHours(
                                        securityTimeBetweenTwoUsagesOfShortCode
                                )
                );
                if (sc != null) {
                    // replace actual line by new code
                    submissionCodeToSave.setId(sc.getId());
                    return Optional.of(this.submissionCodeRepository.save(submissionCodeToSave));
                }
            }
            // if update is not made throw the original exception
            throw divExcetion;
        }

    }

    public boolean updateCodeUsed(SubmissionCodeDto submissionCodeDto) {
        String code = submissionCodeDto.getCode();
        SubmissionCode submissionCode = submissionCodeRepository.findByCode(code);
        if (Objects.isNull(submissionCode)) {
            return false;
        }
        submissionCode.setDateUse(submissionCodeDto.getDateUse());
        submissionCode.setUsed(true);
        submissionCodeRepository.save(submissionCode);
        return true;
    }

    /**
     * Return number of code for the given lot identifier.
     *
     * @param lotIdentifier lot identifier in db
     * @return return number of code with the given lot identifier
     */
    public long getNumberOfCodesForLotIdentifier(long lotIdentifier) {
        return this.submissionCodeRepository.countSubmissionCodeByLotkeyId(lotIdentifier);
    }

    /**
     * Get specific range of code rows
     *
     * @param lotIdentifier  lot identifier the codes should be matched
     * @param page           page number
     * @param elementsByPage the row page the list ends.
     * @return list of code page row "page" elementsByPage rows "elementsByPage"
     *         e.g. : page = 10 and elementsByPage = 12 , size list is 3 and has
     *         only row 10, 11, 12
     */
    public Page<SubmissionCode> getSubmissionCodesFor(long lotIdentifier, int page, int elementsByPage) {
        return this.submissionCodeRepository
                .findAllByLotkeyId(lotIdentifier, PageRequest.of(page, elementsByPage));
    }

    public void removeByLot(Lot lot) {
        this.submissionCodeRepository.deleteAllByLotkey(lot);
    }

}
