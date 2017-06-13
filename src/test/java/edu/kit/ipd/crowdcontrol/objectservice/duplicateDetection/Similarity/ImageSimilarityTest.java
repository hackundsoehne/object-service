package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by lucaskrauss on 10/03/16.
 * Testclass for the ImageSimilarity-class
 */
public class ImageSimilarityTest {

    private BufferedImage similar1a;
    private BufferedImage similar1b;
    private BufferedImage different;
    private BufferedImage similar2a;
    private BufferedImage similar2b;
    private BufferedImage similar3a;
    private BufferedImage similar3b;


    @Before
    public void setUp() throws Exception {

        try {
            URL url1 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/s08.jpg");
            URL url2 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/d03.jpg");
            URL url3 = new URL("http://i.telegraph.co.uk/multimedia/archive/02830/cat_2830677b.jpg");
            URL url4 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/d06.jpg");
            URL url5 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/s13.jpg");
            URL url6 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/d04.jpg");
            URL url7 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/s09.jpg");
            similar1a = ImageIO.read(url1);
            similar1b = ImageIO.read(url2);
            different = ImageIO.read(url3);
            similar2a = ImageIO.read(url4);
            similar2b = ImageIO.read(url5);
            similar3a = ImageIO.read(url6);
            similar3b = ImageIO.read(url7);
        } catch (Exception e) {
            return;
        }
    }

    @After
    public void tearDown() throws Exception {
        similar1a = null;
        similar1b = null;
        similar2a = null;
        similar2b = null;
        different = null;

    }

    /**
     * Tests similarity-calculation based on image-signatures
     *
     * @throws Exception
     */
    @Test
    public void testIdentifyImageSimilarity() throws Exception {
        if (similar1a != null && similar1b != null && different != null && similar2b != null && similar2a != null) {
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1a, similar1b) > .95);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1a, different) < 90);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1b, different) < 90);

            assertEquals(ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1a, similar1a), 1, .00001);
            assertEquals(ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1b, similar1b), 1, .00001);
            assertEquals(ImageSimilarity.identifyImageSimilarityBasedOnSignature(different, different), 1, .00001);

/*
            System.out.println("1a:1b "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1a,similar1b));
            System.out.println("3a:3b "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar3a,similar3b));
            System.out.println("2a:2b "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar2a,similar2b)+"\n");
            System.out.println("3a:2b "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar3a,similar2b));
            System.out.println("3b:1a "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar3b,similar1a));
            System.out.println("1a:2a "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1a,similar2a));
            System.out.println("1b:2b "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1b,similar2b)+"\n");
            System.out.println("1a:d "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1a,different));
            System.out.println("1b:d "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar1b,different));
            System.out.println("2a:d "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar2a,different));
            System.out.println("2b:d "+ImageSimilarity.identifyImageSimilarityBasedOnSignature(similar2b,different));
            System.out.println("---------------------");
*/

        }
    }

    /**
     * Tests similarity-calculation with a simple pHash-algorithm.
     * This approach is less reliable than similarity-calculation via image-signature,
     * it is more likely to give false positives / negatives if one of the given images is a cutout from the other
     *
     * @throws Exception
     */
    @Test
    public void testSimilaritySimpleHashGreyscale() throws Exception {
        if (similar1a != null && similar1b != null && different != null && similar2b != null && similar2a != null) {

            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar1b, similar1a) >= .7);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar2b, similar2a) >= .7);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar1b, different) < .7);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar1b, different) < .7);

            assertEquals(ImageSimilarity.identifyImageSimilarityBasedOnPHash(different, different), 1, 0.00001);


        }
    }

    @Test
    @Ignore
    public void testSimilarityImageHashFromColorDeviation() throws Exception{
        assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar1b),ImageSimilarity.getImageHashFromColorDeviation(similar1a)) > .75);
        assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar3b),ImageSimilarity.getImageHashFromColorDeviation(similar3a)) > .75);
        assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar3b),ImageSimilarity.getImageHashFromColorDeviation(different)) < .75);
        assertEquals(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(different),ImageSimilarity.getImageHashFromColorDeviation(different)),1,0.0001);
    }

    @Test
    public void testSimilarityImageHashFromSignature() throws Exception{
        if (similar1a != null && similar1b != null && different != null && similar2b != null && similar2a != null) {

            assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a), ImageSimilarity.getImageHashFromSignature(similar1b)) >= .75);
            assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar2a), ImageSimilarity.getImageHashFromSignature(similar2b)) >=.75);
            assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar3a), ImageSimilarity.getImageHashFromSignature(similar3b)) >= .75);
            assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(different), ImageSimilarity.getImageHashFromSignature(different)) >= .75);

            assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a), ImageSimilarity.getImageHashFromSignature(different)) < .75);
            assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a), ImageSimilarity.getImageHashFromSignature(different)) < .75);
            assertTrue(HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a), ImageSimilarity.getImageHashFromSignature(different)) < .75);




           /* System.out.println("1a:1b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar1a),ImageSimilarity.getImageHashFromColorDeviation(similar1b)));
            System.out.println("3a:3b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar3a),ImageSimilarity.getImageHashFromColorDeviation(similar3b)));
            System.out.println("2a:2b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar2a),ImageSimilarity.getImageHashFromColorDeviation(similar2b))+"\n");
            System.out.println("1a:2a "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar1a),ImageSimilarity.getImageHashFromColorDeviation(similar2a)));
            System.out.println("3a:2b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar3a),ImageSimilarity.getImageHashFromColorDeviation(similar2b)));
            System.out.println("3b:1a "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar3b),ImageSimilarity.getImageHashFromColorDeviation(similar1a)));
            System.out.println("1b:2b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar1b),ImageSimilarity.getImageHashFromColorDeviation(similar2b))+"\n");


            System.out.println("1a:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar1a),ImageSimilarity.getImageHashFromColorDeviation(different)));
            System.out.println("1a:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar1a),ImageSimilarity.getImageHashFromColorDeviation(different)));
            System.out.println("2a:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar2a),ImageSimilarity.getImageHashFromColorDeviation(different)));
            System.out.println("2b:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromColorDeviation(similar2b),ImageSimilarity.getImageHashFromColorDeviation(different))+"\n\n");

            System.out.println("1a:1b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a),ImageSimilarity.getImageHashFromSignature(similar1b)));
            System.out.println("3a:3b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar3a),ImageSimilarity.getImageHashFromSignature(similar3b)));
            System.out.println("2a:2b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar2a),ImageSimilarity.getImageHashFromSignature(similar2b))+"\n");
            System.out.println("1a:2a "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a),ImageSimilarity.getImageHashFromSignature(similar2a)));
            System.out.println("3a:2b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar3a),ImageSimilarity.getImageHashFromSignature(similar2b)));
            System.out.println("3b:1a "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar3b),ImageSimilarity.getImageHashFromSignature(similar1a)));
            System.out.println("1b:2b "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1b),ImageSimilarity.getImageHashFromSignature(similar2b))+"\n");


            System.out.println("1a:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a),ImageSimilarity.getImageHashFromSignature(different)));
            System.out.println("1a:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar1a),ImageSimilarity.getImageHashFromSignature(different)));
            System.out.println("2a:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar2a),ImageSimilarity.getImageHashFromSignature(different)));
            System.out.println("2b:d "+HashSimilarity.getSimilarityFromHash(ImageSimilarity.getImageHashFromSignature(similar2b),ImageSimilarity.getImageHashFromSignature(different)));

*/


        }

        }
}