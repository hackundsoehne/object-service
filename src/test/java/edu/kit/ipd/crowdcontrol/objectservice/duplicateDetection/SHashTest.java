package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import org.junit.Test;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class SHashTest {





    @Test
    public void testGetHammingDistance() throws Exception {


        String a = "Creates a bit set whose initial size is large enough to explicitly represent bits with indices in the range 0 through nbits-1.";
        String b = "Creates a bit set whose initial size is large enough to explicitly  bits with indices in the range 0 through nbits-1.";

        System.out.println("distance:" +SHash.getHammingDistance(a,b));

        System.out.println("Similarity: "+SHash.getSimilarity(a,b));

    }
}