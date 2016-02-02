package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class Shingle {

    private static final int NGRAM_SIZE = 3;
    public static Set<String> getShingle(String input){

        Set<String> shingles = new HashSet<>();
        for (int i = 0; i < input.length() - (NGRAM_SIZE - 1); i++){

            String ngram = input.substring(i,i + NGRAM_SIZE);
            shingles.add(ngram);
        }
        return shingles;

    }
}
