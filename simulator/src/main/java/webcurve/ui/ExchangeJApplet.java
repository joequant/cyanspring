/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcurve.ui;
/**
 * @author dennis_d_chen@yahoo.com
 */
import javax.swing.JApplet;

import webcurve.exchange.Exchange;

/**
 *
 * @author dennis
 */
public class ExchangeJApplet extends JApplet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 968059193502092373L;

	/**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	ExchangeJFrame frame = new ExchangeJFrame(new Exchange());
                frame.setVisible(true);            
            }
        });
    }


}
