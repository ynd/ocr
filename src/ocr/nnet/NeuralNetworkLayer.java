/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocr.nnet;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Scanner;

/**
 * A layer of neurons.
 * @author dauphiya
 */
public class NeuralNetworkLayer implements Serializable {

    private final float[] biases;
    private final float[][] weights;
    private final Activation activation;

    /**
     *
     * @param n_in number of inputs of the layer
     * @param n_out number of outputs of the layer
     */
    public NeuralNetworkLayer(int n_in, int n_out, Activation activation) {
        this.biases = new float[n_out];
        this.weights = new float[n_out][n_in];
        this.activation = activation;
    }

    /**
     * Load the parameters of the network from the given input stream.
     * @param parameters input stream to read parameters from
     */
    public void loadParameters(InputStream parameters) {
        Scanner input = new Scanner(parameters);

        for (int i = 0; i < weights[0].length; i++) {
            for (int j = 0; j < weights.length; j++) {
                weights[j][i] = Float.parseFloat(input.next());
            }
        }

        for (int i = 0; i < biases.length; i++) {
            biases[i] = Float.parseFloat(input.next());
        }
    }

    /**
     * Get the output of the layer.
     * @param inputs
     * @return
     */
    public double[] getOutput(double[] inputs) {
        double[] outputs = new double[weights.length];

        for (int j = 0; j < weights.length; j++) {
            double sum = biases[j];
            for (int i = 0; i < weights[0].length; i++) {
                sum += inputs[i] * weights[j][i];
            }

            outputs[j] = sum;
        }

        activation.getOutput(outputs);

        return outputs;
    }
}
