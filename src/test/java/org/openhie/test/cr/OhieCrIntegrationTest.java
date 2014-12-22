package org.openhie.test.cr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PreDestroy;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhie.test.cr.util.CrMessageUtil;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

/**
 * OHIE-CR-01
 * This test validates that the Client Registry rejects a poorly formed message lacking appropriate assigner information in PID-3.
 * @author Justin
 *
 */
public class OhieCrIntegrationTest {

	private final Log log = LogFactory.getLog(this.getClass());

	public static final String TEST_DOMAIN_OID = "2.16.840.1.113883.3.72.5.9.1";
	public static final String TEST_A_DOMAIN_OID = "2.16.840.1.113883.3.72.5.9.2";
	/**
	 * Setup the patients, etc
	 */
	@BeforeClass
	public static void setup()
	{
		
		try
		{
			// Test 5 step 10 must have this user configured
			Message ohieCr05Step10 = CrMessageUtil.loadMessage("OHIE-CR-05-10");
			Message response = CrMessageUtil.sendMessage(ohieCr05Step10);
			Terser responseTerser = new Terser(response);
			CrMessageUtil.assertAccepted(responseTerser);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//log.error(e);
			fail();
		}
		
	}
	
	/**
	 * OHIE-CR-01
	 * This test validates that the Client Registry rejects a poorly formed message lacking appropriate assigner information in PID-3.
	 */
	@Test()
	public void OhieCr01() {
		
		try
		{
			Message request = CrMessageUtil.loadMessage("OHIE-CR-01-10");
			Message response = CrMessageUtil.sendMessage(request);
			
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertRejected(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();
		}
	}

	/**
	 * OHIE-CR-02
	 * This test validates that the Client Registry is capable of populating the CX.4.1 from CX.4.2 and CX.4.3 or vice-versa given partial data in the CX.4 field.
	 */
	@Test
	public void OhieCr02() {
	
		try
		{
			Message step10 = CrMessageUtil.loadMessage("OHIE-CR-02-10"),
					step20 = CrMessageUtil.loadMessage("OHIE-CR-02-20"),
					step30 = CrMessageUtil.loadMessage("OHIE-CR-02-30"),
					step40 = CrMessageUtil.loadMessage("OHIE-CR-02-40");
			
			// STEP 10
			Message response = CrMessageUtil.sendMessage(step10);
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS","TEST");
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			
			// STEP 20
			response = CrMessageUtil.sendMessage(step20);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K23", "RSP_K23", "2.5");
			try
			{
				// Terser should throw!
				
				assertTerser.getSegment("/QUERY_RESPONSE(1)/PID");
				fail();
			}
			catch(Exception e){}
			CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-438", "TEST", TEST_DOMAIN_OID);
			
			// STEP 30
			response = CrMessageUtil.sendMessage(step30);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS","TEST");
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			
			// STEP 40
			response = CrMessageUtil.sendMessage(step40);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K23", "RSP_K23", "2.5");
			CrMessageUtil.assertHasOneQueryResult(assertTerser);
			CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-439", "TEST", TEST_DOMAIN_OID);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();
		}
	}
	
	/**
	 * OHIE-CR-03
	 * This test ensures that the receiver rejects messages which contain identifiers assigned from authorities which are unknown.
	 */
	@Test
	public void OhieCr03()
	{
		try
		{
			
			Message step10 = CrMessageUtil.loadMessage("OHIE-CR-03-10"),
					step20 = CrMessageUtil.loadMessage("OHIE-CR-03-20");
			
			// Step 10
			Message response = CrMessageUtil.sendMessage(step10);
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertRejected(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			CrMessageUtil.assertHasERR(assertTerser);
			
			// Step 20
			response = CrMessageUtil.sendMessage(step20);
			assertTerser = new Terser(response);
			CrMessageUtil.assertRejected(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			CrMessageUtil.assertHasERR(assertTerser);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();
		}
	}
	
	/**
	 * OHIE-CR-04
	 * This test ensures that two assigning authorities cannot assign identifiers from the other�s assigning domain. In this test, the harness mimics two authorities (TEST_HARNESS_A and TEST_HARNESS_B). They each register a patient and the harness then verifies that TEST_HARNESS_B does not assign an identifier from TEST_HARNESS_A�s identity domain
	 */
	@Test
	public void OhieCr04()
	{
		try
		{
			Message step20 = CrMessageUtil.loadMessage("OHIE-CR-04-20"),
					step30 = CrMessageUtil.loadMessage("OHIE-CR-04-30");
			
			Message response = CrMessageUtil.sendMessage(step20);
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS_A", "TEST");
			
			response = CrMessageUtil.sendMessage(step30);
			assertTerser = new Terser(response);
			CrMessageUtil.assertRejected(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS_B", "TEST");
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			CrMessageUtil.assertHasERR(assertTerser);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();
		}
	}
	
	/**
	 * OHIE-CR-05
	 * This test ensures that the receiver does not reject a message containing only an identifier, and one of gender, date of birth, mother�s identifier. This test makes no assertion about merging/matching patients
	 */
	@Test
	public void OhieCr05()
	{
		try
		{

			// Load message
			Message step20 = CrMessageUtil.loadMessage("OHIE-CR-05-20"),
					step30 = CrMessageUtil.loadMessage("OHIE-CR-05-30");
			
			Message response = CrMessageUtil.sendMessage(step20);
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			
			// Verify
			response = CrMessageUtil.sendMessage(step30);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K23", "RSP_K23", "2.5");
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertHasOneQueryResult(assertTerser);
			CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-441", "TEST", TEST_DOMAIN_OID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();
		}
	}
	
	/**
	 * OHIE-CR-06
	 * This test ensures that the receiver is able to merge patient data from an assigning authority (TEST_A) which has an national patient identifier (assigning authority NID). The demographics data does not match, this is to test that matching is done on explicit identifiers.
	 */
	@Test
	public void OhieCr06()
	{
		try
		{
			Message step20 = CrMessageUtil.loadMessage("OHIE-CR-06-20"),
					step30 = CrMessageUtil.loadMessage("OHIE-CR-06-30"),
					step40 = CrMessageUtil.loadMessage("OHIE-CR-06-40");
			
			Message response = CrMessageUtil.sendMessage(step20);
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			
			// Register should link
			response = CrMessageUtil.sendMessage(step30);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS_A", "TEST");
			
			// Query
			response = CrMessageUtil.sendMessage(step40);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K23", "RSP_K23", "2.5");
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS_A", "TEST");
			CrMessageUtil.assertHasOneQueryResult(assertTerser);
			CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-449", "TEST_A", TEST_A_DOMAIN_OID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();			
		}
	}
	
	/**
	 * This test ensures that the receiver is able to match an incoming patient with their mother via the �Mother�s Identifier� property. In this test, the harness with register a patient (the mother) and subsequently will register an infant record (only dob, gender and id) with the mother�s identifier attached. The test will ensure that the link occurred by validating a demographic query contains the mother�s name.
	 */
	@Test
	public void OhieCr07()
	{
		try
		{
			Message step10 = CrMessageUtil.loadMessage("OHIE-CR-07-10"),
					step20 = CrMessageUtil.loadMessage("OHIE-CR-07-20"),
					step30 = CrMessageUtil.loadMessage("OHIE-CR-07-30"),
					step40 = CrMessageUtil.loadMessage("OHIE-CR-07-40");
			
			// Step 10 - Setup
			Message response = CrMessageUtil.sendMessage(step10);
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			
			// Step 20 - Send ADT Message with min data
			response = CrMessageUtil.sendMessage(step20);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			
			// Step 30 - Verify Creation in the receiver
			response = CrMessageUtil.sendMessage(step30);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K23", "RSP_K23", "2.5");
			CrMessageUtil.assertHasOneQueryResult(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-440", "TEST", TEST_DOMAIN_OID);
			
			// Step 40 - Harness validates linking
			response = CrMessageUtil.sendMessage(step40);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K22", "RSP_K21", "2.5");
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-440", "TEST", TEST_DOMAIN_OID);
			assertEquals("JONES", assertTerser.get("/QUERY_RESPONSE(0)/PID-6-1"));
			assertEquals("JENNIFER", assertTerser.get("/QUERY_RESPONSE(0)/PID-6-2"));
			CrMessageUtil.assertHasPIDContainingId(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), 21, "RJ-439", "TEST", TEST_DOMAIN_OID);
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();
		}
	}
	
	/**
	 * This test ensures that the receiver is able to store and usefully convey (regurgitate) a more complete patient record having multiple names, addresses, telephone numbers, mother�s identifier, mother�s name, birth date, multiple birth order, etc.
	 */
	@Test
	public void OhieCr08() 
	{
		try
		{
			Message step10 = CrMessageUtil.loadMessage("OHIE-CR-08-10"),
					step30 = CrMessageUtil.loadMessage("OHIE-CR-08-30");
			
			// Step 10 - Setup
			Message response = CrMessageUtil.sendMessage(step10);
			Terser assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "ACK", "A01", null, "2.3.1");
			
			// Step 30 - Verify Creation in the receiver
			response = CrMessageUtil.sendMessage(step30);
			assertTerser = new Terser(response);
			CrMessageUtil.assertAccepted(assertTerser);
			CrMessageUtil.assertMessageTypeVersion(assertTerser, "RSP", "K22", "RSP_K21", "2.5");
			CrMessageUtil.assertHasOneQueryResult(assertTerser);
			CrMessageUtil.assertReceivingFacility(assertTerser, "TEST_HARNESS", "TEST");
			CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-442", "TEST", TEST_DOMAIN_OID);
			assertEquals("FOSTER", assertTerser.get("/QUERY_RESPONSE(0)/PID-5-1"));
			assertEquals("FANNY", assertTerser.get("/QUERY_RESPONSE(0)/PID-5-2"));
			assertEquals("FULL", assertTerser.get("/QUERY_RESPONSE(0)/PID-5-3"));
			assertEquals("FOSTER", assertTerser.get("/QUERY_RESPONSE(0)/PID-6-1"));
			assertEquals("MARY", assertTerser.get("/QUERY_RESPONSE(0)/PID-6-2"));
			assertEquals("1970", assertTerser.get("/QUERY_RESPONSE(0)/PID-7"));
			assertEquals("F", assertTerser.get("/QUERY_RESPONSE(0)/PID-8"));
			assertEquals("123 W34 St", assertTerser.get("/QUERY_RESPONSE(0)/PID-11-1"));
			assertEquals("FRESNO", assertTerser.get("/QUERY_RESPONSE(0)/PID-11-3"));
			assertEquals("CA", assertTerser.get("/QUERY_RESPONSE(0)/PID-11-4"));
			assertEquals("3049506", assertTerser.get("/QUERY_RESPONSE(0)/PID-11-5"));
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
			fail();
		}
	}
	
	/**
	 * In this test, the test harness will register a patient with a local identifier (TEST domain) and will subsequently query the receiver to retrieve the identifiers linked to the newly registered patient.
	 */
	@Test
	public void OhieCr09()
	{
		
	}
	
	/**
	 * In this test, the test harness will register a patient with a local identifier (TEST domain). The receiver is the assigning authority for the ECID domain and should generate an ECID by whatever means the software performs this task. The test harness will then ask the receiver to do a cross reference between the TEST domain and the ECID domain. This test ensures that the receiver adheres to the �What Domains Returned� query parameter.
	 */
	@Test
	public void OhieCr10()
	{
		
	}
}
