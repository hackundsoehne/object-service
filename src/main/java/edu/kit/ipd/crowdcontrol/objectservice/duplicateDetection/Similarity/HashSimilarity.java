package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity;

/**
 * Created by lucaskrauss on 06/03/16.
 * @author lucaskrauss
 *
 *  This class provides the utilities to identify similar hashes. It supports:
 *   - percentual similarity identification
 *   - hamming distance
 */
public class HashSimilarity {

    /**
     * Computes the similarity of two hashes via simhash
     * @param hash1 first hash
     * @param hash2 second hash
     * @return the similarity of the two given hashes
     */
    public static double getSimilarityFromHash(long hash1, long hash2){
        long xor = hash1 ^ hash2;
        if(xor == 0){
            return 1;
        }
        return 1.0 - (((double) Long.bitCount(xor) + 1) / (65 - Long.numberOfLeadingZeros(xor)));
    }

    /**
     * Calculates the hamming-distance of two given hashes
     * @param hash1 first hash
     * @param hash2 second hash
     * @return hamming distance of the specified hashes
     */
    public static int getHammingDistanceOfHashes(long hash1, long hash2){
        long xor = hash1 ^ hash2;
        return Long.bitCount(xor);

    }
}
