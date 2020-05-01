package fr.gouv.stopc.submission.code.server.ws.vo;

import fr.gouv.stopc.submission.code.server.ws.annotations.CodeType;
import fr.gouv.stopc.submission.code.server.ws.dto.GenerateResponseDto;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author cllange
 */
@ExtendWith(SpringExtension.class)
class TypeCodeValidatorTest {


    

    @Test
    void typeCodeValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<GenerateRequestVo>> violations;

        violations = validator.validate(GenerateRequestVo.builder().type("1456").build());
        assertFalse(violations.isEmpty());

        violations = validator.validate(GenerateRequestVo.builder().type("1").build());
        assertTrue(violations.isEmpty());

    }
}