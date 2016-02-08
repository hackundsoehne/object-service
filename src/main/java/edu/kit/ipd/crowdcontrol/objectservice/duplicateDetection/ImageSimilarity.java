package edu.kit.ipd.crowdcontrol.objectservice.duplicateDetection;

import edu.kit.ipd.crowdcontrol.objectservice.proto.Experiment;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

/**
 * Created by lucas on 08.02.16.
 */
public class ImageSimilarity {

    private final static int BASE_SIZE = 300;

    private double computeImageSimilarity(RenderedImage img1, RenderedImage img2){
        return 0;
    }

    private RenderedImage rescale(RenderedImage img){

        float heightScale = (float)BASE_SIZE / img.getHeight();
        float widthScale = (float)BASE_SIZE / img.getWidth();
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(img);
        parameterBlock.add(heightScale);
        parameterBlock.add(widthScale);
        parameterBlock.add(0.0F);
        parameterBlock.add(0.0F);



        return null;
    }

}
