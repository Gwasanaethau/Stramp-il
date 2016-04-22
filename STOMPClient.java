// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>STOMPClient</code> sets up a connection to a STOMP server in order to
 * listen to messages originating from the server. This should be compatible
 * with version 1.1 of the STOMP protocol.
 *
 * @author Mark David Pokorny
 * @version Dé hAoine, 22ú Aibreán 2016
 * @since Déardaoin, 21ú Aibreán 2016
 */
public class STOMPClient implements Constants
{

// ------------------------------------------- STOMPClient Class ---------------

  private Socket socket;
  private int port;
  private OutputStream transmitter;
  private STOMPListener receiver;

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sets up and initialises a new STOMP client and attempts to connect to a
   * server on the given port.
   *
   * @param port The port number that the server should listen on.
   */
  public STOMPClient(int port)
  {

    Printer.printInfo("Winding-up client and connecting to server on port " + port);
    this.port = port;

    try
    {
      socket = new Socket("localhost", port);
      receiver = new STOMPListener(socket.getInputStream());
      receiver.start();
      transmitter = socket.getOutputStream();
    } // End try

    catch (UnknownHostException uhe)
    {
      Printer.printError("‘localhost’ not recognised. Are you on Windows perchance?");
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

  } // End ‘STOMPClient(int)’ Constructor

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.0 <code>CONNECT</code> command to the server.
   *
   * @param login The login name to send to the server
   * (can be <code>null</code>) if no login required.
   * @param password The password to be sent along with the login name
   * (can be <code>null</code>) if no login required.
   * @see #stomp(String, String)
   */
  public void connect(String login, String password)
  {

    this.command("CONNECT", login, password);

  } // End ‘connect(String, String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.0 <code>CONNECT</code> command to the server without any
   * login details.
   *
   * @see #connect(String, String)
   */
  public void connect()
  {

    this.connect(null, null);

  } // End ‘connect()’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.1 <code>STOMP</code>
   * (formerly <code>CONNECT</code>) command to the server.
   *
   * @param login The login name to send to the server
   * (can be <code>null</code>) if no login required.
   * @param password The password to be sent along with the login name
   * (can be <code>null</code>) if no login required.
   */
  public void stomp(String login, String password)
  {

    this.command("STOMP", login, password);

  } // End ‘stomp(String, String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sends a STOMP 1.1 <code>STOMP</code>
   * (formerly <code>CONNECT</code>) command to the server without any
   * login details.
   *
   * @see #stomp(String, String)
   */
  public void stomp()
  {

    this.stomp(null, null);

  } // End ‘stomp()’ Method

// ------------------------------------------- STOMPClient Class ---------------

  private void command(String command, String login, String password)
  {

    if (socket == null || !socket.isConnected() || socket.isClosed())
    {
      Printer.printError("Cannot send " + command + " command due to absence of a" +
        " TCP connection. Ensure that you can connect to the server with TCP" +
        " first before trying to send STOMP messages.");
      return;
    } // End if

    StringBuilder stompFrame = new StringBuilder(
      command + "\n" + STOMP_VER + HOST_NAME);

    if (login != null && password != null)
      stompFrame.append("login:" + login + "\npasscode:" + password + "\n");

    stompFrame.append("\n\0");

    try
    {
      transmitter.write(stompFrame.toString().getBytes());
      Printer.printDebug("Sending command \033[1;33m↓\n→→→\033[0m\n" +
        stompFrame.toString().substring(0, stompFrame.length() - 1)
        + "\033[1;33m→→→\033[0m");
    } // End try

    catch (IOException ioe)
    {
      Printer.printError("Cannot send " + command + " command due to I/O issue.");
      this.close();
    } // End ‘IOException’ catch

  } // End ‘command(String, String, String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Instructs the client to disconnect from the server and shut itself down.
   */
  public void close()
  {

    if (socket != null && !socket.isClosed())
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

  /**
   * Sets up and initialises a test STOMP client, sends a request to port 50005
   * for a connection, receives test messages and then shuts itself down.
   *
   * @param args Not used (ignored).
   */
  public static void main(String args[])
  {

    STOMPClient client = new STOMPClient(51515);
    client.connect();
    try {Thread.sleep(100);}catch(Throwable t){t.printStackTrace();}
    //client.stomp();
    client.close();
    client.close();

  } // End ‘main(String[] args)’ Method

// ------------------------------------------- STOMPClient Class ---------------

} // End ‘STOMPClient’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
