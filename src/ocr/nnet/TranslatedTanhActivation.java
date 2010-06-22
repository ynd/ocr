/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.nnet;

import java.io.Serializable;

/**
 * The Tanh activation function for neural network layers.
 * @author dauphiya
 */
public class TranslatedTanhActivation implements Activation, Serializable {

    public void getOutput(double[] inputs) {
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = (Math.tanh(inputs[i]) + 1.0) / 2.0;
        }
    }

}
