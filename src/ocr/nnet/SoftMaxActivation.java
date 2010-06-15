/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.nnet;

import java.io.Serializable;

/**
 * The SoftMax activation function for neural network layers.
 * @author dauphiya
 */
public class SoftMaxActivation implements Activation, Serializable {

    public void getOutput(double[] inputs) {
        double sum = 0.0;

        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = Math.exp(inputs[i]);
            sum += inputs[i];
        }
        
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = inputs[i] / sum;
        }
    }

}
