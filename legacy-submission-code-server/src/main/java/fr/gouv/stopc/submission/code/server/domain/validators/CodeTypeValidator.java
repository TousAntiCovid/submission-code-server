package fr.gouv.stopc.submission.code.server.domain.validators;

import fr.gouv.stopc.submission.code.server.domain.annotations.CodeType;
import fr.gouv.stopc.submission.code.server.domain.enums.CodeTypeEnum;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator of fields annotated with CodeType.
 */
public class CodeTypeValidator implements ConstraintValidator<CodeType, String> {

    /**
     * Valid if string corresponding to a instance of enum CodeTypeEnum by running
     * method CodeTypeEnum.exists
     * 
     * @param codeTypeToTest code type to test
     * @return return true if code type to test corresponding to a CodeTypeEnum.
     */
    @Override
    public boolean isValid(String codeTypeToTest, ConstraintValidatorContext constraintValidatorContext) {
        return CodeTypeEnum.exists(codeTypeToTest);
    }
}
