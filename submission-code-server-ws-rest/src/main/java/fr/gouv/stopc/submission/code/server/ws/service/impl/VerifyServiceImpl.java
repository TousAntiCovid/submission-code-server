package fr.gouv.stopc.submission.code.server.ws.service.impl;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.IVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class VerifyServiceImpl implements IVerifyService {

    private ISubmissionCodeService submissionCodeService;

    /**
     *  Default constructor spring-injecting the needed services.
     * @param submissionCodeService service from the database module permitting to interface with the data base.
     */
    @Inject
    public VerifyServiceImpl (ISubmissionCodeService submissionCodeService){
        this.submissionCodeService = submissionCodeService;
    }

    @Override
    public boolean verifyCode(String code, String type) throws SubmissionCodeServerException {
        log.info("Verifying code method");

            log.info("Searching code from database");

            Optional<SubmissionCodeDto> codeDtoOptional = submissionCodeService.getCodeValidity(code, type);


            if (!codeDtoOptional.isPresent()) {
                log.warn("No codes were found");
                return false;
            }

            log.info("Code was found");

            SubmissionCodeDto codeDto = codeDtoOptional.get();

            /*
             *  we don't use the code already used.
             */
            if (codeDto.getUsed().equals(Boolean.TRUE) || Objects.nonNull(codeDto.getDateUse())){
                log.warn("The code has already been used.");
                return false;
            }

            ZoneOffset zoneOffeset = codeDto.getDateAvailable().getOffset();
            OffsetDateTime dateNow = LocalDateTime.now().atOffset(zoneOffeset);

            if(validateDate(dateNow,codeDto.getDateAvailable(),codeDto.getDateEndValidity())){
                log.warn("The code validity is out of the requested date.");
                return false;
            }

            log.info("The code is about to be updated.");

            codeDto.setUsed(true);
            codeDto.setDateUse(dateNow);
            final boolean isUpdated = submissionCodeService.updateCodeUsed(codeDto);

            if(isUpdated) {
                log.info("The code has been updated.");
            } else {
                log.error("Tried to update the code but an error occurred...");
            }
            return isUpdated;
    }

    /**A code cannot be used before he is valid or after it has expired.*/
    private boolean validateDate(OffsetDateTime dateNow, OffsetDateTime dateAvailable, OffsetDateTime dateEndValidity) {
        log.info("Check the validity of 'from' and 'to' dates.");
        if(Objects.isNull(dateAvailable) || Objects.isNull(dateEndValidity)){
            return true;
        }
        return (dateAvailable.isAfter(dateNow) || dateNow.isAfter(dateEndValidity));
    }
}
