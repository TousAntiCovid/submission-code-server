package fr.gouv.stopc.submission.code.server.commun.service.impl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AlphaNumericCodeServiceImplTest {


    @Test
    void generateAlphaNumericCodeTest() {
        Long size = new Long("300000");
        final long start = System.currentTimeMillis();
        final List<String> alphaNumIDS = Stream.generate(AlphaNumericCodeServiceImpl::generateAlphaNumericCode)
                .distinct()
                .limit(size)
                .collect(Collectors.toList());
        System.out.println(String.format("expected %s and get %s in %s millis", size, alphaNumIDS.size(), System.currentTimeMillis()-start));
        assertEquals(size, alphaNumIDS.size());
    }

    @Test
    void getShuffledAlphaNumListTest() {
        final long start = System.currentTimeMillis();
        final List<Character> shuffledAlphaNumList = AlphaNumericCodeServiceImpl.getShuffledAlphaNumList();
        System.out.println(String.format("expected %s and get %s in %s millis", 26+10, shuffledAlphaNumList.size(), System.currentTimeMillis()-start));
        assertEquals(26+10, shuffledAlphaNumList.size());
    }




}