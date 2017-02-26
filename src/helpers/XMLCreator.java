/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author frank
 */
public class XMLCreator {

    private static XMLCreator instance = null;

    public static XMLCreator getInstance() throws IOException {
        if (instance == null) {
            instance = new XMLCreator();
        }
        return instance;
    }

    public void CreateNFOAdult(String folderName, String movieNameNoCut, String movieNameCD, String movieID){
        try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                DOMImplementation impl = docBuilder.getDOMImplementation();

                // movie elements
                //Document doc = docBuilder.newDocument();
                Document doc = impl.createDocument(null,null,null);
                Element rootElement = doc.createElement("movie");
                doc.appendChild(rootElement);

                // title elements
                Element title = doc.createElement("title");
                title.appendChild(doc.createTextNode(movieNameNoCut));
                rootElement.appendChild(title);

                // sorttitle elements
                Element sorttitle = doc.createElement("sorttitle");
                sorttitle.appendChild(doc.createTextNode(movieNameNoCut));
                rootElement.appendChild(sorttitle);

                // rating elements
                Element rating = doc.createElement("rating");
                rating.appendChild(doc.createTextNode(""));
                rootElement.appendChild(rating);

                // year elements
                Element year = doc.createElement("year");
                year.appendChild(doc.createTextNode(""));
                rootElement.appendChild(year);

                // plot elements
                Element plot = doc.createElement("plot");
                plot.appendChild(doc.createTextNode(""));
                rootElement.appendChild(plot);

                // tagline elements
                Element tagline = doc.createElement("tagline");
                tagline.appendChild(doc.createTextNode(""));
                rootElement.appendChild(tagline);

                // runtime elements
                Element runtime = doc.createElement("runtime");
                runtime.appendChild(doc.createTextNode(""));
                rootElement.appendChild(runtime);

                // mpaa elements
                Element mpaa = doc.createElement("mpaa");
                mpaa.appendChild(doc.createTextNode("R"));
                rootElement.appendChild(mpaa);

                // language elements
                Element language = doc.createElement("language");
                language.appendChild(doc.createTextNode(""));
                rootElement.appendChild(language);

                // id elements
                Element id = doc.createElement("id");
                id.appendChild(doc.createTextNode(movieID));
                rootElement.appendChild(id);

                // genre elements
                Element genre = doc.createElement("genre");
                genre.appendChild(doc.createTextNode("Adult"));
                rootElement.appendChild(genre);

                // write the content into xml file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                String nfoFile = folderName + File.separator + movieNameCD + ".nfo";

                StringWriter sw = new StringWriter();
                StreamResult result = new StreamResult(sw);
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
    
                BufferedWriter out;

                out = new BufferedWriter(new FileWriter(nfoFile));
                out.write(sw.toString());
                out.close();


          } catch (IOException ex) {
            Logger.getLogger(XMLCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException pce) {
                pce.printStackTrace();
          } catch (TransformerException tfe) {
                tfe.printStackTrace();
          }
    }
}
