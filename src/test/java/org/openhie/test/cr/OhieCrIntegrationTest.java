package org.openhie.test.cr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PreDestroy;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openhie.test.cr.util.CrMessageUtil;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.RSP_K23;
import ca.uhn.hl7v2.util.Terser;

/**
 * OHIE-CR-01
 * This test validates that the Client Registry rejects a poorly formed message lacking appropriate assigner information in PID-3.
 *
 * @author Justin
 */
public class OhieCrIntegrationTest {

    private final Log log = LogFactory.getLog(this.getClass());

    public static final String TEST_DOMAIN_OID = "2.16.840.1.113883.3.72.5.9.1";
    public static final String TEST_A_DOMAIN_OID = "2.16.840.1.113883.3.72.5.9.2";
    /**
     * Setup the patients, etc
     */

    /**
     @BeforeClass public static void setup()
     {

     try
     {
     // Test 5 step 10 must have this user configured
     Message ohieCr05Step10 = CrMessageUtil.loadMessage("OHIE-CR-05-10");
     Message response = CrMessageUtil.sendMessage(ohieCr05Step10);
     Terser responseTerser = new Terser(response);
     //CrMessageUtil.assertAccepted(responseTerser);

     }
     catch(Exception e)
     {
     e.printStackTrace();
     //log.error(e);
     fail();
     }

     }

     **/

    /**
     * OHIE-CR-02
     * This test validates that the Client Registry is capable of populating the CX.4.1 from CX.4.2 and CX.4.3 or vice-versa given partial data in the CX.4 field.
     */

    @Test
    public void OhieCr02() {

        try {
            Message
                    step20 = CrMessageUtil.loadMessage("OHIE-CR-01-01");

            // STEP 20
            Message response = CrMessageUtil.sendMessage(step20);
            Terser assertTerser = new Terser(response);
            System.out.println("Response:" + response.toString());
            // CrMessageUtil.assertAccepted(assertTerser);
            // CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
            // CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K23", "RSP_K23", "2.5");
            try {
                // Terser should throw!

                assertTerser.getSegment("/QUERY_RESPONSE(1)/PID");
                fail();
            } catch (Exception e) {
            }
            //CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-438", "TEST", TEST_DOMAIN_OID);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
            fail();
        }
    }

}
