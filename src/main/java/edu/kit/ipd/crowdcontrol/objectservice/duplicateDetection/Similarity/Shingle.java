package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lucaskrauss at 02.02.2016
 *  @author lucaskrauss
 *
 *  Provides the functionality to retreive a set of n-grams of specified size
 *  of a given string
 *
 */
public class Shingle {

    /**
     * Gets a set of ngrams of the given string
     * @param input the stringe
     * @param ngramSize size of the n-grams
     * @return set of n-grams of the string
     */
    public static Set<String> getShingle(String input, int ngramSize ){

        Set<String> shingles = new HashSet<>();
        input = input.replace(" ","").toLowerCase();
        for (int i = 0; i < input.length() - (ngramSize - 1); i++){

            String ngram = input.substring(i,i + ngramSize);
            shingles.add(ngram);
        }
        return shingles;

    }

}
