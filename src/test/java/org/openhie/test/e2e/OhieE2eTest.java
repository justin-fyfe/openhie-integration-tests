package org.openhie.test.e2e;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.ihe.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryRequest;
import org.dcm4chee.xds2.infoset.rim.AdhocQueryResponse;
import org.dcm4chee.xds2.infoset.rim.RegistryResponseType;
import org.junit.Test;
import org.openhie.test.cr.OhieCrIntegrationTest;
import org.openhie.test.cr.util.CrMessageUtil;
import org.openhie.test.xds.util.XdsMessageUtil;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

/**
 * OpenHIE E2E tests
 * @author Justin
 *
 */
public class OhieE2eTest {

	/**
	 * Test basic case
	 * @throws IOException 
	 * @throws HL7Exception 
	 * @throws LLPException 
	 * @throws JAXBException 
	 */
	@Test
	public void testSuccessfulProvideAndRetrieve() throws HL7Exception, IOException, LLPException, JAXBException {
		
		// STEP 10 & 20 : Run e2e on PIX
		Message step10 = CrMessageUtil.loadMessage("OHIE-CR-11-10"),
				step20 = CrMessageUtil.loadMessage("OHIE-CR-11-20");

		// STEP 10 - Send to PIX
		Message response = CrMessageUtil.sendMessage(step10);
		Terser assertTerser = new Terser(response);
		CrMessageUtil.assertAccepted(assertTerser);
		
		// STEP 20 - Query PIX to verify creation
		response = CrMessageUtil.sendMessage(step20);
		assertTerser = new Terser(response);
		CrMessageUtil.assertAccepted(assertTerser);
		CrMessageUtil.assertHasOneQueryResult(assertTerser);
		CrMessageUtil.assertHasPID3Containing(assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), "RJ-439", "TEST", OhieCrIntegrationTest.TEST_DOMAIN_OID);
		
		// STEP 30 - Load the XDS PnR
		
		String documentId = String.format("1.3.6.1.4.1.21367.2010.1.2.%s", new SimpleDateFormat("HHmmss").format(new Date()));
		ProvideAndRegisterDocumentSetRequestType pnrRequest = XdsMessageUtil.loadMessage("OHIE-XDS-01-10", ProvideAndRegisterDocumentSetRequestType.class);
		XdsMessageUtil.updatePnrPatientInfo(pnrRequest, assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"), documentId);
		RegistryResponseType xdsResponse = XdsMessageUtil.provideAndRegister(pnrRequest);
		XdsMessageUtil.assertSuccess(xdsResponse);
		
		// STEP 40 - Load the XDS query and perform a query

  		AdhocQueryRequest queryRequest = XdsMessageUtil.loadMessage("OHIE-XDS-01-20", AdhocQueryRequest.class);
		XdsMessageUtil.updateAdhocQueryRequest(queryRequest, assertTerser.getSegment("/QUERY_RESPONSE(0)/PID"));
		AdhocQueryResponse queryResponse = XdsMessageUtil.registryStoredQuery(queryRequest);
		XdsMessageUtil.assertSuccess(queryResponse);
		XdsMessageUtil.assertHasDocumentId(queryResponse, documentId);

		// STEP 50 - Retrieve
		RetrieveDocumentSetRequestType retrieveRequest =  XdsMessageUtil.createRetrieveRequest(documentId, queryResponse);
		RetrieveDocumentSetResponseType retrieveResponse = XdsMessageUtil.retrieveDocumentSet(retrieveRequest);
		assertEquals(1, retrieveResponse.getDocumentResponse().size());
		XdsMessageUtil.assertMatchesHash(queryResponse, retrieveResponse);
	}

}
