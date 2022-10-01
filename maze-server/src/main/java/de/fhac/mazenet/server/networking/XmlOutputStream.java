package de.fhac.mazenet.server.networking;

import de.fhac.mazenet.server.generated.MazeCom;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

public class XmlOutputStream extends UTFOutputStream {

    private Marshaller marshaller;

    public XmlOutputStream(OutputStream outputStream) {
        super(outputStream);
        // Anlegen der JAXB-Komponenten
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MazeCom.class);
            this.marshaller = jaxbContext.createMarshaller();
            this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (JAXBException jaxbException) {
            Debug.print(Messages.getString("XmlOutputStream.ErrorInitialisingJAXBComponent"), DebugLevel.DEFAULT);
        }
    }

    /**
     * Versenden einer XML Nachricht
     *
     * @param mazeCom
     */
    public void write(MazeCom mazeCom) throws IOException {
        // Generierung des fertigen XML
        try {
            // Versenden des XML
            // String mazexml = mazeComToXML(mazeCom);
            // System.out.println("mazexml:" + mazexml);
            this.writeUTF8(mazeComToXML(mazeCom));
            Debug.print(String.format(Messages.getString("XmlOutputStream.SendMessageTypToID"),
                    mazeCom.getMessagetype().value(), mazeCom.getId()), DebugLevel.VERBOSE);
            this.flush();
        } catch (JAXBException e) {
            Debug.print(Messages.getString("XmlOutputStream.errorSendingMessage"), DebugLevel.DEFAULT);
            e.printStackTrace();
        }
    }

    /**
     * Stellt ein MazeCom-Objekt als XML dar
     *
     * @param mazeCom darzustellendes MazeCom-Objekt
     * @return XML-Darstellung als String
     * @throws JAXBException
     */
    public String mazeComToXML(MazeCom mazeCom) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        this.marshaller.marshal(mazeCom, stringWriter);
        return stringWriter.toString();
    }

}
