// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>STOMPClient</code> sets up a connection to a STOMP server in order to
 * listen to messages originating from the server. This should be compatible
 * with version 1.1 of the STOMP protocol.
 *
 * @author Mark David Pokorny
 * @version Dé Sathairn, 23ú Aibreán 2016
 * @since Déardaoin, 21ú Aibreán 2016
 */
public class STOMPClient implements Constants
{

// ------------------------------------------- STOMPClient Class ---------------

  private int port;
  private Socket socket;
  private OutputStream transmitter;
  private STOMPListener receiver;
  private boolean isSTOMPConnected, disconnectIssued;
  private String serverName, sessionID, topic;

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sets up and initialises a new STOMP client.
   *
   * @param port The port number that the client should attempt to connect to.
   */
  public STOMPClient(int port)
  {

    this.port = port;
    socket = null;
    transmitter = null;
    receiver = null;
    isSTOMPConnected = false;
    disconnectIssued = false;
    serverName = null;
    sessionID = null;

  } // End ‘STOMPClient(int)’ Constructor

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Tells the client to initiate a TCP handshake with the server.
   * This must be performed before any STOMP messages can be passed.
   *
   * @return Whether the TCP handshake concluded successfully or not.
   */
  public boolean handshake()
  {

    if (socket != null)
    {
      Printer.printWarning("TCP connection already established!");
      return false;
    } // End if

    Printer.printInfo(
      "Winding-up client and handshaking with server on port " + port);
    boolean success = false;

    try
    {
      socket = new Socket("localhost", port);
      receiver = new STOMPListener(socket.getInputStream(), this);
      receiver.start();
      transmitter = socket.getOutputStream();
      success = true;
    } // End try

    catch (UnknownHostException uhe)
    {
      Printer.printError(
        "‘localhost’ not recognised. Are you on Windows perchance?");
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

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.0 <code>CONNECT</code> frame to the server.
   *
   * @param login The login name to send to the server
   * (can be <code>null</code>) if no login required.
   * @param password The password to be sent along with the login name
   * (can be <code>null</code>) if no login required.
   * @see #stomp(String, String)
   */
  public void connect(String login, String password)
  {

    genericConnect("CONNECT", login, password);

  } // End ‘connect(String, String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.0 <code>CONNECT</code> frame to the server without any
   * login details.
   *
   * @see #connect(String, String)
   */
  public void connect()
  {

    connect(null, null);

  } // End ‘connect()’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.1 <code>STOMP</code>
   * (formerly <code>CONNECT</code>) frame to the server.
   *
   * @param login The login name to send to the server
   * (can be <code>null</code>) if no login required.
   * @param password The password to be sent along with the login name
   * (can be <code>null</code>) if no login required.
   */
  public void stomp(String login, String password)
  {

    genericConnect("STOMP", login, password);

  } // End ‘stomp(String, String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.1 <code>STOMP</code>
   * (formerly <code>CONNECT</code>) frame to the server without any
   * login details.
   *
   * @see #stomp(String, String)
   */
  public void stomp()
  {

    stomp(null, null);

  } // End ‘stomp()’ Method

// ------------------------------------------- STOMPClient Class ---------------

  private void genericConnect(String command, String login, String password)
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
    {
      Printer.printError("Cannot send " + command +
        " frame due to absence of a TCP connection." +
        " Ensure that you can connect to the server with TCP" +
        " first before trying to send STOMP messages.");
    } // End if
    else if (disconnectIssued)
    {
      Printer.printError("DISCONNECT has been sent, no more STOMP frames" +
        " are allowed to be sent!");
    } // End else if
    else if (isSTOMPConnected)
    {
      Printer.printWarning("STOMP connection already opened!");
    } // End else if
    else
    {

      StringBuilder stompFrame = new StringBuilder(
        command + "\n" + STOMP_VER + HOST_NAME);

      if (login != null && password != null)
        stompFrame.append("login:" + login + "\npasscode:" + password + "\n");

      stompFrame.append("\n\0");

      try
      {
        transmitter.write(stompFrame.toString().getBytes());
        Printer.printDebug("Sending frame \033[1;33m↓\n→→→\033[0m\n" +
          stompFrame.toString().substring(0, stompFrame.length() - 1)
          + "\n\033[1;33m→→→\033[0m");
      } // End try

      catch (IOException ioe)
      {
        Printer.printError("Cannot send " + command +
          " frame due to I/O issue.");
      } // End ‘IOException’ catch

    } // End else

  } // End ‘genericConnect(String, String, String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a <code>DISCONNECT</code> frame to the server.
   */
  public void disconnect()
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
    {
      Printer.printError("Cannot send DISCONNECT" +
        " frame due to absence of a TCP connection." +
        " Ensure that you can connect to the server with TCP" +
        " first before trying to send STOMP messages.");
    } // End if
    else if (disconnectIssued)
    {
      Printer.printError("DISCONNECT has been sent, no more STOMP frames" +
        " are allowed to be sent!");
    } // End else if
    else if (!isSTOMPConnected)
    {
      Printer.printWarning("STOMP connection already closed!");
    } // End else if
    else
    {

      String stompFrame = "DISCONNECT\nreceipt:strampáilDisconnect\n\n\0";

      try
      {
        transmitter.write(stompFrame.getBytes());
        Printer.printDebug("Sending frame \033[1;33m↓\n→→→\033[0m\n" +
          stompFrame.substring(0, stompFrame.length() - 1)
          + "\n\033[1;33m→→→\033[0m");
      } // End try

      catch (IOException ioe)
      {
        Printer.printError("Cannot send DISCONNECT frame due to I/O issue.");
      } // End ‘IOException’ catch

      isSTOMPConnected = false;
      disconnectIssued = true;

    } // End else

  } // End ‘disconnect()’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a <code>SUBSCRIBE</code> frame to the server.
   *
   * @param destination The destination topic to subscribe to.
   * @param receipt Tags whether the server should acknowledge receipt of the
   * subscription request.
   */
  public void subscribe(String destination, boolean receipt)
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
    {
      Printer.printError("Cannot send SUBSCRIBE" +
        " frame due to absence of a TCP connection." +
        " Ensure that you can connect to the server with TCP" +
        " first before trying to send STOMP messages.");
    } // End if
    else if (disconnectIssued)
    {
      Printer.printError("DISCONNECT has been sent, no more STOMP frames" +
        " are allowed to be sent!");
    } // End else if
    else if (!isSTOMPConnected)
    {
      Printer.printError("STOMP connection closed! A STOMP connection needs" +
        " to be established first.");
    } // End else if
    else if (topic != null)
    {
      Printer.printWarning("Subscription to " + topic +
        " already established!");
    } // End else if
    else
    {

      StringBuilder stompFrame = new StringBuilder(
        "SUBSCRIBE\nid:strampáil\ndestination:" + destination + "\nack:auto\n");

      if (receipt)
        stompFrame.append("receipt:strampáil\n");

      stompFrame.append("\n\0");

      try
      {
        transmitter.write(stompFrame.toString().getBytes());
        Printer.printDebug("Sending frame \033[1;33m↓\n→→→\033[0m\n" +
          stompFrame.toString().substring(0, stompFrame.length() - 1)
          + "\n\033[1;33m→→→\033[0m");
        topic = destination;
      } // End try

      catch (IOException ioe)
      {
        Printer.printError("Cannot send SUBSCRIBE frame due to I/O issue.");
      } // End ‘IOException’ catch

    } // End else

  } // End ‘subscribe(String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends an <code>UNSUBSCRIBE</code> frame to the server.
   *
   * @param receipt Tags whether the server should acknowledge receipt of the
   * unsubscription request.
   */
  public void unsubscribe(boolean receipt)
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
    {
      Printer.printError("Cannot send SUBSCRIBE" +
        " frame due to absence of a TCP connection." +
        " Ensure that you can connect to the server with TCP" +
        " first before trying to send STOMP messages.");
    } // End if
    else if (disconnectIssued)
    {
      Printer.printError("DISCONNECT has been sent, no more STOMP frames" +
        " are allowed to be sent!");
    } // End else if
    else if (!isSTOMPConnected)
    {
      Printer.printError("STOMP connection closed! A STOMP connection needs" +
        " to be established first.");
    } // End else if
    else if (topic == null)
    {
      Printer.printWarning("No subscription present – cannot unsubscribe!");
    } // End else if
    else
    {

      StringBuilder stompFrame = new StringBuilder(
        "UNSUBSCRIBE\nid:strampáil\n");

      if (receipt)
        stompFrame.append("receipt:212\n");

      stompFrame.append("\n\0");

      try
      {
        transmitter.write(stompFrame.toString().getBytes());
        Printer.printDebug("Sending frame \033[1;33m↓\n→→→\033[0m\n" +
          stompFrame.toString().substring(0, stompFrame.length() - 1)
          + "\n\033[1;33m→→→\033[0m");
        topic = null;
      } // End try

      catch (IOException ioe)
      {
        Printer.printError("Cannot send UNSUBSCRIBE frame due to I/O issue.");
      } // End ‘IOException’ catch

    } // End else

  } // End ‘subscribe(String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Confirms response from server that STOMP connection has been established.
   *
   * @param headers The headers returned by the server.
   */
  public void registerSTOMPConnection(ArrayList<String> headers)
  {

    for (int i = 0; i < headers.size(); i++)
    {
      String header = headers.get(i);
      if (header.startsWith("server"))
        serverName = header.substring(header.indexOf(":") + 1);
      else if (header.startsWith("session"))
        sessionID = header.substring(header.indexOf(":") + 1);
    } // End for

    isSTOMPConnected = true;
    Printer.printInfo("STOMP connection established.");
    Printer.printDebug("Server Name: " + serverName);
    Printer.printDebug("Session ID: " + sessionID);

  } // End ‘registerSTOMPConnection(ArrayList<String>)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Registers response from server that an ERROR has occurred.
   *
   * @param headers The headers returned by the server.
   * @param body The frame body returned by the server.
   */
  public void registerSTOMPError(ArrayList<String> headers, String body)
  {

    Printer.printError("ERROR frame received.");

    for (int i = 0; i < headers.size(); i++)
      Printer.printDebug("Header: " + headers.get(i));

    Printer.printDebug("Body: " + body);

  } // End ‘registerSTOMPError(ArrayList<String>, String body)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Instructs the client to disconnect from the server and shut itself down.
   */
  public void close()
  {

    if (socket != null && !socket.isClosed() && isSTOMPConnected)
    {
      Printer.printInfo("Disconnecting from server.");

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

  } // End ‘close()’ Method

// ------------------------------------------- STOMPClient Class ---------------

} // End ‘STOMPClient’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
