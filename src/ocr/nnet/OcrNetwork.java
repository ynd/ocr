/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocr.nnet;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * A Neural Network used to do OCR.
 * @author dauphiya
 */
public class OcrNetwork implements Serializable {

    private final NeuralNetworkLayer[] layers;

    /**
     * Create the Ocr Network with the predefined configuration.
     */
    public OcrNetwork(NeuralNetworkLayer[] layers) {
        this.layers = layers;
    }

    /**
     * Load the parameters of the network from text files.
     */
    public void loadParameters(InputStream[] streams) {
        for (int i = 0; i < streams.length; i++) {
            layers[i].loadParameters(streams[i]);
        }
    }

    /**
     * Serialize the neural network to a file.
     */
    public void saveObject(OutputStream output) {
        try {
            ObjectOutputStream obj_out = new ObjectOutputStream(output);

            obj_out.writeObject(this);
        } catch (IOException ex) {
            Logger.getLogger(OcrNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deserialize neural network from a file.
     * @return deserialized network
     */
    public static OcrNetwork loadObject(InputStream input) {
        OcrNetwork network = null;
        BufferedInputStream bi = new BufferedInputStream(input);
        try {
            // Read bytes 'B' and 'Z'.
            bi.read();
            bi.read();

            ObjectInputStream in = new ObjectInputStream(new CBZip2InputStream(bi));
            network = (OcrNetwork) in.readObject();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(OcrNetwork.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OcrNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }

        return network;
    }

    /**
     * Get the network's input layer for the given image.
     * @return
     */
    private double[] getInputLayer(BufferedImage image) {
        double[] inputs = new double[image.getWidth() * image.getHeight()];

        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                int pixel = image.getRGB(i, j);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                inputs[j * image.getWidth() + i] = (red + green + blue) / (255.0 * 3.0);
            }
        }

        return inputs;
    }

    /**
     * Deform the image slightly by means of translation, rotation and scaling.
     * @param input image
     * @param output image
     * @param rng seeded random number generator
     */
    private void getDeformedImage(BufferedImage input,
            BufferedImage output, Random rng) {
        double xTranslation = (rng.nextInt(20) - 10) / 100. * output.getWidth();
        double yTranslation = (rng.nextInt(20) - 10) / 100. * output.getHeight();
        double rotation = Math.toRadians(rng.nextInt(20) - 10);
        double xScale = (1 + (rng.nextInt(20) - 10) / 100.) * output.getWidth();
        double yScale = (1 + (rng.nextInt(20) - 10) / 100.) * output.getHeight();

        Graphics2D g = (Graphics2D) output.getGraphics();
        g.translate(xTranslation, yTranslation);
        g.rotate(rotation, output.getWidth() / 2, output.getHeight() / 2);
        g.drawImage(input, 0, 0, (int) xScale, (int) yScale, null);
    }

    /**
     * Return the output of the network for the given image.
     * @param image
     * @return
     */
    public double[] getOutput(BufferedImage image) {
        double[] outputs = getInputLayer(image);

//        for (int i = 0; i < layers.length; i++) {
//            outputs = layers[i].getOutput(outputs);
//        }

        for (NeuralNetworkLayer layer: layers) {
            outputs = layer.getOutput(outputs);
        }

        return outputs;
    }

    /**
     * Return the output of the network for the given image. Get the mean
     * prediction for multiple deformations of the input image.
     * @param image
     * @return
     */
    public double[] getOutputRobust(BufferedImage image) {
        double[] outputs = getOutput(image);

        final int count = 10;
        Random rng = new Random(Calendar.getInstance().getTimeInMillis());
        BufferedImage deformedImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < (count - 1); i++) {
            getDeformedImage(image, deformedImage, rng);

            double[] deformedOutputs = getOutput(deformedImage);
            for (int j = 0; j < deformedOutputs.length; j++) {
                outputs[j] += deformedOutputs[j];
            }
        }

        for (int i = 0; i < outputs.length; i++) {
            outputs[i] /= count;
        }

        return outputs;
    }

    /**
     * Convert an index of the output of the neural network to a character.
     * @param category
     * @return
     */
    public static String indexToLabel(int index) {
        if (index <= 9) {
            return String.valueOf(index);
        } else if (index <= 35) {
            return String.valueOf((char) (65 + (index - 10)));
        } else {
            return String.valueOf((char) (97 + (index - 36)));
        }
    }
}
