package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity;

import org.junit.After;
import org.junit.Before;
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


    @Before
    public void setUp() throws Exception {

        try {
            URL url1 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/s08.jpg");
            URL url2 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/d03.jpg");
            URL url3 = new URL("http://i.telegraph.co.uk/multimedia/archive/02830/cat_2830677b.jpg");
            URL url4 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/d06.jpg");
            URL url5 = new URL("http://www.lac.inpe.br/JIPCookbook/Resources/ImageSimilarity/s13.jpg");
            similar1a = ImageIO.read(url1);
            similar1b = ImageIO.read(url2);
            different = ImageIO.read(url3);
            similar2a = ImageIO.read(url4);
            similar2b = ImageIO.read(url5);
        } catch (Exception e) {
            return;
        }
    }

    @After
    public void tearDown() throws Exception {
        similar1a = null;
        similar1b = null;
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
    public void testSimilaritySimplePHash() throws Exception {
        if (similar1a != null && similar1b != null && different != null && similar2b != null && similar2a != null) {

            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar1b, similar1a) >= .7);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar2b, similar2a) >= .7);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar1b, different) < .7);
            assertTrue(ImageSimilarity.identifyImageSimilarityBasedOnPHash(similar1b, different) < .7);

            assertEquals(ImageSimilarity.identifyImageSimilarityBasedOnPHash(different, different), 1, 0.00001);


        }
    }
}