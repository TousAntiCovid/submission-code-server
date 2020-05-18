package fr.gouv.stopc.submission.code.server.ws.service.generateservice;

import fr.gouv.stopc.submission.code.server.commun.enums.CodeTypeEnum;
import fr.gouv.stopc.submission.code.server.ws.dto.CodeDetailedDto;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateServiceTestHelper {


    /**
     * asserting code ALPHANUM6
     * {@link #assertingCode(CodeDetailedDto, CodeTypeEnum, String)}
     */
    static void assertingALPHANUM6Code(CodeDetailedDto gr)
    {
        assertingCode(gr, CodeTypeEnum.ALPHANUM_6,"([A-Z0-9]{6})");
    }

    /**
     * asserting code UUID
     * {@link #assertingCode(CodeDetailedDto, CodeTypeEnum, String)}
     */
    static void assertingUUIDv4Code(CodeDetailedDto gr)
    {
        assertingCode(gr, CodeTypeEnum.UUIDv4,"([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})" );
    }

    /**
     * asserting code
     */
    static void assertingCode(CodeDetailedDto gr, CodeTypeEnum cte, String pattern)
    {
        assertNotNull(gr);

        // asserting it is CodeTypeEnum code
        cte.getTypeCode().equals(gr.getTypeAsInt().toString());
        cte.getType().equals(gr.getTypeAsString());

        // asserting code matches CodeTypeEnum pattern
        Pattern p = Pattern.compile(pattern);
        assertTrue(p.matcher(gr.getCode()).matches());
    }

}
