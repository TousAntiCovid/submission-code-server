package fr.gouv.stopc.submission.code.server.commun.service.impl;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AlphaNumericCodeServiceImpl implements IAlphaNumericCodeService {

    private static final String ALPHA_LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC= "0123456789";
    private static final Integer CODE_SIZE = 6;

    private static final List<Character> ALPHA_NUMERIC_CHAR_ARRAY = String
            .format(
                    "%s%s",
                    ALPHA_LOWER_CASE,
                    NUMERIC)
            .chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());

    public String generateCode() {
        final List<Character> characters = getShuffledAlphaNumList();

        final Random random = new Random();
        String alphaNum = "";
        for (int i = 0; i < CODE_SIZE; i++) {
            alphaNum += characters.get(random.nextInt(ALPHA_NUMERIC_CHAR_ARRAY.size()-1)).toString();
        }
        return alphaNum;
    }

    /**
     * @return return a shuffled copy of ALPHA_NUMERIC_CHAR_ARRAY
     */
    protected static List<Character> getShuffledAlphaNumList() {
        final Random random = new Random();
        final ArrayList<Character> tempAlphaNumList = new ArrayList<>(ALPHA_NUMERIC_CHAR_ARRAY);
        Collections.shuffle(ALPHA_NUMERIC_CHAR_ARRAY,random);
        return tempAlphaNumList;
    }

}
