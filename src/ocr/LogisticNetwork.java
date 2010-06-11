/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Scanner;

/**
 * A layer of neurons.
 * @author dauphiya
 */
public class LogisticNetwork implements Serializable {

    public float[] biases;
    public float[][] weights;

    /**
     *
     * @param n_in number of inputs of the layer
     * @param n_out number of outputs of the layer
     */
    public LogisticNetwork(int n_in, int n_out) {
        biases = new float[n_out];
        weights = new float[n_out][n_in];
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
     * Get the output of the layer using the softmax activation function.
     * @param inputs
     * @return
     */
    public double[] getOutputSoftMax(double[] inputs) {
        double[] outputs = new double[weights.length];
        
        double outputs_sum = 0.0;

        for (int j = 0; j < weights.length; j++) {
            double sum = biases[j];
            for (int i = 0; i < weights[0].length; i++) {
                sum += inputs[i] * weights[j][i];
            }
            outputs[j] = Math.exp(sum);
            outputs_sum += outputs[j];
        }
        
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = outputs[i] / outputs_sum;
        }

        return outputs;
    }

    /**
     * Get the output of the layer using the Tanh activation function.
     * @param inputs
     * @return
     */
    public double[] getOutputTanh(double[] inputs) {
        double[] outputs = new double[weights.length];

        for (int j = 0; j < weights.length; j++) {
            double sum = biases[j];
            for (int i = 0; i < weights[0].length; i++) {
                sum += inputs[i] * weights[j][i];
            }
            outputs[j] = (Math.tanh(sum) + 1.0) / 2.0;
        }

        return outputs;
    }

    /**
     * Get the output of the layer using the sigmoid activation function.
     * @param inputs
     * @return
     */
    public double[] getOutputSigmoid(double[] inputs) {
        double[] outputs = new double[weights.length];

        for (int j = 0; j < weights.length; j++) {
            double sum = biases[j];
            for (int i = 0; i < weights[0].length; i++) {
                sum += inputs[i] * weights[j][i];
            }
            outputs[j] = 1.0 / (1.0 + Math.exp(-sum));
        }

        return outputs;
    }
    
}
