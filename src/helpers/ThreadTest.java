/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.Timer;
import javax.swing.JLabel;
import javax.swing.text.View;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author frank
 */
public class ThreadTest implements Runnable {

    private int busyIconIndex = 0;
    private Timer busyIconTimer;
    private JLabel statusAnimationLabel;
    private Icon[] busyIcons;
    private int busyAnimationRate;
    private boolean loop = true;


    public ThreadTest(JLabel statusAnimationLabel, Icon[] busyIcons, ResourceMap resourceMap) {
        this.statusAnimationLabel = statusAnimationLabel;
        this.busyIcons = busyIcons;
        this.busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
    }

    public void run() {
       if (!busyIconTimer.isRunning()) {
           busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                    statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
                }
            });

           statusAnimationLabel.setVisible(true);
           this.busyIconIndex = 0;
           statusAnimationLabel.setIcon(busyIcons[this.busyIconIndex]);
           busyIconTimer.start();
        }

       while (loop){}
    }

    public void stop() {
        loop = false;
        this.busyIconTimer.stop();
    }
}
