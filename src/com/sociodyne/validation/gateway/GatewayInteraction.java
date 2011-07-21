package com.sociodyne.validation.gateway;

import java.io.Closeable;
import java.util.Map;

import org.w3c.dom.Document;

public interface GatewayInteraction extends Closeable {

  /**
   * Begin a gateway interaction. An interaction can process one or multiple
   * documents. Based
   * on the configuration of the gateway interaction, the documents may be
   * processed in real-time
   * or in a batch.
   * 
   * @param properties
   *          Connection properties for the gateway. Not all properties may be
   *          used
   *          by this interaction
   */
  void open(Map<String, String> properties) throws Exception;

  /**
   * Submits a document for processing. The result of processing MAY be
   * available only after
   * the {@link #close()} method is called.
   * 
   * @param document
   *          An EDI/XML document to be submitted to the gateway
   */
  Document submit(Document document) throws Exception;

  /**
   * Finalizes the gateway interaction. Results MAY be available ONLY after this
   * method is called.
   */
  void close();

  void addListener(Listener listener);
  
  void removeListener(Listener listener);

  public interface Listener {
    void requestStarted();
    void requestCompleted();
    void requestSuccessful();
    void requestFailed(Exception exception);
  }
}
