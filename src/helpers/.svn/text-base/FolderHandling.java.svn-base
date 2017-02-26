/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author frank
 */
public class FolderHandling {

   private static FolderHandling instance = null;

    public static FolderHandling getInstance() throws IOException {
        if (instance == null) {
            instance = new FolderHandling();
        }
        return instance;
    }

    public void createFolder(String foldername, String rootfolder) {
        File file = new File(rootfolder + File.pathSeparator + foldername);
    }
}
