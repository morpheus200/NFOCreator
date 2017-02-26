/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import org.jdesktop.application.Application;

/**
 *
 * @author frank
 */
public class IdleTask extends org.jdesktop.application.Task { // this is the Task
    public IdleTask(Application app) {
        super(app);
    }

    @Override
    protected  Void doInBackground() {
        try {
            // specific code for your task
            // this code shows progress bar with status message for a few seconds
            setMessage("starting up");// status message
            for(int progress=0; progress<100; progress += (int)(Math.random()*10)) {
                setProgress(progress); // progress bar (0-100)
                setMessage("prog: "+progress); // status message
                try {
                    Thread.sleep((long)500); // sleep 500ms
                } catch (InterruptedException ignore) {
                }
            }
            setMessage("done");// status message
        }
        catch(java.lang.Exception e) {
            //specific code for exceptions
        }

        return null;
    }

    protected void succeeded() {
    }
}
