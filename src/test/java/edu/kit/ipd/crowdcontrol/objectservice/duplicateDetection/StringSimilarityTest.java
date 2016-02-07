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
    String diffB = "Tom drives to the red square in his car";



    @Test
    public void testGetJaccardCoefficient() throws Exception{


        //test similar strings
        assertTrue(StringSimilarity.getJaccardCoefficient(simA,simB) > 0.8);
        //test equal strings
        assertEquals(StringSimilarity.getJaccardCoefficient(simA,simA),1.0, 0.0001);
        //test different strings
        assertTrue(StringSimilarity.getJaccardCoefficient(diffA,diffB) < 0.8);

    }

    @Test
    public void testSimhash() throws Exception {


        //test similar strings
        assertTrue(StringSimilarity.getSimilarityFromString(simA,simB) > 0.8 );
        //test equal strings
        assertEquals(StringSimilarity.getSimilarityFromString(simA,simA),1.0,0.00001);
        //test different strings
        assertTrue(StringSimilarity.getSimilarityFromString(diffA,diffB) < 0.8);

    }
}