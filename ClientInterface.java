// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>ClientInterface</code> sets up a connection to a
 * <a href="http://stomp.github.io/stomp-specification-1.1.html">STOMP</a>
 * server in order to communicate with the server. This should be compatible
 * with version 1.1 of the STOMP protocol.
 *
 * @author Mark David Pokorny
 * @version Dé Sathairn, 30ú Aibreán 2016
 * @since Déardaoin, 21ú Aibreán 2016
 */
public class ClientInterface implements Constants
{

// --------------------------------------- ClientInterface Class ---------------

  private int port, sequenceNumber, sequenceReceived;
  private Socket socket;
  private OutputStream transmitter;
  private ClientReceiver receiver;
  private boolean isSTOMPConnected, disconnectIssued, errorReceived;
  private String address, topic, id;
  private ConcurrentLinkedQueue<String> messageRepository;
  private Notifier notifier;

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sets up and initialises a new STOMP client.
   *
   * @param address The server address that this client
   * should attempt to connect to. This can be a host name or an IP address.
   * @param port The port number that this client should attempt to connect to.
   * @param debugLevel The verbosity level of messages displayed by this client.
   * @param notifier The <code>Notifier</code> to be notified when the client
   * receives a message (can be <code>null</code>).
   */
  public ClientInterface(
    String address, int port, int debugLevel, Notifier notifier)
  {

    this.address = address;
    this.port = port;
    Printer.debugLevel = debugLevel;
    this.notifier = notifier;

    sequenceNumber = 1;
    sequenceReceived = 0;
    socket = null;
    transmitter = null;
    receiver = null;
    isSTOMPConnected = false;
    disconnectIssued = false;
    errorReceived = false;
    topic = null;
    id = "strampáil";
    messageRepository = new ConcurrentLinkedQueue<String>();

  } // End ‘ClientInterface(String, int, int, Notifier)’ Constructor

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Instructs the client to initiate a TCP handshake with the server.
   * This must be performed before any STOMP messages can be passed.
   *
   * @return Whether the TCP handshake concluded successfully or not.
   */
  public boolean handshake()
  {

    boolean success = false;

    if (socket != null)
    {
      Printer.printWarning("TCP connection already established!");
      return success;
    } // End if

    Printer.printInfo(
      "Winding-up client and handshaking with server on port " + port);

    try
    {
      socket = new Socket(address, port);
      receiver = new ClientReceiver(socket.getInputStream(), this);
      receiver.start();
      transmitter = socket.getOutputStream();
      success = true;
    } // End try

    catch (UnknownHostException uhe)
    {
      Printer.printError("Address ‘" + address + "’ not recognised.");
    } // End ‘UnknownHostException’ catch

    catch (IllegalArgumentException iae)
    {
      Printer.printError("Please specify a port number between 0 and 65535");
    } // End ‘IllegalArgumentException’ catch

    catch (IOException ioe)
    {
      Printer.printError("Cannot connect to port " + port + "." +
        " Check that there’s a server running there and try again.");
    } // End ‘IOException’ catch

    return success;

  } // End ‘handshake()’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Instructs the client to disconnect from the server and shut itself down.
   */
  public void close()
  {

    if (socket != null && !socket.isClosed())
    {
      Printer.printInfo("Disconnecting from server.");

      if (topic != null)
      {
        Printer.printWarning("Still subscribed to " + topic + "!");
        unsubscribe(false);
      } // End if

      if (isSTOMPConnected)
      {
        Printer.printWarning("STOMP connection is still active!");
        disconnect();
      } // End if

      try
      {
        socket.close();
      } // End try

      catch (IOException ioe)
      {
        Printer.printError("I/O error when disconnecting from server.");
      } // End ‘IOException’ catch

    } // End if
    else
      Printer.printWarning("Connection already closed!");

    if (receiver != null)
      try { receiver.join(); } catch (InterruptedException ie) {}

    if (errorReceived)
      System.exit(1);
    else
      System.exit(0);

  } // End ‘close()’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Retrieves the first message from the repository (if any).
   *
   * @return The message (<code>null</code> if no messages found).
   */
  public String retrieveMessage()
  {
    return messageRepository.poll();
  } // End ‘retrieveMessage()’ Method

// --------------------------------------- ClientInterface Class ---------------

// ========================================= STOMP Frame Methods ===============

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a STOMP 1.0 <code>CONNECT</code> frame to the server.
   *
   * @param hostName The name of the host this
   * client should attempt to connect to.
   * @param login The login name to send to the server
   * (can be <code>null</code> if no login required).
   * @param password The password to be sent along with the login name
   * (can be <code>null</code> if no login required).
   * @return Whether the connection was successfully established or not.
   * @see #stomp(String, String, String)
   */
  public boolean connect(String hostName, String login, String password)
  {
    return genericConnect("CONNECT", hostName, login, password);
  } // End ‘connect(String, String, String)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a STOMP 1.0 <code>CONNECT</code> frame to the server without any
   * login details.
   *
   * @param hostName The name of the host this
   * client should attempt to connect to.
   * @return Whether the connection was successfully established or not.
   * @see #connect(String, String, String)
   */
  public boolean connect(String hostName)
  {
    return connect(hostName, null, null);
  } // End ‘connect(String)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a STOMP 1.1 <code>STOMP</code>
   * (formerly <code>CONNECT</code>) frame to the server.
   *
   * @param hostName The name of the host this
   * client should attempt to connect to.
   * @param login The login name to send to the server
   * (can be <code>null</code> if no login required).
   * @param password The password to be sent along with the login name
   * (can be <code>null</code> if no login required).
   * @return Whether the connection was successfully established or not.
   */
  public boolean stomp(String hostName, String login, String password)
  {
    return genericConnect("STOMP", hostName, login, password);
  } // End ‘stomp(String, String, String)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a STOMP 1.1 <code>STOMP</code>
   * (formerly <code>CONNECT</code>) frame to the server without any
   * login details.
   *
   * @param hostName The name of the host this
   * client should attempt to connect to.
   * @return Whether the connection was successfully established or not.
   * @see #stomp(String, String, String)
   */
  public boolean stomp(String hostName)
  {
    return stomp(hostName, null, null);
  } // End ‘stomp(String)’ Method

// --------------------------------------- ClientInterface Class ---------------

