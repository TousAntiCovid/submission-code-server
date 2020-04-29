package fr.gouv.stopc.submission.code.server.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GenerateResponseDto {


    private TypeKey typeAsString;

    private TypeKey typeAsInt;

    // should be 6 long or x long
    private String code;

    private String validFrom;

    private String validUntil;



    public enum TypeKey {
        UUIDv4("UUIDv4", 1),
        ALPHANUM6("6-alphanum", 2);

        private final String name;
        private final Integer code;

        TypeKey(String name, Integer code) {
            this.name = name;
            this.code = code;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Integer toInteger() {
            return this.code;
        }
    }


}
