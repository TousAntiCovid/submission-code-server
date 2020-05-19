package fr.gouv.stopc.submission.code.server.commun.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CodeTypeEnum {

    UUIDv4("1", "UUIDv4", Pattern.UUIDV4),
    ALPHANUM_6 ("2", "6-alphanum", Pattern.ALPHANUM_6);


    /**
     * type code is an numeric in string (ex. "1")
     */
    private final String typeCode;

    /**
     * type is the name of the code type. (ex. "UUIDv4")
     */
    private final String type;

    /**
     * pattern code matching regexp
     */
    private final String pattern;

    /**
     * Default and only constructor
     * @param typeCode {@link #typeCode}
     * @param type {@link #type}
     */
    CodeTypeEnum(final String typeCode, final String type, final String pattern) {
        this.typeCode = typeCode;
        this.type = type;
        this.pattern = pattern;
    }


    /**
     * Method equals get a string (ex. "1" or "UUIDv4") to know if the enum is corresponding to the value.
     * @param typeOrTypeCode (ex. "1" or "UUIDv4") value to test if the enum is corresponding to "typeOrTypeCode"
     * @return if the enum is corresponding to the parameter in method returned value is "true" otherwise returned value is "false"
     */
    public final Boolean isTypeOf(String typeOrTypeCode) {
        return this.type.equals(typeOrTypeCode) || this.typeCode.equals(typeOrTypeCode) ;
    }

    /**
     * Static method exists get a string (ex. "1" or "UUIDv4") to know if an enum is corresponding to the value.
     * It uses the methode {@link #isTypeOf(String)} to check the value.
     * @param typeOrTypeCode (ex. "1" or "UUIDv4") value to test if the enum is corresponding to "typeOrTypeCode"
     * @return if an enum is corresponding to the parameter in method returned value is "true" otherwise returned value is "false"
     */
    public static final Boolean exists(final String typeOrTypeCode) {
        for (CodeTypeEnum et :  Arrays.asList(values())) {
            if(et.isTypeOf(typeOrTypeCode)) return true;
        }
        return false;
    }

    public interface Pattern {
        String ALPHANUM_6 = "([a-zA-Z0-9]{6})";
        String UUIDV4 = "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})";
    }
}