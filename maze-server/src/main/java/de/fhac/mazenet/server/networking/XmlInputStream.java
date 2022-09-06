package de.fhac.mazenet.server.networking;

import de.fhac.mazenet.server.generated.MazeCom;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;

public class XmlInputStream extends UTFInputStream {

    private Unmarshaller unmarshaller;

    public XmlInputStream(InputStream inputStream) {
        super(inputStream);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MazeCom.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                // muss getResourceAsStream() statt getResource() sein
                // damit es auch in jars funktioniert
                InputStream resourceAsStream = getClass().getResourceAsStream("/xsd/mazeCom.xsd");
                // Der Inputstream resourceAsStream wird in die Datei temp.xsd
                // geschrieben und dann dem Schema uebergeben
                // XXX: Kein bessere Implementierung gefunden
                File tempFile = File.createTempFile("temp", ".xsd");
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                int read;
                byte[] bytes = new byte[1024];
                while ((read = resourceAsStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, read);
                }
                fileOutputStream.close();
                Schema schema = schemaFactory.newSchema(tempFile);
                unmarshaller.setSchema(schema);
                tempFile.deleteOnExit();
            } catch (SAXException e) {
                e.printStackTrace();
                Debug.print(Messages.getString("XmlInputStream.XMLSchemaFailed"), DebugLevel.DEFAULT);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JAXBException e) {
            Debug.print(Messages.getString("XmlInputStream.errorInitialisingJAXBComponent"), DebugLevel.DEFAULT);
        }
    }

    /**
     * Liest eine Nachricht und gibt die entsprechende Instanz zurueck
     * 
     * @return
     * @throws IOException
     */
    public MazeCom readMazeCom() throws IOException, UnmarshalException {
        MazeCom result = null;
        try {
            String xml = this.readUTF8();
            result = XMLToMazeCom(xml);
            Debug.print(Messages.getString("XmlInputStream.received"), DebugLevel.DEBUG);
            Debug.print(xml, DebugLevel.DEBUG);
        } catch (UnmarshalException e) {
            throw e;
        } catch (JAXBException e) {
            e.printStackTrace();
            Debug.print(Messages.getString("XmlInputStream.errorUnmarshalling"), DebugLevel.DEFAULT);
        } catch (NullPointerException e) {
            Debug.print(Messages.getString("XmlInputStream.nullpointerWhileReading"), DebugLevel.DEFAULT);
        }
        return result;
    }

    public MazeCom XMLToMazeCom(String xml) throws JAXBException {
        StringReader stringReader = new StringReader(xml);
        return (MazeCom) this.unmarshaller.unmarshal(stringReader);
    }

}