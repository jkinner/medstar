package com.sociodyne.validation.gateway;

import com.sociodyne.test.gateway.TestGatewayInteraction;
import com.sociodyne.validation.gateway.CMSGatewayInteraction.CMSException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

public class CMSGatewayInteractionTest extends BaseGatewayInteractionTest<CMSGatewayInteractionTest.MockCMSGatewayInteraction> {
	private static final byte[] VALID_EMPTY_RESPONSE = new byte[] {
		1, '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', 2, 3
	};

	protected Map<String,String> createFakeProperties() {
		Map<String,String> properties = new HashMap<String,String>();
		properties.put("hostname", "localhost");
		properties.put("port", "9000");
		return properties;
	}

	@Override
	protected MockCMSGatewayInteraction createInteraction(byte[] response) {
		return new MockCMSGatewayInteraction(response);
	}


	@Override
	protected byte[] createDefaultResponse() {
		return VALID_EMPTY_RESPONSE;
	}

	

	@Override
	protected String getReceiver() {
		return "CMS            ";
	}

	@Override
	protected String extractBuffer(byte[] buffer) throws Exception {
		return extractCMSPacket(buffer);
	}


	@Override
	protected byte[] createBuffer(String string) throws Exception {
		return createCMSPacket(string);
	}


	//////////////////////////// MBean Tests //////////////////////////////////////////
	public void testMBeanSetters() throws Exception {
    replay();

		MockCMSGatewayInteraction interaction = new MockCMSGatewayInteraction(createCMSPacket(""));
		MockCMSGatewayInteraction.MBean mbean = interaction.new MBean();
		mbean.setServiceTimeout(1234L);
		assertEquals(1234L, mbean.getServiceTimeout());
	}
	
	public void testMBeanSentMetric() throws Exception {
    replay();

		MockCMSGatewayInteraction interaction = new MockCMSGatewayInteraction(createCMSPacket(""));
		MockCMSGatewayInteraction.MBean mbean = interaction.new MBean();
		interaction.addListener(mbean);
		Map<String,String> properties = createFakeProperties();

		interaction.open(properties);
		Document doc = createEmptyEDIDocument();
		interaction.submit(doc);
		assertEquals(1, mbean.getSent());
	}

	public void testMBeanFailedMetric() throws Exception {
	  replay();

		byte[] invalidPacket = createCMSPacket("");
		invalidPacket[0] = 0;
		MockCMSGatewayInteraction interaction = new MockCMSGatewayInteraction(invalidPacket);
		MockCMSGatewayInteraction.MBean mbean = interaction.new MBean();
    interaction.addListener(mbean);

		Map<String,String> properties = createFakeProperties();

		interaction.open(properties);
		Document doc = createEmptyEDIDocument();
		try {
	    interaction.submit(doc);
			fail("Expected a GatewayProtocolException");
		} catch (GatewayProtocolException e) {
			// Expected with failure
		}
		assertEquals(1, mbean.getFailed());
	}

	public void testMBeanResetMetrics() throws Exception {
    replay();

		MockCMSGatewayInteraction interaction = new MockCMSGatewayInteraction(createCMSPacket(""));
		MockCMSGatewayInteraction.MBean mbean = interaction.new MBean();
    interaction.addListener(mbean);
		
		Map<String,String> properties = createFakeProperties();

		interaction.open(properties);
		Document doc = createEmptyEDIDocument();
		interaction.submit(doc);
		assertEquals(1, mbean.getSent());
		mbean.resetMetrics();
		assertEquals(0, mbean.getSent());
	}

	public void testCMSException() throws Exception {
	  replay();

		try {
			MockCMSGatewayInteraction interaction = new MockCMSGatewayInteraction(createCMSPacket("HETS                     00000501420110114103451780 ISBY00502 - Authorization for this transaction cannot be validated"));
			CMSGatewayInteraction.MBean mbean = interaction.new MBean();
			mbean.setRetryCount(0);
			interaction.open(createFakeProperties());
			interaction.submit(createEmptyEDIDocument());
			fail("Expected an ExecutionException");
		} catch (CMSException cmse) {
			// Success!
			assertEquals("SBY00502", cmse.getCode());
			assertEquals(" - Authorization for this transaction cannot be validated", cmse.getMessage());
			assertEquals("                     000005014", cmse.getTrace());
		}
	}

	private byte[] createCMSPacket(String edi) {
		byte[] ediBytes = edi.getBytes();
		byte[] responseBytes = new byte[ediBytes.length + 13];
		System.arraycopy(ediBytes, 0, responseBytes, 12, ediBytes.length);
		StringBuffer sbLength = new StringBuffer();
		// There will always be at least 1 character (a '0' at least)
		int characters = 1;
		int length = ediBytes.length;
		while ( length > 10 ) {
			length /= 10;
			characters++;
		}

		for ( int i = 0; i < 10 - characters; i++ ) {
			sbLength.append('0');
		}
		String lengthString = sbLength.append(Integer.toString(ediBytes.length)).toString();
		System.arraycopy(lengthString.getBytes(), 0, responseBytes, 1, 10);
		responseBytes[0] = 1;
		responseBytes[11] = 2;
		responseBytes[responseBytes.length-1] = 3;
		return responseBytes;
	}

	public String extractCMSPacket(byte[] packet) throws Exception {
		assertTrue("Packet is too short", packet.length >= 12);
		assertEquals(CMSGatewayInteraction.SOH, packet[0]);
		assertEquals(CMSGatewayInteraction.STX, packet[11]);
		byte[] lengthBytes = new byte[10];
		System.arraycopy(packet, 1, lengthBytes, 0, 10);
		int length = Integer.parseInt(new String(lengthBytes));
		assertEquals(CMSGatewayInteraction.ETX, packet[12+length]);
		assertTrue("Buffer data is not the proper length", packet.length == 13 + length);
		byte[] buffer = new byte[length];
		System.arraycopy(packet, 12, buffer, 0, length);
		return new String(buffer);
	}

	public class MockCMSGatewayInteraction extends CMSGatewayInteraction implements TestGatewayInteraction {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayInputStream bais;

		public MockCMSGatewayInteraction(byte[] response) {
			bais = new ByteArrayInputStream(response);
		}

		public byte[] getRequest() {
			return baos.toByteArray();
		}


		@Override
		protected void setUpConnection(Map<String, String> properties)
				throws GatewayConnectionException {

			// No sockets in mock implementation
			;
		}

		@Override
		protected void tearDownConnection() {
			// No sockets in mock implementation
			;
		}

		
		@Override
		protected OutputStream getRequestContext() throws IOException {
			return baos;
		}

		@Override
		protected InputStream getResponseContext() throws IOException {
			return bais;
		}
	}
}
