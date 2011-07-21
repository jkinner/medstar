package com.sociodyne.validation.gateway;

import com.sociodyne.test.Mock;
import com.sociodyne.test.MockTest;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class AbstractGatewayInteractionTest extends MockTest {

  @Mock
  Document document;

  protected String getReceiver() {
    return "sociodyne";
  }

  protected Document createEmptyEDIDocument() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ediroot><interchange Standard=\"ANSI X.12\" Date=\"050516\" Time=\"0734\" StandardsId=\"U\" Version=\"00401\" Control=\"000005014\"><sender><address Id=\"T000000011     \" Qual=\"ZZ\"/></sender><receiver><address Id=\""
        + getReceiver()
        + "\" Qual=\"ZZ\"/></receiver><group GroupType=\"HS\" ApplSender=\"T000000011\" ApplReceiver=\""
        + getReceiver().trim()
        + "\" Date=\"20050516\" Time=\"073411\" Control=\"5014\" StandardCode=\"X\" StandardVersion=\"004010X092A1\"><transaction DocType=\"270\" Name=\"Eligibility, Coverage or Benefit Inquiry\" Control=\"000000001\"></transaction></group></interchange></ediroot>";
    return DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(new InputSource(new StringReader(xml)));
  }

  public void testRetries() throws Exception {
    final MockAbstractGatewayInteraction interaction = new MockAbstractGatewayInteraction() {

      @Override
      protected Document receiveResponse(Object is) throws IOException, GatewayInteractionException {
        super.receiveResponse(is);
        throw new IOException();
      }

    };

    replay();

    interaction.requestContext = new Object();
    interaction.responseContext = new Object();
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("username", "sociodyne");
    properties.put("password", "password");

    interaction.open(properties);
    try {
      interaction.submit(document);
      fail("Expected an IOException");
    } catch (IOException e) {
      // Expected
    }
    assertEquals(5, interaction.tries);
  }

  public class MockAbstractGatewayInteraction extends AbstractGatewayInteraction<Object, Object> {

    private Logger logger = createNiceMock(Logger.class);

    public int tries = 0;

    public Object requestContext;
    public Object responseContext;
    public Object monitor = null;
    public int teardownCount = 0;

    @Override
    protected void setUpConnection(Map<String, String> properties)
        throws GatewayConnectionException {
      this.properties = properties;
    }

    @Override
    protected Object getRequestContext() throws IOException {
      return requestContext;
    }

    @Override
    protected void sendRequest(Object requestContext, Document document) throws IOException,
        GatewayInteractionException {
      assertSame(this.requestContext, requestContext);
    }

    @Override
    protected Object getResponseContext() throws IOException {
      return responseContext;
    }

    @Override
    protected Document receiveResponse(Object is) throws IOException, GatewayInteractionException {
      tries++;
      assertSame(responseContext, is);

      if (monitor != null) {
        synchronized (monitor) {
          monitor.notifyAll();
        }
        synchronized (monitor) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            throw new GatewayInteractionException(e);
          }
        }
      }
      return document;
    }

    @Override
    protected void tearDownConnection() {
      synchronized (this) {
        teardownCount++;
      }
      properties = null;
    }

    @Override
    protected Logger getLogger() {
      return logger;
    }

//    @Override
//    public Document submit(Document object) throws Exception {
//      this.sendRequest(requestContext, document);
//      Document result = this.receiveResponse(responseContext);
//      return result;
//    }
  }
}
