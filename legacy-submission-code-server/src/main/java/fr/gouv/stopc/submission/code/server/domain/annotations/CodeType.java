package fr.gouv.stopc.submission.code.server.domain.annotations;

import fr.gouv.stopc.submission.code.server.domain.validators.CodeTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { CodeTypeValidator.class })
/**
 * @see CodeTypeValidator description
 */
public @interface CodeType {

    String message() default "{value} is not an instance of TypeEnum";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
