/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 *
 * @author frank
 */
public class Moviename {

    private static Moviename instance = null;
   // private Lookups lookups = new Lookups();
    private LinkedList<Lookups> linkLookups = new LinkedList();

    public static Moviename getInstance() throws IOException {
        if (instance == null) {
            instance = new Moviename();
        }
        return instance;
    }

    public LinkedList<Lookups> GetMovielist(String foldername){
        addFiles(foldername);
        return this.linkLookups;
    }

    public String[] DirectoryListening(String foldername) {
        File file = new File(foldername);
        String[] dir = file.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return new File (dir, name).isDirectory();
          }
        });
        if (dir != null) {
            return dir;
        }
        else {
            return null;
        }
    }
    /**
     * Scannt das Verzeichnis nach Filmdateien
     * @param foldername Pfad des Scanfolders
     */
    private void addFiles(String foldername) {
        String regex = "\\s-\\s([A-Z][A-Z]|[a-z][a-z]|[a-z][A-Z]|[A-Z][a-z])+[1-9]";
        File dir = new File(foldername);

        // It is also possible to filter the list of returned files.
        // This example does not return any files that start with `.'.
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String[] suffixFilter = {".mpg", ".avi" , ".mpeg", ".mkv", "mp4"};

                for (String filter : suffixFilter)
                {
                    if (name.endsWith(filter)){
                        return true;
                    }
                }
                    return false;
            }
        };

        File[] files = dir.listFiles(filter);

        String[][] movieBuffer = new String[files.length][3];

        int i = 0;
        for (File buffer : files) {
            //voller name
            movieBuffer[i][0] = buffer.getName();

            //name ohne endung
            int index = buffer.getName().lastIndexOf('.');
            if (index>0&& index <= buffer.getName().length() - 2 ) {
                movieBuffer[i][1] = buffer.getName().substring(0, index);
            }

            //komplett geschnitten
            movieBuffer[i][2] =
                    buffer.getName().substring(0, index).replaceAll(regex, "");
            i++;
        }

        String doubleBuffer = "";
        i=0;
        for (String[] buffer : movieBuffer) {
            if (!doubleBuffer.equals(buffer[2])) {
                Lookups lookups = new Lookups();
                lookups.movies = buffer;
                linkLookups.add(lookups);
                i++;
                doubleBuffer = buffer[2];
            }
        }
    }

    /**
     * Erstellt ein Verzeichnis
     * @param directoryCreate VErzeichnis zum erstellen
     * @return state
     */
    public boolean CreateFolder(String directoryCreate){
        if (!FolderExist(directoryCreate)) {
            return new File(directoryCreate).mkdir();
        }
        else {
            return false;
        }
    }

    public boolean CopyFile (String source, String dest) throws FileNotFoundException, IOException {
        boolean copy = false;
        if (FileExist(source) && !FileExist(dest)) {
           File fsource = new File(source);
           File fdest = new File(dest);
           InputStream in = new FileInputStream(fsource);
           OutputStream out = new FileOutputStream(fdest);
           byte[] buf = new byte[1024];
           int len;
           while ((len = in.read(buf)) > 0){
           out.write(buf, 0, len);
           }

           in.close();
           out.close();
           copy = true;
        }

        return copy;
    }

    /**
     * Verschiebt alle Moviedateien welche den movieName enthalten.
     * @param movieName Name des Movie (teilweise)
     * @param sourceFolder Quellverzichnis
     * @param destFolder Zielverzeichnis
     * @return state
     */
    public boolean MoveFile(String movieName, String sourceFolder, String destFolder) {
        boolean copy = false;
        if (FolderExist(destFolder)) {
            // File (or directory) to be moved

            File sourcedir = new File(sourceFolder);
            FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                    String[] suffixFilter = {".avi", ".mpg", ".mpeg", ".mkv"};
                    for (String filter : suffixFilter)
                    {
                        return name.endsWith(filter);
                    }
                        return false;
                }
            };

            File[] files = sourcedir.listFiles(filter);
            // Destination directory
            File target = new File(destFolder);
            File source;


            for (File file : files) {
                if (file.toString().contains(movieName)) {
                    source = file;
                    copy = source.renameTo(new File(target, source.getName()));
                }
            }
        }

        return copy;
    }

    private boolean FolderExist(String directoryCreate) {
        return new File (directoryCreate).exists();
    }

    public boolean FileExist(String directoryFilename) {
        return new File (directoryFilename).exists() ;
    }
}
