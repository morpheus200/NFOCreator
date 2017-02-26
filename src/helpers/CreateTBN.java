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
public class CreateTBN {

   private static CreateTBN instance = null;

    public static CreateTBN getInstance() throws IOException {
        if (instance == null) {
            instance = new CreateTBN();
        }
        return instance;
    }

    public boolean create(String[] dirList, String homeDirectory) throws IOException {
        boolean ret = false;
        for (String item : dirList) {
            String fullPath = homeDirectory + File.separator + item + File.separator;
            if ((Moviename.getInstance().FileExist(fullPath + "folder.jpg")) &&
                    !Moviename.getInstance().FileExist(fullPath + "movie.tbn")) {
                ret = Moviename.getInstance().CopyFile(fullPath + "folder.jpg", fullPath + "movie.tbn");
            }
        }

        return ret;
    }
}
