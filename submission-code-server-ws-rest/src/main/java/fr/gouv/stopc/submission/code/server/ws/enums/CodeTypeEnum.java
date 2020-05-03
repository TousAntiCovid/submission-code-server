package fr.gouv.stopc.submission.code.server.ws.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author cllange
 */
@Getter
public enum CodeTypeEnum {

    UUIDv4("1", "UUIDv4"),
    ALPHANUM_6 ("2", "6-alphanum");


    /**
     * type code is an numeric in string (ex. "1")
     */
    private final String typeCode;

    /**
     * type is the name of the code type. (ex. "UUIDv4")
     */
    private final String type;

    /**
     * Default and only constructor
     * @param typeCode {@link #typeCode}
     * @param type {@link #type}
     */
    CodeTypeEnum(String typeCode, String type) {
        this.typeCode = typeCode;
        this.type = type;
    }


    /**
     * Method equals get a string (ex. "1" or "UUIDv4") to know if the enum is corresponding to the value.
     * @param typeOrTypeCode (ex. "1" or "UUIDv4") value to test if the enum is corresponding to "typeOrTypeCode"
     * @return if the enum is corresponding to the parameter in method returned value is "true" otherwise returned value is "false"
     */
    public final Boolean equals(String typeOrTypeCode) {
        return this.type.equals(typeOrTypeCode) || this.typeCode.equals(typeOrTypeCode) ;
    }

    /**
     * Static method exists get a string (ex. "1" or "UUIDv4") to know if an enum is corresponding to the value.
     * It uses the methode {@link #equals(String)} to check the value.
     * @param typeOrTypeCode (ex. "1" or "UUIDv4") value to test if the enum is corresponding to "typeOrTypeCode"
     * @return if an enum is corresponding to the parameter in method returned value is "true" otherwise returned value is "false"
     */
    public static final Boolean exists(final String typeOrTypeCode) {
        for (CodeTypeEnum et :  Arrays.asList(values())) {
            if(et.equals(typeOrTypeCode)) return true;
        }
        return false;
    }
}