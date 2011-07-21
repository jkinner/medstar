// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.validation.gateway;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.w3c.dom.Document;

import com.google.common.collect.Sets;

/**
 * The base class for all gateway interactions, it encodes the lifecycle of the
 * interactions
 * with various validation gateways. The lifecycle is as follows:
 * 
 * <ol>
 * <li>Open the interaction</li>
 * <li>Wait until it's time to send a request</li>
 * <li>Send a request on the open interaction</li>
 * <li>(Optional) Do something else while the conversation is happening with the
 * gateway</li>
 * <li>Get the response</li>
 * <li>Repeat steps 2-5</li>
 * <li>Close the interaction</li>
 * </ol>
 * 
 * See {@link GatewayInteraction} for details about using a gateway interaction
 * as a client.
 * 
 * When implementing a gateway interaction, each step in the life cycle is
 * represented by
 * a method defined in this base class:
 * 
 * <ol>
 * <li>{@link #setUpConnection()}</li>
 * <li>...</li>
 * <li>
 * <ul>
 * <li>{@link #getOutputStream()}</li>
 * <li>{@link #sendRequest(Object, Document)}</li>
 * </ul>
 * </li>
 * <li>...</li>
 * <li>
 * <ul>
 * <li>{@link #getInputStream()}</li>
 * <li>{@link #receiveResponse(RS)}</li>
 * </ul>
 * </li>
 * <li>...</li>
 * <li>{@link #tearDownConnection()}</li>
 * </ol>
 * 
 * @author jkinner
 * 
 */
public abstract class AbstractGatewayInteraction<RQ, RS> implements GatewayInteraction {
  /* Set of component that are interested in events related to interactions. */
  private final Set<Listener> listeners = Sets.newHashSet();
  private final CompoundListener allListeners = new CompoundListener();

  private transient boolean isOpen = false;

  protected Map<String, String> properties;

  // MBean properties
  protected long serviceClientTimeout = 2 * 60 * 1000; // Two minutes
  protected int retryCount = 5;

  // Reset using "resetMetrics" MBean method
  protected long sent; // RO
  protected long received; // RO
  protected long failed; // RO
  protected long succeeded; // RO
  protected long totaltime; // RO
  protected long maxQueueLength; // RO

  public void open(Map<String, String> properties) throws Exception {
    this.properties = properties;
    setUpConnection(properties);
    isOpen = true;
  }

