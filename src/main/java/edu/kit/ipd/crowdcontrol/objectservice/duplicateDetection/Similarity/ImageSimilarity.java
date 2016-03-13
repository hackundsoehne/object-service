package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection.Similarity;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;

/**
 * Created by lucaskrauss on 10.02.16.
 * @author lucaskrauss
 *
 * Calculates the similarity of two images.
 * Two algorithms are implemented:
 * Similarity via image-signature
 *  This approach is the more reliable one of the two given algorithms but the slower one aswell.
 *  For that each image is seperated in 25 sections and for each of them its color-signature is calculated.
 *  The corresponding sections of the pictures are copared after that. Their similarity is based on the deviation of
 *  the colorsignature of each section and thus based on location and color
 *
 * Similarity via a simple perceptual hash algorithm
 *  This approach is less reliable than similarity-calculation via image-signature,
 *  it is more likely to give false negatives if one of the given images is a cutout from the other
 *
 */
public class ImageSimilarity {

    final static double MAX_DISTANCE = Math.sqrt(3*(Math.pow(255,2)))*25;
    final static int BASE_SIZE = 300;



    /**
     * Identifies the similarity of the two given images.
     * Each image is divided in 25 sections, whose averaged color-values form the image-signature.
     * The similarity of the two images is based on the deviation of their color-value per image-section.
     *
     * @param image1 first image
     * @param image2 second image
     * @return similarity of the two images
     */
    public static double identifyImageSimilarityBasedOnSignature(BufferedImage image1, BufferedImage image2) {

        Color[][] signatureImage1 = getImageSignature(image1,60,5,5);
        Color[][] signatureImage2 = getImageSignature(image2,60,5,5);

        return identifyImageSignatureSimilarity(signatureImage1,signatureImage2);
    }

    /**
     * Identifies the similarity of the two given images.
     * The similarity is based on the hamming-distance of the perceptual-hashes of the two images {@link #getImageHashFromGreyScaleDeviation(BufferedImage)}
     * For more details refer to the class-description
     * @param bufferedImage1 first image
     * @param bufferedImage2 second image
     * @return similarity of the two images
     */
    public static double identifyImageSimilarityBasedOnPHash(BufferedImage bufferedImage1, BufferedImage bufferedImage2){
        return HashSimilarity.getSimilarityFromHash(getImageHashFromGreyScaleDeviation(bufferedImage1), getImageHashFromGreyScaleDeviation(bufferedImage2));
    }

    /**
     * Identifies the similarity of two given image-signatures
     * It is based on the deviation of the signatures color-value per section.
     *
     * @param signatureImage1 signature of the first image
     * @param signatureImage2 signature of the second image
     * @return similarity of the specified signatures
     */
    public static double identifyImageSignatureSimilarity(Color[][] signatureImage1, Color[][] signatureImage2){
        //calc distance based on the difference of the RGB-values of the imageSignatures
        double distance = 0;
        for (int i = 0; i < signatureImage1.length; i++) {
            for (int j = 0; j < signatureImage1[i].length; j++) {
                double red = Math.pow(signatureImage1[i][j].getRed() - signatureImage2[i][j].getRed(), 2);
                double green = Math.pow(signatureImage1[i][j].getGreen() - signatureImage2[i][j].getGreen(), 2);
                double blue = Math.pow(signatureImage1[i][j].getBlue() - signatureImage2[i][j].getBlue(), 2);
                distance += Math.sqrt(red + green + blue);
            }
        }


        //Normalize distance
        return 1-(distance/MAX_DISTANCE);
    }

    /**
     * Calculates the image-signature of the given image.
     * The signature is presented as 25 samples of the image. The color-value of the sample is averaged.
     * This signature combines color-information with locality-information.
     * @param image whose signature is calculated
     * @return a two dimensional Color-array. Each entry Color[x][y] contains the color-value of the image-section
     * (x,y) to (x+1,y+1). The locality of the image-section is preserved.
     */
    public static Color[][] getImageSignature(BufferedImage image, int sampleSize,int signatureHeight, int signatureWidth) {
        if (!(image.getHeight() == signatureHeight*sampleSize) && !(image.getWidth() == signatureWidth*sampleSize)) {
            image = rescale(image, signatureHeight*sampleSize,signatureWidth*sampleSize);
        }
        Color[][] imgSignature = new Color[signatureWidth][signatureHeight];

        for (int imgX = 0; imgX < imgSignature.length; imgX++) {
            for (int imgY = 0; imgY < imgSignature[0].length; imgY++) {
                imgSignature[imgX][imgY] = getAvg(image,sampleSize, imgX, imgY);
            }
        }
        return imgSignature;

    }

    /**
     * Calculates average color-signature of specified image-section
     *
     * @param image image to be processed
     * @param imgX x-coordinate of the beginning of the image-section
     * @param imgY y-coordinate of the beginning of the image-section
     * @return average color-signature of the image section
     */
    private static Color getAvg(BufferedImage image,int sampleSize, int imgX, int imgY) {
        int[] colorBuffer = new int[3];
        for (int x = imgX * sampleSize; x < (imgX + 1) * sampleSize; x++) {
            for (int y = imgY * sampleSize; y < (imgY + 1) * sampleSize; y++) {
                Color c = new Color(image.getRGB(y,x));
                colorBuffer[0] += c.getRed();
                colorBuffer[1] += c.getGreen();
                colorBuffer[2] += c.getBlue();
            }
        }
        colorBuffer[0] /= sampleSize*sampleSize;
        colorBuffer[1] /= sampleSize*sampleSize;
        colorBuffer[2] /= sampleSize*sampleSize;

        return new Color(colorBuffer[0], colorBuffer[1], colorBuffer[2]);
    }

