package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import com.google.common.collect.Sets;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

/**
 * Created by lucaskrauss at 02.02.2016
 * @author lucaskrauss
 *
 *
 * Provides several methods to compare strings.
 * The following functionallity is provided:
 * - jaccard-coefficient
 * - hamming-distance
 * - simhashing
 *
 */
public class StringSimilarity {



    /**
     * Computes the hamming-distance of the given strings
     * @param string1 first string
     * @param string2 second string
     * @return hamming distance of the input strings
     */
    public static int getHammingDistanceOfStrings(String string1, String string2) {
        Set<String> shingle1 = Shingle.getShingle(string1,3);
        Set<String> shingle2 = Shingle.getShingle(string2,3);

        long hash1 = computeSimhashFromShingles(shingle1);
        long hash2 = computeSimhashFromShingles(shingle2);

        return getHammingDistanceOfHashes(hash1,hash2);
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

    /**
     * Computes the jaccard-coefficient of two stings.
     * The given strings will be divided in sets of substrings which will be compared
     * to get the jaccard-coefficient.
     * @param stringA first string
     * @param stringB second string
     * @return jaccard-coefficient of the two input strings
     */
    public static float getJaccardCoefficient(String stringA, String stringB){
        Set<String> shinglesA = Shingle.getShingle(stringA,3);
        Set<String> shinglesB = Shingle.getShingle(stringB,3);
        float intersection = Sets.intersection(shinglesA,shinglesB).size();
        float union = Sets.union(shinglesA,shinglesB).size();
        return intersection/union;

    }

    /**
     * Computes the similarity of two strings via simhash
     * @param string1 first string
     * @param string2 second string
     * @return the similarity of the two given strings
     */
    public static double getSimilarityFromTwoString(String string1, String string2, int shingleNgramSize) {
        Set<String> shingle1 = Shingle.getShingle(string1,shingleNgramSize);
        Set<String> shingle2 = Shingle.getShingle(string2,shingleNgramSize);

        long hash1 = computeSimhashFromShingles(shingle1);
        long hash2 = computeSimhashFromShingles(shingle2);

        return getSimilarityFromHash(hash1,hash2);
    }



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
     * Computes hashing of the given set of shingles
     * @param shingles ngrams of the original input string
     * @return hash of the shingle-set
     */
    public static long computeSimhashFromShingles(Set<String> shingles) {
        //hash vector
        int[] vector = new int[64];

        for (String str : shingles) {
            long hash = 0;
            //generate hash with MD5
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte[] bytes = messageDigest.digest(str.getBytes());

                //get first 64-bits of MD5-hash
                ByteBuffer wrp = ByteBuffer.wrap(bytes);
                hash = wrp.getLong();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();

            }

            //set hast-vector
            for (int b = 0; b < 63; b++) {
                //bit test
                if ((hash & (0x8000000000000000L >>> b)) > 0) {
                    vector[b] += 1;
                } else {
                    vector[b] -= 1;
                }
            }
        }

        //build simhash
        long simhash = 0;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > 0) {
                simhash |= (0x8000000000000000L >>> i);
            }

        }

        return simhash;

    }





}
