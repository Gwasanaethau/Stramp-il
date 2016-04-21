// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
 * @version Déardaoin, 21ú Aibreán 2016
 * @since Déardaoin, 21ú Aibreán 2016
 */
public class STOMPClient
{

// ------------------------------------------- STOMPClient Class ---------------

  private static final String PROG_NAME = "Strampáil Client";

  private Socket socket;
  private int port;
  private BufferedReader receiver;

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

    try
    {
      socket = new Socket("localhost", port);
      receiver = new BufferedReader(
        new InputStreamReader(socket.getInputStream()));
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

    STOMPClient client = new STOMPClient(50005);
    client.close();

  } // End ‘main(String[] args)’ Method

// ------------------------------------------- STOMPClient Class ---------------

} // End ‘STOMPClient’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
