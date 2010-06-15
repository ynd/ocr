/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocr.nnet;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import ocr.OcrApp;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * The Neural Network used to do OCR.
 * @author dauphiya
 */
public class OcrNetwork implements Serializable {

    private NeuralNetworkLayer[] layers;

    /**
     * Create the Ocr Network with the predefined configuration.
     */
    public OcrNetwork() {
        layers = new NeuralNetworkLayer[4];
        layers[0] = new NeuralNetworkLayer(32 * 32, 1000, new TanhActivation());
        layers[1] = new NeuralNetworkLayer(1000, 1000, new TanhActivation());
        layers[2] = new NeuralNetworkLayer(1000, 1000, new TanhActivation());
        layers[3] = new NeuralNetworkLayer(1000, 62, new SoftMaxActivation());
    }

    /**
     * Load the parameters of the network from text files.
     */
    public void loadParameters() {
        layers[0].loadParameters(OcrApp.class.getResourceAsStream("resources/params/layer0.save"));
        layers[1].loadParameters(OcrApp.class.getResourceAsStream("resources/params/layer1.save"));
        layers[2].loadParameters(OcrApp.class.getResourceAsStream("resources/params/layer2.save"));
        layers[3].loadParameters(OcrApp.class.getResourceAsStream("resources/params/layer3.save"));
    }

    /**
     * Serialize the neural network to a file.
     */
    public void saveObject() {
        try {
            FileOutputStream f_out = new FileOutputStream("network.save");

            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

            obj_out.writeObject(this);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OcrNetwork.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OcrNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Deserialize neural network from a file.
     * @return deserialized network
     */
    public static OcrNetwork loadObject() {
        OcrNetwork network = null;
        BufferedInputStream bi = new BufferedInputStream(OcrApp.class.getResourceAsStream("resources/params/network.save.bz2"));
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
     * Return the output of the network for the given image.
     * @param image
     * @return
     */
    public double[] getOutput(BufferedImage image) {
        double[] inputs = getInputLayer(image);

        double[] output0 = layers[0].getOutput(inputs);
        double[] output1 = layers[1].getOutput(output0);
        double[] output2 = layers[2].getOutput(output1);
        double[] output3 = layers[3].getOutput(output2);

        return output3;
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
