/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.nnet;

/**
 * The activation function of a neural network layer.
 * @author dauphiya
 */
public interface Activation {
    /**
     * Get the activation for the given inputs.
     * @param inputs
     * @return
     */
    public void getOutput(double[] inputs);
}
