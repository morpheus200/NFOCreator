/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 *
 * @author frank
 */
public class Connection {

    private String searchadultcduniverse = "http://www.cduniverse.com/warning.asp?Decision=I+Agree+%2D+ENTER&CrossOver=&Referer=%2Fsresult.asp%3FHT_Search=TITLE%26style=ice%26altsearch=no%26setshowimage=off%26HT%5FSearch%5FInfo=";

    private String refferAdult2 = "http://www.cduniverse.com/productinfo.asp?pid=";

    private String refferAdult = "http://www.cduniverse.com/warning.asp?Decision=I+Agree+%2D+ENTER&CrossOver=&Referer=";

    private String imgAdult = "http://www.cduniverse.com";

    private String searchimdb = "";

    private Lookups lookups = new Lookups();

    private String adultName = "";

    public Lookups ScanAdult(String movieName) {
        try {
            String website = ReadWebsite(searchadultcduniverse + movieName.replace(" ", "%20"));
            ResponseProcessing(website);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lookups;
    }

    public void run () {
        try {
            String website = ReadWebsite(searchadultcduniverse + this.adultName.replace(" ", "%20"));
            ResponseProcessing(website);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setName (String movieName) {
        this.adultName = movieName;
    }

   /* public Lookups getLookups () {
        return this.lookups;
    }*/

    public String[] ScanIMDB(String movieName) {
        try {
            ReadWebsite(searchimdb + movieName.replace(" ", "%20"));
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public boolean loadPicture(String pictureUrl, String destFolder) throws IOException {
        boolean status = false;
        try {
                BufferedImage image = null;
                URL url = new URL(pictureUrl);
                image = ImageIO.read(url);
                File folderjpg = new File(destFolder + File.separator + "folder.jpg");
                ImageIO.write(image, "jpg",folderjpg);
                status = true;
            } catch (IOException e) {
                    e.printStackTrace();
            }

        return status;
    }

    private String ReadWebsite(String urlstring) throws IOException {
        URL url = new URL(urlstring);
        URLConnection con = url.openConnection();
        Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
        Matcher m = p.matcher(con.getContentType());
        /* If Content-Type doesn't match this pre-conception, choose default and
         * hope for the best. */
        String charset = m.matches() ? m.group(1) : "ISO-8859-1";
        Reader r = new InputStreamReader(con.getInputStream(), charset);
        StringBuilder buf = new StringBuilder();
        while (true) {
          int ch = r.read();
          if (ch < 0)
            break;
          buf.append((char) ch);
        }

        return buf.toString();
    }

    private void ResponseProcessing(String response) {
        String[] splits = response.split("&nbsp;DVD</b>");
        if (splits.length == 1) {
            splits = response.split("&nbsp;DVDs</b>");
        }

        lookups.movieInfo = new String[3][splits.length - 1];
        lookups.moviePictureSmall = new BufferedImage[splits.length - 1];
        int deleter = 0;
        for (String buffer : splits) {
            if (deleter != splits.length - 1) {
                int index = buffer.indexOf("<a   href=\"/productinfo.asp");
                String sub = "";
                if (index < buffer.length() && index > 0) {
                    sub = buffer.substring(index);
                    //MovieName Extrahier den MovieNamen aus dem String (Suche-Antwort)
                    int movieNameIndex = sub.lastIndexOf("<b>");
                    lookups.movieInfo[0][deleter] = sub.substring(movieNameIndex + 3);
                    //PID Extrahiert PID aus dem Returnstring
                    String regex = "pid=[0-9]+";
                    Pattern pattern = Pattern.compile(regex);
                    // Check for the existence of the pattern
                    Matcher matcher = pattern.matcher(sub);
                    matcher.find();    // true
                    // Retrieve matching string
                    matcher.group();
                    // Retrieve indices of matching string
                    int pidStart = matcher.start();
                    int pidEnd = matcher.end();
                    lookups.movieInfo[1][deleter] = sub.substring(pidStart + 4, pidEnd);

                    String cover = "";
                    try {
                        cover = this.ReadWebsite(this.refferAdult2 + lookups.movieInfo[1][deleter]);
                    } catch (IOException ex) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    String[] coverSplits = cover.split("\">Large Front<");
                    /**
                     * Kein Front Cover vorhanden. Es wird dass Small Cover genommen.
                     */
                    if (coverSplits.length <= 1) {
                        this.LoadSmallCover(coverSplits, deleter);
                    } else {
                        this.LoadSmallCover(coverSplits, deleter);
                        this.GetLargeCoverURL(coverSplits, deleter);
                    }             

                }
            }
            
            deleter++;
        }
    }

    private void LoadSmallCover(String[] coverSplits, int counter){
        String searchStringSmallBegin = "http://cover";
        int finalSplitIndexStart = coverSplits[0].indexOf(searchStringSmallBegin);
        String finalSplit = coverSplits[0].substring(finalSplitIndexStart);
        String searchStringEnd = ".jpg";
        int urlIndexEnd = finalSplit.indexOf(searchStringEnd);
        //Bild Url wird gelesen und das Bild wird geladen und in den BufferImage[] geschrieben.
        URL url;
        try {
            url = new URL(finalSplit.substring(0, urlIndexEnd + searchStringEnd.length()));
            lookups.movieInfo[2][counter] = url.toString();
            lookups.moviePictureSmall[counter] = ImageIO.read(url);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void GetLargeCoverURL(String[]coverSplits, int counter) {
        String[] largeCoverSplits = coverSplits[0].toString().split("/images.asp");
        String largeCoverUrl = "/images.asp";
        largeCoverUrl = largeCoverUrl.concat(largeCoverSplits[largeCoverSplits.length - 1]);
        String response = "";
        try {
            response = this.ReadWebsite(this.imgAdult + largeCoverUrl);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }

        String[] splitResponse = response.split("img src=\"http");
        int stringend = splitResponse[1].indexOf("jpg\" border=0>");
        if (splitResponse != null) {
            lookups.movieInfo[2][counter] = "http" + splitResponse[1].substring(0, stringend + 3);
        }
    }
}