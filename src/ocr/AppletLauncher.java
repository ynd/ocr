/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocr;

import java.awt.Color;
import javax.swing.JApplet;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

/**
 *
 * @author dauphiya
 */
public class AppletLauncher extends JApplet {

    @Override
    public void init() {
        OcrApp app = OcrApp.getInstance(OcrApp.class);
        OcrView view = new OcrView(app);
        view.getMenuBar().setVisible(false);
        setContentPane(view.getRootPane());

        view.getComponent().setBackground(UIManager.getColor("Panel.background"));
        view.getComponent().setBorder(new LineBorder(new Color(172, 172, 186), 1, true));
    }
}
