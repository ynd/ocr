/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.nnet;

import java.io.Serializable;

/**
 * The sigmoid activation function for neural network layers.
 * @author dauphiya
 */
public class SigmoidActivation implements Activation, Serializable {

    public void getOutput(double[] inputs) {
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = 1.0 / (1.0 + Math.exp(inputs[i]));
        }
    }

}
