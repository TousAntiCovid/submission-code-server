package fr.gouv.stopc.submission.code.server.domain.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum CodeTypeEnum {

    LONG("1", "UUIDv4", Pattern.LONG, 36),
    SHORT("2", "6-alphanum", Pattern.SHORT, 6),
    TEST("3", "12-aphanum", Pattern.TEST, 12);

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
     * length int
     */
    private final int length;

    /**
     * Default and only constructor
     * 
     * @param typeCode {@link #typeCode}
     * @param type     {@link #type}
     */
    CodeTypeEnum(final String typeCode, final String type, final String pattern, final int length) {
        this.typeCode = typeCode;
        this.type = type;
        this.pattern = pattern;
        this.length = length;
    }

    public static Optional<CodeTypeEnum> searchMatchType(String type) {
        for (CodeTypeEnum et : Arrays.asList(values())) {
            if (et.isTypeOrTypeCodeOf(type))
                return Optional.of(et);
        }
        return Optional.empty();
    }

    /**
     * Method equals get a string (ex. "1" or "UUIDv4") to know if the enum is
     * corresponding to the value.
     * 
     * @param typeOrTypeCode (ex. "1" or "UUIDv4") value to test if the enum is
     *                       corresponding to "typeOrTypeCode"
     * @return if the enum is corresponding to the parameter in method returned
     *         value is "true" otherwise returned value is "false"
     */
    public final Boolean isTypeOrTypeCodeOf(String typeOrTypeCode) {
        return this.type.equals(typeOrTypeCode) || this.typeCode.equals(typeOrTypeCode);
    }

    public static Optional<CodeTypeEnum> searchMatchLength(int length) {
        for (CodeTypeEnum et : Arrays.asList(values())) {
            if (et.isOfLength(length)) {
                return Optional.of(et);
            }
        }
        return Optional.empty();
    }

    public final Boolean matchPattern(String code) {
        return code.matches(this.pattern);
    }

    public final Boolean isOfLength(int length) {
        return (this.length == length);
    }

    /**
     * Static method exists get a string (ex. "1" or "UUIDv4") to know if an enum is
     * corresponding to the value. It uses the methode
     * {@link #isTypeOrTypeCodeOf(String)} to check the value.
     * 
     * @param typeOrTypeCode (ex. "1" or "UUIDv4") value to test if the enum is
     *                       corresponding to "typeOrTypeCode"
     * @return if an enum is corresponding to the parameter in method returned value
     *         is "true" otherwise returned value is "false"
     */
    public static final Boolean exists(final String typeOrTypeCode) {
        Optional<CodeTypeEnum> matchType = searchMatchType(typeOrTypeCode);
        return matchType.isPresent();
    }

    public interface Pattern {

        String SHORT = "([a-zA-Z0-9]{6})";

        String LONG = "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})";

        String TEST = "([a-zA-Z0-9]{12})";
    }

}
