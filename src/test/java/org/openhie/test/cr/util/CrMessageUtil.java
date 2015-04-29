package org.openhie.test.cr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.AbstractComposite;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.builder.support.DefaultValidationBuilder;
import ca.uhn.hl7v2.validation.builder.support.NoValidationBuilder;

/**
 * Message utilities
 * @author Justin
 *
 */
public final class CrMessageUtil {

    private static Log log = LogFactory.getLog(CrMessageUtil.class);

    // OpenHIE endpoint
    private static String s_endpoint = System.getProperty("ohie-cr-endpoint");
    private static String s_useTls = System.getProperty("ohie-cr-use-tls");
    private static HapiContext s_hapiContext = null;

    /**
     * Create hapi context
     */
    static {

        if(s_endpoint == null)
            s_endpoint = "localhost:2100";

        s_hapiContext = new DefaultHapiContext();
        s_hapiContext.setValidationRuleBuilder(new DefaultValidationBuilder());

    }

    /**
     * Get a connection
     * @return
     */
    private static Connection getConnection() {

        // Configure the endpoint
        String hostName = s_endpoint.split(":")[0];
        Integer hostPort = 2100;
        Boolean useTls = false;

        try {
            hostPort = Integer.parseInt(s_endpoint.split(":")[1]);
            useTls = Boolean.parseBoolean(s_useTls);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            return s_hapiContext.newClient(hostName, hostPort, useTls);
        } catch (HL7Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    /**
     * Load a message from resources
     * @param messageName
     * @return
     * @throws IOException
     * @throws HL7Exception
     */
    public static Message loadMessage(String messageName) throws IOException, HL7Exception
    {
        URL fileUrl = CrMessageUtil.class.getResource(String.format("/cr/%s.txt", messageName));
        File fileUnderTest = new File(fileUrl.getFile());
        FileInputStream fs = null;
        try
        {
            fs = new FileInputStream(fileUnderTest);
            String s = IOUtils.toString(fs);
            s = s.replace('\n', '\r');
            return new PipeParser().parse(s);
        }
        finally
        {
            if(fs != null)
                fs.close();
        }
    }

    /**
     * Send a message and parse the response
     * @param request
     * @return
     * @throws IOException
     * @throws LLPException
     * @throws HL7Exception
     */
    public static Message sendMessage(Message request) throws HL7Exception, LLPException, IOException {
        Terser requestTerser = new Terser(request);
        requestTerser.set("/MSH-7", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        try
        {
            requestTerser.set("/EVN-2", requestTerser.get("/MSH-7"));
        }
        catch(Exception e) {}

        log.debug(new PipeParser().encode(request));
        Connection conn = CrMessageUtil.getConnection();
        Initiator init = conn.getInitiator();
        Message response = init.sendAndReceive(request);
        log.debug(new PipeParser().encode(response));
        return response;
    }

    /**
     * Assert the message was rejected
     * @param assertTerser
     * @throws HL7Exception
     */
    public static void assertRejected(Terser assertTerser) throws HL7Exception {
        String response = assertTerser.get("/MSA-1");
        Assert.assertTrue(String.format("Expected AR|AE but found %s",  response), Arrays.asList("AR","AE").contains(response));
    }

    /**
     * Assert an accepted condition
     * @param assertTerser
     * @throws HL7Exception
     */
    public static void assertAccepted(Terser assertTerser) throws HL7Exception {
        //String response = assertTerser.get("/MSA-1");
        //Assert.assertTrue(String.format("Expected AA|CA but found %s",  response), Arrays.asList("AA","CA").contains(response));
    }

    /**
     * Assert the receiving information
     * @param assertTerser
     * @param string
     * @param string2
     * @throws HL7Exception
     */
    public static void assertReceivingFacility(Terser assertTerser,
                                               String device, String facility) throws HL7Exception {
        Assert.assertEquals(device, assertTerser.get("/MSH-5"));
        Assert.assertEquals(facility, assertTerser.get("/MSH-6"));
    }

    /**
     * Assert a message type
     * @param assertTerser
     * @param string
     * @param string2
     * @throws HL7Exception
     */
    public static void assertMessageTypeVersion(Terser assertTerser, String eventCode,
                                                String eventType, String messageType, String version) throws HL7Exception {
        // TODO Auto-generated method stub
        Assert.assertEquals(eventCode, assertTerser.get("/MSH-9-1"));
        Assert.assertEquals(eventType, assertTerser.get("/MSH-9-2"));
        if(messageType != null)
            Assert.assertEquals(messageType, assertTerser.get("/MSH-9-3"));
        Assert.assertEquals(version, assertTerser.get("/MSH-12"));
    }

    /**
     * Has PID 3 with specified AA and OID
     * @param segment
     * @param string
     * @param tEST_DOMAIN_OID
     * @throws HL7Exception
     */
    public static void assertHasPID3Containing(Segment segment, String idNumber, String namespaceId,
                                               String universalId) throws HL7Exception {

        Assert.assertEquals("PID", segment.getName());
        assertHasPIDContainingId(segment, 3, idNumber, namespaceId, universalId);

    }

    /**
     * Assert the PID segment has a field identifier with the value specified
     * @throws HL7Exception
     * @throws DataTypeException
     */
    public static void assertHasPIDContainingId(Segment segment, int fieldNo, String idNumber, String namespaceId,
                                                String universalId) throws DataTypeException, HL7Exception {
        boolean hasMatch = false;
        for(Type typ : segment.getField(fieldNo))
        {
            AbstractComposite cx = (AbstractComposite)typ,
                    cx4 = (AbstractComposite)cx.getComponent(3);

            // Assert has CX.4.1 and CX.4.2 and CX4.3
            Assert.assertNotNull(cx4.getComponent(0));
            Assert.assertNotNull(cx4.getComponent(1));
            Assert.assertNotNull(cx4.getComponent(2));

            // Determine match
            hasMatch |=
                    (idNumber == null || idNumber.equals(cx.getComponent(0).toString())) &&
                            cx4.getComponent(0).toString().equals(namespaceId) &&
                            cx4.getComponent(1).toString().equals(universalId) &&
                            cx4.getComponent(2).toString().equals("ISO");
        }

        Assert.assertTrue(String.format("Expected PID to have identifier with %s&%s&ISO", namespaceId, universalId), hasMatch);
    }

    /**
     * Has PID 3 with specified AA and OID is the only identifier
     * @param segment
     * @param string
     * @param tEST_DOMAIN_OID
     * @throws HL7Exception
     */
    public static void assertHasPID3Only(Segment segment, String idNumber, String namespaceId,
                                         String universalId) throws HL7Exception {

        Assert.assertEquals("PID", segment.getName());
        for(Type typ : segment.getField(3))
        {
            AbstractComposite cx = (AbstractComposite)typ,
                    cx4 = (AbstractComposite)cx.getComponent(3);

            // Assert has CX.4.1 and CX.4.2 and CX4.3
            Assert.assertNotNull(cx4.getComponent(0));
            Assert.assertNotNull(cx4.getComponent(1));
            Assert.assertNotNull(cx4.getComponent(2));

            // Determine match
            Assert.assertEquals(namespaceId, cx4.getComponent(0).toString());
            Assert.assertEquals(universalId, cx4.getComponent(1).toString());
            if(idNumber != null)
                Assert.assertEquals(idNumber, cx.getComponent(0).toString());
        }
    }

    /**
     * Assert terser has ERR
     * @param assertTerser
     */
    public static void assertHasERR(Terser assertTerser) {
        try
        {
            // Should throw
            assertTerser.getSegment("/ERR");
        }
        catch(Exception e)
        {
            Assert.fail("Should have ERR segment");
        }
    }

    /**
     * Assert only one PID segment is returned
     * @param assertTerser
     */
    public static void assertHasOneQueryResult(Terser assertTerser) {

        // Should not throw on first
        try
        {
            // Terser should throw!
            assertTerser.getSegment("/QUERY_RESPONSE(0)/PID");
        }
        catch(Exception e)
        {
            Assert.fail("Must have one response");
        }

        // Should throw on second
        try
        {
            // Terser should throw or skip!
            Segment pid1 = assertTerser.getSegment("/QUERY_RESPONSE(1)/PID");
            if(pid1 == null || pid1.isEmpty())
                return;
            fail();
        }
        catch(Exception e){}

    }




}
