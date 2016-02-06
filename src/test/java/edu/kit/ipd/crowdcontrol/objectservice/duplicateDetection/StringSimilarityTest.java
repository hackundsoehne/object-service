package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class StringSimilarityTest {
    String simA = "Henry C. Harper v. The Law Offices of Huey & Luey, LLP";
    String simB =  "Harper v. The Law Offices of Huey & Luey, LLP";

    String diffA = "Tom drives down the road in his red car";
    String diffB = "Tom drives to the red place in his car";

    String leander1 = "lucas krauss hört gerne musik";
    String leander2 = "lukas krauss hört gerne musik";
    String leander3 = "luckas krauss hört gerne musik";

    @Test
    public void testGetJaccardCoefficient() throws Exception{
        System.out.println("jA: "+ StringSimilarity.getJaccardCoefficient(leander1,leander2));
        System.out.println("jB: "+StringSimilarity.getJaccardCoefficient(leander2,leander3));
        System.out.println("jC: "+StringSimilarity.getJaccardCoefficient(leander1,leander3));


        //test similar strings
        assertTrue(StringSimilarity.getJaccardCoefficient(simA,simB) > 0.8);
        //test equal strings
        assertEquals(StringSimilarity.getJaccardCoefficient(simA,simA),1.0, 0.0001);
        //test different strings
        assertTrue(StringSimilarity.getJaccardCoefficient(diffA,diffB) < 0.8);

    }

    @Test
    public void testGetHammingDistance() throws Exception {


        System.out.println("A: "+ StringSimilarity.getSimilarityFromString(leander1,leander2));
        System.out.println("B: "+StringSimilarity.getSimilarityFromString(leander2,leander3));
        System.out.println("C: "+StringSimilarity.getSimilarityFromString(leander1,leander3));
        //test similar strings
        assertTrue(StringSimilarity.getSimilarityFromString(simA,simB) > 0.8 );
        //test equal strings
        assertEquals(StringSimilarity.getSimilarityFromString(simA,simA),1.0,0.00001);
        //test different strings
        assertTrue(StringSimilarity.getSimilarityFromString(diffA,diffB) < 0.8);

    }
}