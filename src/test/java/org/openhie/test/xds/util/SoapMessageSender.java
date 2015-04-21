package org.openhie.test.xds.util;

/**
 * Created by snkasthu on 4/20/15.
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.soap.*;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

public class SoapMessageSender {

    private static final Log log = LogFactory.getLog(SoapMessageSender.class);

    public static void main(String args[]) throws Exception {
        SoapMessageSender test = new SoapMessageSender();

        // Create SOAP Connection
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();


        // Send SOAP Message to SOAP Server
        String url = "http://iol.test.ohie.org:5001/xdsrepository";
        SOAPMessage so = test.getSoapMessageFromString();
        SOAPHeader header = so.getSOAPHeader();


        log.info(soapConnection.call(test.getSoapMessageFromString(), url));

        // print SOAP Response
        System.out.print("Response SOAP Message:");
        //soapResponse.writeTo(System.out);

        soapConnection.close();
    }




    /*private static SOAPMessage createSOAPRequest() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "urn:ihe:iti:xds-b:2007";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("example", serverURI);

        /*
        Constructed SOAP Request Message:
        <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:example="http://ws.cdyne.com/">
            <SOAP-ENV:Header/>
            <SOAP-ENV:Body>
                <example:VerifyEmail>
                    <example:email>mutantninja@gmail.com</example:email>
                    <example:LicenseKey>123</example:LicenseKey>
                </example:VerifyEmail>
            </SOAP-ENV:Body>
        </SOAP-ENV:Envelope>
         */

      /*  SOAPMessage message = MessageFactory.newInstance().createMessage();
        soapPart.setContent(new StreamSource(new FileInputStream("/Users/snkasthu/SourceCode/cds/openhie-integration-tests/src/test/resources/xds/OHIE-XDS-01-10.xml")));
        message.saveChanges();

        System.out.print("Request SOAP Message:");
        message.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }*/

    private SOAPMessage getSoapMessageFromString() throws SOAPException, IOException {
        MessageFactory factory = MessageFactory.newInstance();

       //MimeHeaders header=new MimeHeaders();
        //header.addHeader("Content-Type","application/xop+xml; application/soap+xml" + "action=urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b" + "boundary=MIMEBoundaryurn_uuid_E3F7CE4554928DA89B1231365678616;"+ "Content-Transfer-Encoding=binary" + "Content-ID=\"<0.urn:uuid:E3F7CE4554928DA89B1231365678617@apache.org>\"");
        //header.addHeader("action", "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b");


        SOAPMessage message = MessageFactory.newInstance().createMessage();
        SOAPPart soapPart = message.getSOAPPart();

        MimeHeaders header= message.getMimeHeaders();
        //header.addHeader("Content-Type","application/xop+xml; application/soap+xml" + "action=urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b" + "boundary=MIMEBoundaryurn_uuid_E3F7CE4554928DA89B1231365678616;"+ "Content-Transfer-Encoding=binary" + "Content-ID=\"<0.urn:uuid:E3F7CE4554928DA89B1231365678617@apache.org>\"");
        //header.addHeader("Content-Type", "multipart/related;start=<rootpart*17f1ec84-85ec-4f1f-9b29-423a6a2e354f@example.jaxws.sun.com>;type=application/xop+xml;boundary=uuid:17f1ec84-85ec-4f1f-9b29-423a6a2e354f\";start-info=\"application/soap+xml\";action=\"urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b");
        header.setHeader("Content-Type", "application/soap+xml;charset=UTF-8");

        SOAPHeader soap = message.getSOAPHeader();
        soapPart.setContent(new StreamSource(new FileInputStream("/Users/snkasthu/SourceCode/cds/openhie-integration-tests/src/test/resources/xds/OHIE-XDS-01-10.xml")));

        System.out.print("Request SOAP Message :");
        message.writeTo(System.out);
        System.out.println();



        return message;
    }

}