  private boolean genericConnect(
    String command, String hostName, String login, String password)
  {

    boolean success = false;
    if (hostName == null || hostName.equals(""))
      Printer.printError("‘" + hostName + "’ is not a valid host name.");
    else if (socket == null || !socket.isConnected() || socket.isClosed())
      Printer.printTCPError(command);
    else if (disconnectIssued)
      Printer.printDisconnectError();
    else if (isSTOMPConnected)
      Printer.printWarning("STOMP connection already opened!");
    else
    {

      StringBuilder stompFrame = new StringBuilder(
        command + "\naccept-version:1.1\nhost:" + hostName + "\n");

      if (login != null && password != null)
        stompFrame.append("login:" + login + "\npasscode:" + password + "\n");

      stompFrame.append("\n\0");

      try
      {

        transmitter.write(stompFrame.toString().getBytes());
        Printer.printSendFrame(
          stompFrame.toString().substring(0, stompFrame.length() - 1));

        while (!isSTOMPConnected && !errorReceived)
          Thread.yield();
        success = isSTOMPConnected;

      } // End try

      catch (IOException ioe)
      {
        Printer.printIOError(command);
      } // End ‘IOException’ catch

    } // End else

    return success;

  } // End ‘genericConnect(String, String, String, String)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a <code>DISCONNECT</code> frame to the server.
   */
  public void disconnect()
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
      Printer.printTCPError("DISCONNECT");
    else if (disconnectIssued)
      Printer.printDisconnectError();
    else if (!isSTOMPConnected)
      Printer.printWarning("STOMP connection already closed!");
    else
    {

      String stompFrame = "DISCONNECT\nreceipt:disconnect-" +
        sequenceNumber + "\n\n\0";

      try
      {

        transmitter.write(stompFrame.getBytes());
        Printer.printSendFrame(
          stompFrame.substring(0, stompFrame.length() - 1));

        waitForReceipt();
        isSTOMPConnected = false;
        disconnectIssued = true;

      } // End try

      catch (IOException ioe)
      {
        Printer.printIOError("DISCONNECT");
      } // End ‘IOException’ catch

    } // End else

  } // End ‘disconnect()’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a <code>SUBSCRIBE</code> frame to the server.
   *
   * @param topic The destination topic to subscribe to.
   * @param receipt Tags whether the server should acknowledge receipt of the
   * subscription request.
   * @see #subscribe(String, String, boolean)
   */
  public void subscribe(String topic, boolean receipt)
  {
    subscribe(topic, null, receipt);
  } // End ‘subscribe(String, boolean)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a <code>SUBSCRIBE</code> frame to the server.
   *
   * @param topic The destination topic to subscribe to.
   * @param id The name this client should reveal itself as.
   * If blank or <code>null</code>, a default will be used.
   * @param receipt Tags whether the server should acknowledge receipt of the
   * subscription request.
   */
  public void subscribe(String topic, String id, boolean receipt)
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
      Printer.printTCPError("SUBSCRIBE");
    else if (disconnectIssued)
      Printer.printDisconnectError();
    else if (!isSTOMPConnected)
      Printer.printSTOMPError();
    else if (this.topic != null)
      Printer.printWarning("Subscription to " + this.topic +
        " already established!");
    else
    {

      if (id != null && !id.equals(""))
        this.id = id;

      StringBuilder stompFrame = new StringBuilder(
        "SUBSCRIBE\nid:" + this.id + "\ndestination:" + topic + "\nack:auto\n");

      if (receipt)
        stompFrame.append("receipt:subscribe-" + sequenceNumber + "\n");

      stompFrame.append("\n\0");

      try
      {

        transmitter.write(stompFrame.toString().getBytes());
        Printer.printSendFrame(
          stompFrame.toString().substring(0, stompFrame.length() - 1));

        if (receipt)
          waitForReceipt();

        this.topic = topic;

      } // End try

      catch (IOException ioe)
      {
        Printer.printIOError("SUBSCRIBE");
      } // End ‘IOException’ catch

    } // End else

  } // End ‘subscribe(String, boolean)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends an <code>UNSUBSCRIBE</code> frame to the server.
   *
   * @param receipt Tags whether the server should acknowledge receipt of the
   * unsubscription request.
   */
  public void unsubscribe(boolean receipt)
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
      Printer.printTCPError("UNSUBSCRIBE");
    else if (disconnectIssued)
      Printer.printDisconnectError();
    else if (!isSTOMPConnected)
      Printer.printSTOMPError();
    else if (topic == null)
      Printer.printWarning("No subscription present – cannot unsubscribe!");
    else
    {

      StringBuilder stompFrame = new StringBuilder(
        "UNSUBSCRIBE\nid:" + id + "\n");

      if (receipt)
        stompFrame.append("receipt:unsubscribe-" + sequenceNumber + "\n");

      stompFrame.append("\n\0");

      try
      {

        transmitter.write(stompFrame.toString().getBytes());
        Printer.printSendFrame(
          stompFrame.toString().substring(0, stompFrame.length() - 1));

        if (receipt)
          waitForReceipt();

        topic = null;

      } // End try

      catch (IOException ioe)
      {
        Printer.printIOError("UNSUBSCRIBE");
      } // End ‘IOException’ catch

    } // End else

  } // End ‘unsubscribe(boolean)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Sends a <code>SEND</code> frame to the server.
   *
   * @param message The message to be sent to the server. This must be encoded
   * in UTF-8 format.
   * @param receipt Tags whether the server should acknowledge receipt of the
   * message sent.
   */
  public void send(String message, boolean receipt)
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
      Printer.printTCPError("SEND");
    else if (disconnectIssued)
      Printer.printDisconnectError();
    else if (!isSTOMPConnected)
      Printer.printSTOMPError();
    else if (topic == null)
      Printer.printError("No subscription present – cannot send message!");
    else
    {

      StringBuilder stompFrame = new StringBuilder("SEND\ndestination:" +
        topic + "\ncontent-type:text/plain\ncontent-length:" +
        message.getBytes().length + "\n");

        if (receipt)
          stompFrame.append("receipt:send-" + sequenceNumber + "\n");

        stompFrame.append("\n" + message + "\0");

      try
      {

        transmitter.write(stompFrame.toString().getBytes());
        Printer.printSendFrame(
          stompFrame.toString().substring(0, stompFrame.length() - 1));

        if (receipt)
          waitForReceipt();

      } // End try

      catch (IOException ioe)
      {
        Printer.printIOError("SEND");
      } // End ‘IOException’ catch

    } // End else

  } // End ‘send(String, boolean)’ Method

