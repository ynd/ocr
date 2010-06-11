/*
 * OcrView.java
 */
package ocr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSlider;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * The application's main frame.
 */
public class OcrView extends FrameView {

    public OcrView(SingleFrameApplication app) {
        super(app);

        initComponents();

        resultImage = new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY);
        scaledImage = new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY);
        paths = new LinkedList<Pair<GeneralPath, Integer>>();

        rng = new Random(Calendar.getInstance().getTimeInMillis());

        // Create the layers of the network.


        // Load serialized network with ObjectInputStream
        // Here's how the initial network was created:
//        network = new LogisticNetwork[4];
//        network[0] = new LogisticNetwork(32 * 32, 1000);
//        network[1] = new LogisticNetwork(1000, 1000);
//        network[2] = new LogisticNetwork(1000, 1000);
//        network[3] = new LogisticNetwork(1000, 62);
//
//        // Load parameters of the model.
//        network[0].loadParameters(getClass().getResourceAsStream("resources/params/layer0.save"));
//        network[1].loadParameters(getClass().getResourceAsStream("resources/params/layer1.save"));
//        network[2].loadParameters(getClass().getResourceAsStream("resources/params/layer2.save"));
//        network[3].loadParameters(getClass().getResourceAsStream("resources/params/layer3.save"));

        BufferedInputStream bi = new BufferedInputStream(getClass().getResourceAsStream("resources/params/network.save.bz2"));
        try {
            // Read bytes 'B' and 'Z'.
            bi.read();
            bi.read();

            ObjectInputStream in = new ObjectInputStream(new CBZip2InputStream(bi));
            network = (LogisticNetwork[]) in.readObject();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(OcrView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OcrView.class.getName()).log(Level.SEVERE, null, ex);
        }

        predictionTimer = new java.util.Timer();
        predictionTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                double[] inputs = getNetworkInputLayer(resultImage);

                double[] outputs = getNetworkOutputLayer(inputs);

                // Display the 3 most probable categories.
                int[] maximums = new int[3];
                maximums[0] = maximums[1] = maximums[2] = -1;

                for (int i = 0; i < outputs.length; i++) {
                    if (maximums[0] == -1 || outputs[i] >= outputs[maximums[0]]) {
                        maximums[2] = maximums[1];
                        maximums[1] = maximums[0];
                        maximums[0] = i;
                    } else if (maximums[1] == -1 || outputs[i] >= outputs[maximums[1]]) {
                        maximums[2] = maximums[1];
                        maximums[1] = i;
                    } else if (maximums[2] == -1 || outputs[i] >= outputs[maximums[2]]) {
                        maximums[2] = i;
                    }
                }

                predLabel0.setText(categorytoLabel(maximums[0]));
                predBar0.setValue((int) (outputs[maximums[0]] * 100));
                predLabel1.setText(categorytoLabel(maximums[1]));
                predBar1.setValue((int) (outputs[maximums[1]] * 100));
                predLabel2.setText(categorytoLabel(maximums[2]));
                predBar2.setValue((int) (outputs[maximums[2]] * 100));
            }
        }, 0, 100);
    }

    /**
     * Get the network's input layer for the given image.
     * @return
     */
    private double[] getNetworkInputLayer(BufferedImage image) {
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
     * Get output of the network for the given input.
     * @return
     */
    private double[] getNetworkOutputLayer(double[] inputs) {
        double[] output0 = network[0].getOutputTanh(inputs);
        double[] output1 = network[1].getOutputTanh(output0);
        double[] output2 = network[2].getOutputTanh(output1);
        double[] output3 = network[3].getOutputSoftMax(output2);

        return output3;
    }

    /**
     * Convert an index of the output of the neural network to a character.
     * @param category
     * @return
     */
    private String categorytoLabel(int category) {
        if (category <= 9) {
            return String.valueOf(category);
        } else if (category <= 35) {
            return String.valueOf((char) (65 + (category - 10)));
        } else {
            return String.valueOf((char) (97 + (category - 36)));
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = OcrApp.getApplication().getMainFrame();
            aboutBox = new OcrAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        OcrApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        inputPanel = new javax.swing.JPanel() {
            public void paint(Graphics g) {
                g.drawImage(inputImage, 0, 0, null);
            }
        };
        jLabel1 = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        thicknessSlider = new javax.swing.JSlider();
        predLabel0 = new javax.swing.JLabel();
        predLabel1 = new javax.swing.JLabel();
        predLabel2 = new javax.swing.JLabel();
        predBar0 = new javax.swing.JProgressBar();
        predBar1 = new javax.swing.JProgressBar();
        predBar2 = new javax.swing.JProgressBar();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        xTranslationSlider = new javax.swing.JSlider();
        yTranslationSlider = new javax.swing.JSlider();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        resultPanel = new javax.swing.JPanel() {
            public void paint(Graphics g) {
                g.drawImage(resultImage, 0, 0, null);
            }
        };
        jLabel7 = new javax.swing.JLabel();
        rotationSlider = new javax.swing.JSlider();
        resetTransformationButton = new javax.swing.JButton();
        yScaleSlider = new javax.swing.JSlider();
        xScaleSlider = new javax.swing.JSlider();
        jLabel8 = new javax.swing.JLabel();
        saltSlider = new javax.swing.JSlider();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        gaussianSlider = new javax.swing.JSlider();
        polaritySlider = new javax.swing.JSlider();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        scratchingSlider = new javax.swing.JSlider();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ocr.OcrApp.class).getContext().getResourceMap(OcrView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setMaximumSize(new java.awt.Dimension(880, 390));
        mainPanel.setMinimumSize(new java.awt.Dimension(880, 390));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setOpaque(false);
        mainPanel.setPreferredSize(new java.awt.Dimension(880, 390));

        inputPanel.setBackground(resourceMap.getColor("inputPanel.background")); // NOI18N
        inputPanel.setMaximumSize(new java.awt.Dimension(140, 140));
        inputPanel.setMinimumSize(new java.awt.Dimension(140, 140));
        inputPanel.setName("inputPanel"); // NOI18N
        inputPanel.setPreferredSize(new java.awt.Dimension(140, 140));
        inputPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                inputPanelMousePressed(evt);
            }
        });
        inputPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                inputPanelMouseDragged(evt);
            }
        });
        inputImage = new BufferedImage(inputPanel.getPreferredSize().width, inputPanel.getPreferredSize().height, BufferedImage.TYPE_BYTE_GRAY);

        org.jdesktop.layout.GroupLayout inputPanelLayout = new org.jdesktop.layout.GroupLayout(inputPanel);
        inputPanel.setLayout(inputPanelLayout);
        inputPanelLayout.setHorizontalGroup(
            inputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 134, Short.MAX_VALUE)
        );
        inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 140, Short.MAX_VALUE)
        );

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        resetButton.setText(resourceMap.getString("resetButton.text")); // NOI18N
        resetButton.setName("resetButton"); // NOI18N
        resetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resetButtonMouseClicked(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(62, 45));

        thicknessSlider.setMaximum(10);
        thicknessSlider.setValue(5);
        thicknessSlider.setName("thicknessSlider"); // NOI18N
        thicknessSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                thicknessSliderStateChanged(evt);
            }
        });

        predLabel0.setFont(resourceMap.getFont("predLabel0.font")); // NOI18N
        predLabel0.setForeground(resourceMap.getColor("predLabel0.foreground")); // NOI18N
        predLabel0.setText(resourceMap.getString("predLabel0.text")); // NOI18N
        predLabel0.setMaximumSize(new java.awt.Dimension(14, 17));
        predLabel0.setMinimumSize(new java.awt.Dimension(14, 17));
        predLabel0.setName("predLabel0"); // NOI18N
        predLabel0.setPreferredSize(new java.awt.Dimension(14, 17));

        predLabel1.setText(resourceMap.getString("predLabel1.text")); // NOI18N
        predLabel1.setMaximumSize(new java.awt.Dimension(14, 17));
        predLabel1.setMinimumSize(new java.awt.Dimension(14, 17));
        predLabel1.setName("predLabel1"); // NOI18N
        predLabel1.setPreferredSize(new java.awt.Dimension(14, 17));

        predLabel2.setText(resourceMap.getString("predLabel2.text")); // NOI18N
        predLabel2.setMaximumSize(new java.awt.Dimension(14, 17));
        predLabel2.setMinimumSize(new java.awt.Dimension(14, 17));
        predLabel2.setName("predLabel2"); // NOI18N
        predLabel2.setPreferredSize(new java.awt.Dimension(14, 17));

        predBar0.setName("predBar0"); // NOI18N
        predBar0.setStringPainted(true);

        predBar1.setName("predBar1"); // NOI18N
        predBar1.setStringPainted(true);

        predBar2.setName("predBar2"); // NOI18N
        predBar2.setStringPainted(true);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setName("jSeparator1"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(70, 39));

        xTranslationSlider.setMaximum(50);
        xTranslationSlider.setMinimum(-50);
        xTranslationSlider.setValue(0);
        xTranslationSlider.setName("xTranslationSlider"); // NOI18N
        xTranslationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                xTranslationSliderStateChanged(evt);
            }
        });

        yTranslationSlider.setMaximum(50);
        yTranslationSlider.setMinimum(-50);
        yTranslationSlider.setValue(0);
        yTranslationSlider.setName("yTranslationSlider"); // NOI18N
        yTranslationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                yTranslationSliderStateChanged(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setName("jSeparator2"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        resultPanel.setBackground(resourceMap.getColor("resultPanel.background")); // NOI18N
        resultPanel.setName("resultPanel"); // NOI18N
        resultPanel.setPreferredSize(new java.awt.Dimension(32, 32));

        org.jdesktop.layout.GroupLayout resultPanelLayout = new org.jdesktop.layout.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 32, Short.MAX_VALUE)
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 32, Short.MAX_VALUE)
        );

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(70, 39));

        rotationSlider.setMaximum(180);
        rotationSlider.setMinimum(-180);
        rotationSlider.setValue(0);
        rotationSlider.setName("rotationSlider"); // NOI18N
        rotationSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rotationSliderStateChanged(evt);
            }
        });

        resetTransformationButton.setText(resourceMap.getString("resetTransformationButton.text")); // NOI18N
        resetTransformationButton.setName("resetTransformationButton"); // NOI18N
        resetTransformationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resetTransformationButtonMouseClicked(evt);
            }
        });

        yScaleSlider.setMaximum(50);
        yScaleSlider.setMinimum(-50);
        yScaleSlider.setValue(0);
        yScaleSlider.setName("yScaleSlider"); // NOI18N
        yScaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                yScaleSliderStateChanged(evt);
            }
        });

        xScaleSlider.setMaximum(50);
        xScaleSlider.setMinimum(-50);
        xScaleSlider.setValue(0);
        xScaleSlider.setName("xScaleSlider"); // NOI18N
        xScaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                xScaleSliderStateChanged(evt);
            }
        });

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(70, 39));

        saltSlider.setValue(0);
        saltSlider.setName("saltSlider"); // NOI18N
        saltSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                saltSliderStateChanged(evt);
            }
        });

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(70, 39));

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(70, 39));

        gaussianSlider.setValue(0);
        gaussianSlider.setName("gaussianSlider"); // NOI18N
        gaussianSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                gaussianSliderStateChanged(evt);
            }
        });

        polaritySlider.setMinimum(-100);
        polaritySlider.setValue(100);
        polaritySlider.setName("polaritySlider"); // NOI18N
        polaritySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                polaritySliderStateChanged(evt);
            }
        });

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(70, 39));

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(70, 39));

        scratchingSlider.setValue(0);
        scratchingSlider.setName("scratchingSlider"); // NOI18N
        scratchingSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                scratchingSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel1)
                    .add(inputPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resetButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(thicknessSlider, 0, 0, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, resetTransformationButton)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(jLabel4)
                                .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                                .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                            .add(jLabel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                            .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(scratchingSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .add(polaritySlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(xScaleSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(yScaleSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(rotationSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(xTranslationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(yTranslationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(saltSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                            .add(gaussianSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(resultPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(predLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(predLabel0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(predLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(predBar0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(predBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(predBar2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jLabel3))
                .add(294, 294, 294))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(inputPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(resetButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(thicknessSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(6, 6, 6))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(31, 31, 31)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(xTranslationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(yTranslationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(rotationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(xScaleSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(yScaleSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(saltSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(gaussianSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(polaritySlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .add(9, 9, 9)
                                        .add(scratchingSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 46, Short.MAX_VALUE)
                                        .add(resetTransformationButton))
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                            .add(jLabel4)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(resultPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jLabel3)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .add(17, 17, 17)
                                        .add(predBar0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(predLabel0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(11, 11, 11)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(predBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(predLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(18, 18, 18)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(predBar2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(predLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ocr.OcrApp.class).getContext().getActionMap(OcrView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Draw the content of the input panel.
     */
    private void drawInputPanel() {
        Graphics2D g = (Graphics2D) inputImage.getGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, inputPanel.getWidth(), inputPanel.getHeight());

        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Pair<GeneralPath, Integer> p : paths) {
            g.setStroke(new BasicStroke(p.getSecond(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(p.getFirst());
        }

        inputPanel.repaint();

        drawResultPanel();
    }

    /**
     * Draw the content of the result panel.
     */
    private void drawResultPanel() {
        Graphics2D bg = scaledImage.createGraphics();
        bg.setColor(Color.BLACK);
        bg.fillRect(0, 0, scaledImage.getWidth(), scaledImage.getHeight());
        for (int i = 0; i < (int) (0.999 + 10.0*scratching/100.); i++) {
            int x = -10 + rng.nextInt(52);
            int y = -10 + rng.nextInt(52);
            int l = 5 + (int) (scratching * rng.nextInt(30) / 100.);
            double t = 2 * Math.PI * rng.nextDouble();
            int w = 1 + (int) (scratching * rng.nextInt(3) / 100.);
            int c = (50 + (int) (scratching * rng.nextInt(250) / 100.)) % 256;

            bg.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            bg.setColor(new Color(c, c, c));
            GeneralPath scratch = new GeneralPath();
            scratch.moveTo(x, y);
            scratch.lineTo((int)(x + l*Math.cos(t)), (int)(y + l*Math.cos(t)));
            bg.draw(scratch);
        }

        Graphics2D fg = resultImage.createGraphics();
        fg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        fg.setColor(Color.BLACK);
        fg.fillRect(0, 0, resultImage.getWidth(), resultImage.getHeight());
        fg.translate(xTranslation / 100.0 * resultImage.getWidth(),
                yTranslation / 100.0 * resultImage.getHeight());
        fg.rotate(rotation * Math.PI / 180.0, resultImage.getWidth() / 2,
                resultImage.getHeight() / 2);
        fg.drawImage(inputImage, 0, 0, (int) ((1.0 + xScale / 100.0) * resultImage.getWidth()),
                (int) ((1.0 + yScale / 100.0) * resultImage.getHeight()), null);

        // Apply non-linear transformations.
        for (int j = 0; j < resultImage.getHeight(); j++) {
            for (int i = 0; i < resultImage.getWidth(); i++) {
                int pixelFg = (resultImage.getRGB(i, j) >> 16) & 0xff;
                int pixelBg = (scaledImage.getRGB(i, j) >> 16) & 0xff;
                int pixel = Math.max(pixelFg, pixelBg);

                if (rng.nextFloat() < salt / 500.0f) {
                    pixel = rng.nextInt(256);
                }

                if (gaussian != 0) {
                    pixel += rng.nextGaussian() * gaussian * 55. / 100.;
                }

                pixel = Math.min(Math.max(pixel, 0), 255);

                pixel = (int) (127.5 + (pixel - 127.5) * polarity / 100.);

                Color c = new Color(pixel, pixel, pixel);
                resultImage.setRGB(i, j, c.getRGB());
            }
        }

        resultPanel.repaint();
    }

    /**
     * Handle dragging on the input panel to draw the brush.
     * @param evt
     */
    private void inputPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inputPanelMouseDragged
        paths.getLast().getFirst().lineTo(evt.getX(), evt.getY());
        drawInputPanel();
    }//GEN-LAST:event_inputPanelMouseDragged

    /**
     * Handle click on the reset button to reset the input and result panels.
     * @param evt
     */
    private void resetButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resetButtonMouseClicked
        Graphics g = inputImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, inputPanel.getWidth(), inputPanel.getHeight());
        inputPanel.repaint();

        g = resultImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, resultImage.getWidth(), resultImage.getHeight());
        resultPanel.repaint();

        paths.clear();
    }//GEN-LAST:event_resetButtonMouseClicked

    /**
     * Handle changes on the thickness slider to adjust thickness.
     * @param evt
     */
    private void thicknessSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thicknessSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        thickness = 2 * source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_thicknessSliderStateChanged

    /**
     * Handle mouse pressed on the input panel to start drawing the brush.
     * @param evt
     */
    private void inputPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inputPanelMousePressed
        // Force drawing by adding fake second point
        if (paths.isEmpty() || paths.getLast().getSecond() != thickness) {
            paths.add(new Pair<GeneralPath, Integer>(new GeneralPath(), thickness));
        }
        paths.getLast().getFirst().moveTo(evt.getX(), evt.getY());
        paths.getLast().getFirst().lineTo(evt.getX(), evt.getY() + 0.1f);
        drawInputPanel();
    }//GEN-LAST:event_inputPanelMousePressed

    /**
     * Handle changes on the x-axis translation slider to adjust translation.
     * @param evt
     */
    private void xTranslationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_xTranslationSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        xTranslation = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_xTranslationSliderStateChanged

    /**
     * Handle changes on the y-axis translation slider to adjust translation.
     * @param evt
     */
    private void yTranslationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_yTranslationSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        yTranslation = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_yTranslationSliderStateChanged

    /**
     * Handle changes on rotation slider to adjust rotation of the image.
     * @param evt
     */
    private void rotationSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rotationSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        rotation = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_rotationSliderStateChanged

    /**
     * Handle click on the reset transformations button.
     * @param evt
     */
    private void resetTransformationButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resetTransformationButtonMouseClicked
        xTranslationSlider.setValue(0);
        yTranslationSlider.setValue(0);
        rotationSlider.setValue(0);
        xScaleSlider.setValue(0);
        yScaleSlider.setValue(0);
        saltSlider.setValue(0);
        gaussianSlider.setValue(0);
        polaritySlider.setValue(100);
        scratchingSlider.setValue(0);
    }//GEN-LAST:event_resetTransformationButtonMouseClicked

    /**
     * Handle changes on y scale slider to adjust scale of the image.
     * @param evt
     */
    private void yScaleSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_yScaleSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        yScale = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_yScaleSliderStateChanged

    /**
     * Handle changes on x scale slider to adjust scale of the image.
     * @param evt
     */
    private void xScaleSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_xScaleSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        xScale = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_xScaleSliderStateChanged

    /**
     * Handle changes on salt and pepper slider to adjust noise in the image.
     * @param evt
     */
    private void saltSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_saltSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        salt = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_saltSliderStateChanged

    /**
     * Handle changes on gaussian slider to adjust noise in the image.
     * @param evt
     */
    private void gaussianSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_gaussianSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        gaussian = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_gaussianSliderStateChanged

    /**
     * Handle changes on polarity slider to adjust polarity of the image.
     * @param evt
     */
    private void polaritySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_polaritySliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        polarity = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_polaritySliderStateChanged

    private void scratchingSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_scratchingSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        scratching = source.getValue();
        drawInputPanel();
    }//GEN-LAST:event_scratchingSliderStateChanged

    /**
     * Pairs of possibly different element types.
     * @param <T>
     * @param <S>
     */
    public class Pair<T, S> {

        public Pair(T f, S s) {
            first = f;
            second = s;
        }

        public T getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return "(" + first.toString() + ", " + second.toString() + ")";
        }
        private T first;
        private S second;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider gaussianSlider;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JSlider polaritySlider;
    private javax.swing.JProgressBar predBar0;
    private javax.swing.JProgressBar predBar1;
    private javax.swing.JProgressBar predBar2;
    private javax.swing.JLabel predLabel0;
    private javax.swing.JLabel predLabel1;
    private javax.swing.JLabel predLabel2;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton resetTransformationButton;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JSlider rotationSlider;
    private javax.swing.JSlider saltSlider;
    private javax.swing.JSlider scratchingSlider;
    private javax.swing.JSlider thicknessSlider;
    private javax.swing.JSlider xScaleSlider;
    private javax.swing.JSlider xTranslationSlider;
    private javax.swing.JSlider yScaleSlider;
    private javax.swing.JSlider yTranslationSlider;
    // End of variables declaration//GEN-END:variables
    private Random rng;
    private LogisticNetwork[] network;
    private final java.util.Timer predictionTimer;
    private Image inputImage;
    private BufferedImage resultImage;
    private BufferedImage scaledImage;
    private LinkedList<Pair<GeneralPath, Integer>> paths;
    private int thickness = 10;
    private int xTranslation = 0;
    private int yTranslation = 0;
    private int rotation = 0;
    private int xScale = 0;
    private int yScale = 0;
    private int salt = 0;
    private int gaussian = 0;
    private int polarity = 100;
    private int scratching = 0;
    private JDialog aboutBox;
}
