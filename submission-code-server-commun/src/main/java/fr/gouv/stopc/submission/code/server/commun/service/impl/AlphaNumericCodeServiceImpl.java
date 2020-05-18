package fr.gouv.stopc.submission.code.server.commun.service.impl;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlphaNumericCodeServiceImpl implements IAlphaNumericCodeService {

    private static final String ALPHA_UPPER_CASE = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
    private static final String NUMERIC= "0123456789";
    private static final Integer CODE_SIZE = 6;

    private static final SecureRandom sRandom = new SecureRandom();

    private static final List<Character> ALPHA_NUMERIC_CHAR_ARRAY = String
            .format(
                    "%s%s",
                    ALPHA_UPPER_CASE,
                    NUMERIC)
            .chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());

    public String generateCode() {
        log.info("Generating random 6-alphanum code");
        final List<Character> characters = getShuffledAlphaNumList();

        String alphaNum = "";
        for (int i = 0; i < CODE_SIZE; i++) {
            alphaNum += characters.get(sRandom.nextInt(ALPHA_NUMERIC_CHAR_ARRAY.size()-1)).toString();
        }
        return alphaNum;
    }

    /**
     * @return return a shuffled copy of ALPHA_NUMERIC_CHAR_ARRAY
     */
    protected static List<Character> getShuffledAlphaNumList() {
        final ArrayList<Character> tempAlphaNumList = new ArrayList<>(ALPHA_NUMERIC_CHAR_ARRAY);
        Collections.shuffle(ALPHA_NUMERIC_CHAR_ARRAY,sRandom);
        return tempAlphaNumList;
    }

}
