package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class Shingle {

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
