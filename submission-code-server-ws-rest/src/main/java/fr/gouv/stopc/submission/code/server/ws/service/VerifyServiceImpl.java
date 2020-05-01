package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.database.dto.SubmissionCodeDto;
import fr.gouv.stopc.submission.code.server.database.service.ISubmissionCodeService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class VerifyServiceImpl implements IVerifyService {

    private ISubmissionCodeService submissionCodeService;

    @Inject
    public VerifyServiceImpl (ISubmissionCodeService submissionCodeService){
        this.submissionCodeService = submissionCodeService;
    }

    @Override
    public boolean verifyCode(String code, String type) {
        Optional<SubmissionCodeDto> codeDtoOptional = submissionCodeService.getCodeValidity(code, type);
        if (!codeDtoOptional.isPresent()){
            return false;
        }
        SubmissionCodeDto codeDto = codeDtoOptional.get();
        /*
            we don't use the code already used.
         */
        if (codeDto.getUsed().equals(Boolean.TRUE) || codeDto.getDateUse()== null){
            return false;
        }

        ZoneOffset zoneOffeset = codeDto.getDateAvailable().getOffset();
        OffsetDateTime dateNow = LocalDateTime.now().atOffset(zoneOffeset);

        if(!validationDate(dateNow,codeDto.getDateAvailable(),codeDto.getDateEndValidity())){
            return false;
        }

        codeDto.setUsed(true);
        codeDto.setDateUse(dateNow);
        return submissionCodeService.updateCodeUsed(codeDto);
    }

    private boolean validationDate(OffsetDateTime dateNow, OffsetDateTime dateAvailable, OffsetDateTime dateEndValidity) {
        if(dateAvailable.isAfter(dateNow)){
            /*
              We don't use the code before being available.
             */
            return false;
        }
            /*
             We don't use the code expired.
             */
        return dateNow.isAfter(dateEndValidity);
    }
}
