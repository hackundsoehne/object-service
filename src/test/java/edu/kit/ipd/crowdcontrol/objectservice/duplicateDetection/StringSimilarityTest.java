package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class StringSimilarityTest {
    String simA = "Henry C. Harper v. The Law Offices of Huey & Luey, LLP";
    String simB =  "Harper v. The Law Offices of Huey & Luey, LLP";


    @Test
    public void testGetJaccardCoefficient() throws Exception{
        System.out.println("Jaccard: "+StringSimilarity.getJaccardCoefficient(simA,simB));


    }

    @Test
    public void testGetHammingDistance() throws Exception {

        System.out.println("Dist: "+ StringSimilarity.getHammingDistance(simA,simB));
        System.out.println("Similarity: "+ StringSimilarity.getSimilarityBySimhash(simA,simB));
        assertTrue(StringSimilarity.getSimilarityBySimhash(simA,simB) > 0.8 );

    }
}