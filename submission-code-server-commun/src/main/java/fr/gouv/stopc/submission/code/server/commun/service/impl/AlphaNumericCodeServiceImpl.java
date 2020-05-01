package fr.gouv.stopc.submission.code.server.commun.service.impl;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;

import java.util.*;
import java.util.stream.Collectors;

public class AlphaNumericCodeServiceImpl implements IAlphaNumericCodeService {

    private static final String ALPHA_LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC= "0123456789";

    private static final List<Character> ALPHA_NUMERIC_CHAR_ARRAY = String
            .format(
                    "%s%s",
                    ALPHA_LOWER_CASE,
                    NUMERIC)
            .chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());

    private static final Integer MAX_SIZE = ALPHA_NUMERIC_CHAR_ARRAY.size();

    public static String generateAlphaNumericCode() {
        final List<Character> characters = getShuffledAlphaNumList();

        final Random random = new Random();
        String alphanum = "";
        for (int i = 0; i < 6; i++) {
            alphanum += characters.get(random.nextInt(MAX_SIZE-1)).toString();
        }
        return alphanum;
    }

    protected static List<Character> getShuffledAlphaNumList() {
        final Random random = new Random();
        final ArrayList<Character> tempAlphaNumList = new ArrayList<>(ALPHA_NUMERIC_CHAR_ARRAY);
        Collections.shuffle(tempAlphaNumList,random);
        return tempAlphaNumList;
    }



}
