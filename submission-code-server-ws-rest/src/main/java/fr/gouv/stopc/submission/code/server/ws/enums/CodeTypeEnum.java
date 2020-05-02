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


    private final String typeCode;
    private final String type;

    CodeTypeEnum(String typeCode, String type) {
        this.typeCode = typeCode;
        this.type = type;
    }


    public final Boolean equals(String typeOrTypeCode) {
        return this.type.equals(typeOrTypeCode) || this.typeCode.equals(typeOrTypeCode) ;
    }

    public static final Boolean exists(final String typeOrTypeCode) {
        for (CodeTypeEnum et :  Arrays.asList(values())) {
            if(et.equals(typeOrTypeCode)) return true;
        }
        return false;
    }
}