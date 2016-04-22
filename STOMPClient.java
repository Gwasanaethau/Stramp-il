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

  private static final String PROG_NAME = "Strampáil Client";

  private Socket socket;
  private int port;
  private boolean tcpConnected;
  private InputStream receiver;
  private OutputStream transmitter;

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Sets up and initialises a new STOMP client and attempts to connect to a
   * server on the given port.
   *
   * @param port The port number that the server should listen on.
   */
  public STOMPClient(int port)
  {

    System.out.println(PROG_NAME + ": " +
      "Winding-up client and connecting to server on port: \033[36m" +
      port + "\033[0m");
    this.port = port;
    tcpConnected = false;

    try
    {
      socket = new Socket("localhost", port);
      receiver = socket.getInputStream();
      transmitter = socket.getOutputStream();
      tcpConnected = true;
    } // End try

    catch (UnknownHostException uhe)
    {
      System.err.println(PROG_NAME + ": \033[31m‘localhost’ not" +
        " recognised. Are you on Windows perchance?\033[0m");
    } // End ‘UnknownHostException’ catch

    catch (IllegalArgumentException iae)
    {
      System.err.println(PROG_NAME + ": \033[31mPlease specify a port" +
        " number between \033[1m0\033[0;31m and \033[1m65535\033[0m");
    } // End ‘IllegalArgumentException’ catch

    catch (IOException ioe)
    {
      System.err.println(PROG_NAME + ": \033[31mCannot connect to port" +
        " \033[1m" + port + "\033[0;31m. Check that there’s a server running" +
        " there and try again.\033[0m");
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

    if (!tcpConnected)
    {

      System.err.println(PROG_NAME + ": \033[31mCannot send" +
        " \033[1m" + command + "\033[0;31m command due to no TCP connection" +
        ". Ensure that you can connect to the server with TCP first!\033[0m");
      return;

    } // End if

    StringBuilder stompFrame = new StringBuilder(
      command + "\n" + /*STOMP_VER +*/ HOST_NAME);

    if (login != null && password != null)
      stompFrame.append("login:" + login + "\npasscode:" + password + "\n");

    stompFrame.append("\n\0");

    try
    {
      transmitter.write(stompFrame.toString().getBytes());
    } // End try

    catch (IOException ioe)
    {
      System.err.println(PROG_NAME + ": \033[31mCannot send" +
        "\033[1m" + command + "\033[0;31m command.\033[0m");
      this.close();
    } // End ‘IOException’ catch

  } // End ‘command(String, String, String)’ Method

// ------------------------------------------- STOMPClient Class ---------------

  // /**
  //  * Instructs the client to disconnect from the server and shut itself down.
  //  */
  // public String receive()
  // {
  //
  //   StringBuilder partialMessage = new StringBuilder();
  //
  //   try
  //   {
  //     while (true)
  //     {
  //       char character = (char) receiver.read();
  //
  //       if (character == '\0')
  //         break;
  //
  //       partialMessage.append(character);
  //     } // End while
  //   } // End try
  //
  //   catch (IOException ioe)
  //   {
  //     System.err.println(PROG_NAME + ": " +
  //       "\033[31mUnable to parse message.\033[0m");
  //   } // End ‘IOException’ catch
  //
  //   return partialMessage.toString();
  //
  // } // End ‘receive()’ Method

// ------------------------------------------- STOMPClient Class ---------------

  /**
   * Instructs the client to disconnect from the server and shut itself down.
   */
  public void close()
  {

    if (socket != null)
    {
      System.out.println(PROG_NAME + ": Disconnecting from server.");

      try
      {
        socket.close();
        tcpConnected = false;
      } // End try

      catch (IOException ioe)
      {
        System.err.println(PROG_NAME + ": " +
          "\033[31mI/O error when disconnecting from server.\033[0m");
      } // End ‘IOException’ catch

    } // End if

    System.exit(0);

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
    //client.stomp();
    client.close();

  } // End ‘main(String[] args)’ Method

// ------------------------------------------- STOMPClient Class ---------------

} // End ‘STOMPClient’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