// --------------------------------------- ClientInterface Class ---------------

// ============================== Server Acknowledgement Methods ===============

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Confirms receipt of a <code>CONNECTED</code> frame.
   */
  void notifyConnected()
  {
    isSTOMPConnected = true;
    Printer.printInfo("STOMP connection established.");
  } // End ‘notifyConnected()’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Registers receipt of an <code>ERROR</code> frame.
   */
  void notifyError(String body)
  {
    Printer.printError("Error frame received:\n" + body);
    errorReceived = true;
    close();
  } // End ‘notifyError(String)’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Registers receipt of a <code>RECEIPT</code> frame.
   */
  void notifyReceipt(int sequenceNumber)
  {
    sequenceReceived = sequenceNumber;
    while (sequenceReceived != 0)
      Thread.yield();
  } // End ‘notifyReceipt()’ Method

// --------------------------------------- ClientInterface Class ---------------

  /**
   * Registers receipt of a <code>MESSAGE</code> frame. The message is stored
   * in the retrieval system and the {@link Notifier} alerted.
   */
  void notifyMessage(String message)
  {
    messageRepository.add(message);
    if (notifier != null)
      notifier.alert();
  } // End ‘notifyMessage(String)’ Method

// --------------------------------------- ClientInterface Class ---------------

// ====================================== Private Helper Methods ===============

// --------------------------------------- ClientInterface Class ---------------

  private void waitForReceipt()
  {

    Printer.printInfo("Waiting for receipt " + sequenceNumber + ".");
    while (sequenceReceived != sequenceNumber && !errorReceived)
      Thread.yield();

    if (sequenceReceived == sequenceNumber)
    {
      Printer.printInfo("Receipt " + sequenceNumber + " received.");
      sequenceNumber++;
      sequenceReceived = 0;
    } // End else if

  } // End ‘waitForReceipt()’ Method

// --------------------------------------- ClientInterface Class ---------------

} // End ‘ClientInterface’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