  public Document submit(Document document) throws Exception {
    if (!isOpen) {
      throw new IllegalStateException();
    }

    try {
      allListeners.requestStarted();
      setUpConnection(properties);

      RQ os = getRequestContext();
      Document response = null;
      long starttime = System.currentTimeMillis();
      Exception exception;
      int attempts = 0;
      final Logger logger = getLogger();
      do {
          exception = null;
          try {
            sendRequest(os, document);
          } catch (final Exception e) {
            exception = e;
          }

          try {
            final RS is = getResponseContext();
            logger.fine("Waiting for response");
            response = receiveResponse(is);
          } catch (final Exception e) {
            // Protocol exception; close the socket and force restart
            exception = e;
            allListeners.requestCompleted();
          }

          if (exception != null) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            // Try to re-establish connection and re-send; after this attempt,
            // let the exception go
            tearDownConnection();
            setUpConnection(properties);
            os = getRequestContext();
          }

          attempts++;
          logger.finer("Finished attempt " + attempts);
        } while (exception != null
            && !(exception instanceof NonRecoverableGatewayInteractionException)
            && attempts < retryCount);

        if (exception != null) {
          logger.fine("Giving up after " + attempts + " attempts with exception: "
              + exception.getMessage());
        }

        if (response != null) {
          received++;
          totaltime += System.currentTimeMillis() - starttime;
        }

        allListeners.requestCompleted();
        if (exception != null) {
          allListeners.requestFailed(exception);
          throw exception;
        }

        allListeners.requestSuccessful();
        return response;
    } catch (Exception e) {
      failed++;
      throw e;
    }
  }

  public void close() {
    properties = null;
  }
  
  public static interface MBeanInterface {

    void setServiceTimeout(long timeout);

    long getServiceTimeout();

    void setRetryCount(int retryCount);

    int getRetryCount();

    long getSent();

    long getReceived();

    long getSucceeded();

    long getFailed();

    long getAverageTime();

    void resetMetrics();
  }

  public class MBean extends StandardMBean implements MBeanInterface, Listener {
    int sent;
    int received;
    int succeeded;
    int failed;

    protected MBean(Class<?> interfaceClass) throws NotCompliantMBeanException {
      super(interfaceClass);
    }

    public MBean() throws NotCompliantMBeanException {
      super(MBeanInterface.class);
    }

    public void setServiceTimeout(long timeout) {
      serviceClientTimeout = timeout;
    }

    public long getServiceTimeout() {
      return serviceClientTimeout;
    }

    public void setRetryCount(int retryCount) {
      AbstractGatewayInteraction.this.retryCount = retryCount;
    }

    public int getRetryCount() {
      return retryCount;
    }

    public long getSent() {
      return sent;
    }

    public long getReceived() {
      return received;
    }

    public long getSucceeded() {
      return succeeded;
    }

    public long getFailed() {
      return failed;
    }

    public long getAverageTime() {
      if (received == 0) {
        return 0;
      }
      return totaltime / received;
    }

    public long getMaxQueueLength() {
      return maxQueueLength;
    }

    public void resetMetrics() {
      totaltime = sent = received = succeeded = failed = 0;
    }

    public void requestStarted() {
      sent++;
    }

    public void requestCompleted() {
      received++;
    }

    public void requestSuccessful() {
      succeeded++;
    }

    public void requestFailed(Exception exception) {
      failed++;
    }
  }

  
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  /**
   * Establishes the connection to the gateway. Assumes that the connection will
   * remain open, but
   * this method may be called multiple times if the connection is dropped and
   * needs to be
   * reestablished.
   * 
   * @throws GatewayConnectionException
   *           if there is a problem connecting to the gateway
   */
  abstract protected void setUpConnection(Map<String, String> properties)
      throws GatewayConnectionException;

  /**
   * Provides an OutputStream that is ready to receive a request.
   * 
   * @param context
   *          the request context
   * @return the output stream
   * @throws IOException
   *           if there is a problem opening the output stream
   */
  abstract protected RQ getRequestContext() throws IOException;

  /**
   * Sends a BerryWorks XML document to the specified OutputStream (probably the
   * one returned
   * by {@link #getOutputStream()}), but there is no guarantee this will be the
   * case.
   * 
   * Implementers may use the {@link #convertEDIToString(Document)} method as a
   * convenience
   * to convert the XML into EDI.
   * 
   * @param os
   *          the output stream
   * @param document
   *          the BerryWorks XML document
   * @throws IOException
   *           if there is an issue writing to the output stream
   * @throws GatewayInteractionException
   *           if there is a protocol or other error in communicating with the
   *           gateway
   */
  abstract protected void sendRequest(RQ requestContext, Document document) throws IOException,
      GatewayInteractionException;

  /**
   * Provides an InputStream the is ready to have a response read from it
   * 
   * @return the response context
   * @throws IOException
   *           if there is a problem opening the input stream.
   */
  abstract protected RS getResponseContext() throws IOException;

  /**
   * Receives a response from the InputStream and provides a
   * {@link GateWayResponse} that contains
   * either a 271 or 997 EDI response. Otherwise, an exception should be thrown.
   * This API should
   * never return null.
   * 
   * @param is the input stream
   * @return an XML representation of the response EDI.
   * @throws IOException
   *           if there is a problem with the input stream.
   * @throws GatewayInteractionException
   *           if there is a protocol or other error communicating with the
   *           gateway.
   */
  abstract protected Document receiveResponse(RS is) throws IOException,
      GatewayInteractionException;

  /**
   * Closes the connection to the gateway and releases all resources.
   */
  abstract protected void tearDownConnection();

  /**
   * Provides a logger that is used to log exceptions or errors associated with
   * the gateway interaction.
   * 
   * @return a logger
   */
  abstract protected Logger getLogger();

  private class CompoundListener implements Listener {

    public void requestStarted() {
      for (Listener listener : listeners) {
        listener.requestStarted();
      }
    }

    public void requestCompleted() {
      for (Listener listener : listeners) {
        listener.requestCompleted();
      }
    }

    public void requestSuccessful() {
      for (Listener listener : listeners) {
        listener.requestSuccessful();
      }
    }

    public void requestFailed(Exception exception) {
      for (Listener listener : listeners) {
        listener.requestFailed(exception);
      }
    }
  }
}
