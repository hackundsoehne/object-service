package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import com.google.common.collect.Sets;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class StringSimilarity {

    public static int getHammingDistance(String string1, String string2) {
        Set<String> shingle1 = Shingle.getShingle(string1);
        Set<String> shingle2 = Shingle.getShingle(string2);

        long hash1 = computeSimhashFromShingles(shingle1);
        long hash2 = computeSimhashFromShingles(shingle2);

        long bits = hash1 ^ hash2;
        int counter = 0;
        while (bits != 0) {
            bits &= bits - 1;
            ++counter;
        }
        return counter;

    }

    public static float getJaccardCoefficient(String stringA, String stringB){
        Set<String> shinglesA = Shingle.getShingle(stringA);
        Set<String> shinglesB = Shingle.getShingle(stringB);
        System.out.println(Sets.intersection(shinglesA,shinglesB).size());
        System.out.println(Sets.union(shinglesA,shinglesB).size());
        float intersection = Sets.intersection(shinglesA,shinglesB).size();
        float union = Sets.union(shinglesA,shinglesB).size();
        return intersection/union;

    }

    public static double getSimilarityBySimhash(String string1, String string2) {
        Set<String> shingle1 = Shingle.getShingle(string1);
        Set<String> shingle2 = Shingle.getShingle(string2);

        long hash1 = computeSimhashFromShingles(shingle1);
        long hash2 = computeSimhashFromShingles(shingle2);

        long xor = hash1 ^ hash2;
        if (xor == 0) {
            return 1.0;
        }
        return 1.0 - (((double) Long.bitCount(xor) + 1) / (65 - Long.numberOfLeadingZeros(xor)));

    }


    private static long computeSimhashFromShingles(Set<String> shingles) {
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