    /**
     * Scales Image to BASE_SIZE * BASE_SIZE
     *
     * @param oldImage
     * @return
     */
    private static BufferedImage rescale(BufferedImage oldImage,int width,int heigth) {

        BufferedImage newImage = new BufferedImage(width, heigth, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = newImage.createGraphics();
        RenderingHints renderingHints = graphics.getRenderingHints();
        renderingHints.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.setRenderingHints(renderingHints);
        graphics.drawImage(oldImage,0,0,width,heigth,null);
        graphics.dispose();

        return newImage;

    }

    /**
     * Calculates 64-bit perceptual image hash.
     * The image is rescaled to 8x8-pixels. The hash is based on the grey-value of each pixel in comparison
     * to the images overall grey-value.
     * @param bufferedImage source image
     * @return perceptual image hash
     */
    public static long getImageHashFromGreyScaleDeviation(BufferedImage bufferedImage){
        bufferedImage = rescale(bufferedImage,8,8);

         ImageFilter filter = new GrayFilter(true,50);
        ImageProducer producer = new FilteredImageSource(bufferedImage.getSource(),filter);

        Image image =Toolkit.getDefaultToolkit().createImage(producer);
        bufferedImage = new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().drawImage(image,0,0,null);


        int buffer = 0;
        for (int i = 0; i < bufferedImage.getHeight(); i++) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {
                buffer += bufferedImage.getRGB(i,j);
            }
        }
        buffer /= 64;
        long hash = 0;
        for (int i = 0; i < bufferedImage.getHeight(); i++) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {
                hash += (bufferedImage.getRGB(i,j) > buffer) ? 1 : 0;
                hash = hash << 1;
            }
        }

        return hash;
    }

    /**
     * Calculates a 64-bit hash of the image based on the distribution of the colors and their deviation to the average
     * color
     * @param bufferedImage image
     * @return ash of the image
     */
    public static long getImageHashFromColorDeviation(BufferedImage bufferedImage){
        bufferedImage = rescale(bufferedImage,5,4);

        int averageRed = 0;
        int averageGreen = 0;
        int averageBlue = 0;
        for (int i = 0; i < bufferedImage.getHeight(); i++) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {
                Color c = new Color(bufferedImage.getRGB(j,i));
                averageRed += c.getRed();
                averageGreen += c.getGreen();
                averageBlue += c.getBlue();
            }
        }
        averageRed /=20;
        averageGreen /=20;
        averageBlue /=20;
        long hash = 0;

        for (int i = 0; i < bufferedImage.getHeight(); i++) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {
                Color c = new Color(bufferedImage.getRGB(j,i));
                hash |= (averageRed > c.getRed()) ? 1 : 0;
                hash <<= 1;
                hash |= (averageGreen > c.getGreen()) ? 1 : 0;
                hash <<= 1;
                hash |= (averageBlue > c.getBlue()) ? 1 : 0;
                hash <<= 1;
            }
        }
        hash |= averageRed > averageGreen ? 1 : 0;
        hash <<= 1;
        hash |= averageGreen > averageBlue ? 1 : 0;
        hash <<= 1;
        hash |= averageBlue > averageRed ? 1 : 0;
        return hash;

    }

    /**
     * Calculates 64-bit hash of the image.
     * The hash-values depends on the locality and the deviation of the colors to the average color.
     * @param bufferedImage the bufferedImage
     * @return hashing of the image
     */
    public static long getImageHashFromSignature(BufferedImage bufferedImage){
        Color [][] signature = getImageSignature(bufferedImage,30,4,5);
        long hash = 0;
        int avgRed = 0;
        int avgGreen = 0;
        int avgBlue = 0;
        for (Color[] aSignature : signature) {
            for (Color anASignature : aSignature) {
                avgRed += anASignature.getRed();
                avgGreen += anASignature.getGreen();
                avgBlue += anASignature.getBlue();
            }
        }
        avgRed /=25;
        avgGreen /= 25;
        avgBlue /=25;
        for (int i = 0; i < signature.length-1; i++) {
            for (int j = 0; j < signature[i].length - 1; j++) {
                hash |= (signature[i][j].getRed() > avgRed) ? 1 : 0;
                hash <<= 1;
                hash |= (signature[i][j].getGreen() > avgGreen) ? 1 : 0;
                hash <<= 1;
                hash |= (signature[i][j].getBlue() > avgBlue) ? 1 : 0;
                        hash <<= 1;
            }
        }
        hash |= avgRed > avgGreen ? 1 : 0;
        hash <<= 1;
        hash |= avgGreen > avgBlue ? 1 : 0;
        hash <<= 1;
        hash |= avgBlue > avgRed ? 1 : 0;
        return hash;
    }
}
