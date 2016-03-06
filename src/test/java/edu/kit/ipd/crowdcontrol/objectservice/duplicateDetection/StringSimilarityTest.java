package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.StringSimilarity;
import org.junit.Ignore;
import edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity.StringSimilarity;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class StringSimilarityTest {
    String simA1 = "Henry C. Harper v. The Law Offices of Huey & Luey, LLP";
    String simA2 =  "Harper v. The Law Offices of Huey & Luey, LLP";

    String diffA = "Tom drives down the road in his red car";
    String diffB = "Tom drives to the red square in his car";

    String simB1 = "Once upon a midnight dreary, while I pondered, weak and weary,\n" +
            "Over many a quaint and curious volume of forgotten lore";

    String simB2= "Once upon in a midnight dream, while I pondered, weary and weak,\n" +
            "Over many a quaint and curious volume of forgotten lore";

    @Test
    public void testGetJaccardCoefficient() throws Exception{


        //test similar strings
        assertTrue(StringSimilarity.getJaccardCoefficient(simA1, simA2) > 0.8);
        //test equal strings
        assertEquals(StringSimilarity.getJaccardCoefficient(simA1, simA1),1.0, 0.0001);
        //test different strings
        assertTrue(StringSimilarity.getJaccardCoefficient(diffA,diffB) < 0.8);

    }

    @Test
    public void testSimhash() throws Exception {


        //test similar strings
        assertTrue(StringSimilarity.getSimilarityFromTwoString(simA1, simA2,3) > 0.8 );
        //test equal strings
        assertEquals(StringSimilarity.getSimilarityFromTwoString(simA1, simA1,3),1.0,0.00001);
        //test different strings
        assertTrue(StringSimilarity.getSimilarityFromTwoString(diffA,diffB,3) < 0.8);

    }


    @Ignore
    @Test
    public void estimateBestShingleSize() throws Exception{
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA2,1));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA2,2));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA2,3));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA2,4));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA2,5)+"\n");

        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA1,1));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA1,2));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA1,3));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA1,4));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simA1, simA1,5)+"\n");

        System.out.println(StringSimilarity.getSimilarityFromTwoString(diffA,diffB,1));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(diffA,diffB,2));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(diffA,diffB,3));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(diffA,diffB,4));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(diffA,diffB,5)+"\n");

        System.out.println(StringSimilarity.getSimilarityFromTwoString(simB1,simB2,1));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simB1,simB2,2));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simB1,simB2,3));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simB1,simB2,4));
        System.out.println(StringSimilarity.getSimilarityFromTwoString(simB1,simB2,5));



    }
}