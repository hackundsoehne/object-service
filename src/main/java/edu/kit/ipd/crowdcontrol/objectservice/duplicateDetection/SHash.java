package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * Created by lucaskrauss at 02.02.2016
 */
public class SHash {

    public static int getHammingDistance(String string1, String string2){
        Set<String> shingle1 = Shingle.getShingle(string1);
        Set<String> shingle2 = Shingle.getShingle(string2);

        long hash1 = computeHashFromShingle(shingle1);
        long hash2 = computeHashFromShingle(shingle2);

        long bits = hash1 ^ hash2;
        int counter = 0;
        while (bits != 0){
            bits &= bits-1;
            ++counter;
        }
        return counter;

    }


    public static double getSimilarity(String string1, String string2){
        Set<String> shingle1 = Shingle.getShingle(string1);
        Set<String> shingle2 = Shingle.getShingle(string2);

        long hash1 = computeHashFromShingle(shingle1);
        long hash2 = computeHashFromShingle(shingle2);

        long xor = hash1 ^ hash2;
        System.out.println("\nhash1:"+hash1);
        System.out.println("hash2:"+hash2);
        System.out.println("xor"+xor);
        System.out.println("bitcnt:"+Long.bitCount(xor));
        System.out.println("bitsz:"+(64-Long.numberOfLeadingZeros(xor))+"\n");

        return 1.0-((double)Long.bitCount(xor)/(64-Long.numberOfLeadingZeros(xor)));

    }




    private static long computeHashFromShingle(Set<String> shingles) {

        int[] vector = new int[64];

        for (String str : shingles) {
            byte[] bytes = null;
            //generate hash
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                bytes = messageDigest.digest(str.getBytes());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            for (int b = 0; b < bytes.length; b++) {
                //bit test
                for (int i = 0; i < 8; i++) {
                    if ((((bytes[b] & 0xFF) >>> (7 - i)) & 0x1) > 0) {
                        vector[i] += 1;
                    } else {
                        vector[i] -= 1;
                    }
                }
            }
        }

        long shash = 0;
        for (int i = 0; i < vector.length; i++){
            if( vector[i] > 0){
                shash |= (1L << i);
            }
        }

        return shash;

    }


}
