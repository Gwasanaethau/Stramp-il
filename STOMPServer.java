// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>STOMPServer</code> is designed to be a simple test server for STOMP
 * messages. This should be compatible with version 1.1 of the STOMP protocol.
 * It is not intended to be a heavy-duty server.
 *
 * @author Mark David Pokorny
 * @version Déardaoin, 21ú Aibreán 2016
 * @since Déardaoin, 21ú Aibreán 2016
 */
public class STOMPServer
{

// ------------------------------------------- STOMPServer Class ---------------

  private ServerSocket socket;
  private Socket clientSocket;
  private int port;
  private PrintWriter broadcaster;

// ------------------------------------------- STOMPServer Class ---------------

  /**
   * Sets up and initialises a new STOMP server on the given port.
   *
   * @param port The port number that the server should listen on.
   */
  public STOMPServer(int port)
  {

    this.port = port;

    try
    {
      socket = new ServerSocket(port);
    } // End try

    catch (IllegalArgumentException iae)
    {
      System.err.println("Strampáil Server: \033[31mPlease specify a port" +
        " number between \033[33m0\033[31m and \033[33m65535\033[0m");
    } // End ‘IllegalArgumentException’ catch

    catch (IOException ioe)
    {
      System.err.println("Strampáil Server: \033[31mCannot connect to port" +
        " \033[36m" + port + "\033[0m. Check that something isn’t already" +
        " connected there and try again.");
    } // End ‘IOException’ catch

  } // End ‘STOMPServer(int)’ Constructor

// ------------------------------------------- STOMPServer Class ---------------

  /**
   * Instructs the server to listen for a new client connection.
   */
  public void listen()
  {

    System.out.println("Strampáil Server: " +
      "\033[34mWaiting for connections on port: \033[33m" + port + "\033[m");

    try
    {
      clientSocket = socket.accept();
      System.out.println("Strampáil Server: \033[32mClient connected!\033[0m");
      broadcaster = new PrintWriter(clientSocket.getOutputStream(), false);
    } // End try

    catch (IOException ioe)
    {
      System.err.println("Strampáil Server: " +
        "\033[31mI/O error connecting client.\033[0m");
    } // End ‘IOException’ catch

  } // End ‘listen()’ Method

// ------------------------------------------- STOMPServer Class ---------------

  /**
   * Instructs the server to disconnect its client (if present)
   * and shut itself down.
   */
  public void close()
  {

    if (clientSocket != null)
    {
      System.out.println("Strampáil Server: " +
        "\033[34mDisconnecting client.\033[0m");

      try
      {
        clientSocket.close();
      } // End try

      catch (IOException ioe)
      {
        System.err.println("Strampáil Server: " +
          "\033[31mI/O error when disconnecting client.\033[0m");
      } // End ‘IOException’ catch

    } // End if

    System.out.println("Strampáil Server: " +
      "\033[34mWinding-down server.\033[0m");
    try
    {
      socket.close();
    } // End try

    catch (IOException ioe)
    {
      System.err.println("Strampáil Server: " +
        "\033[31mI/O error when winding-down.\033[0m");
    } // End ‘IOException’ catch

    System.exit(0);

  } // End ‘close()’ Method

// ------------------------------------------- STOMPServer Class ---------------

  /**
   * Sends a STOMP message to the client.
   *
   * @param message The message to be sent to the client.
   */
  public void send(String message)
  {

    if (clientSocket == null)
      System.err.println("Strampáil Server: " +
        "\033[33mNo clients to send message to!\033[0m");
    else
    {
      System.out.println("Stramáil Server: " +
        "\033[36mSending message ‘" + message + "’ to client.\033[0m");
      broadcaster.write(message);
      broadcaster.flush();
    } // End else

  } // End ‘send(String)’ Method

// ------------------------------------------- STOMPServer Class ---------------

  /**
   * Sets up and initialises a test STOMP server on port 50005, listens for a
   * connection, sends a test message and then shuts itself down.
   *
   * @param args Not used (ignored).
   */
  public static void main(String args[])
  {

    STOMPServer server = new STOMPServer(50005);
    server.listen();
    server.send("Hi! This is a test message from the server!");
    server.send("Hallo! Dies ist einem Prüfungssatz von den Server!");
    server.close();

  } // End ‘main(String[])’ Method

// ------------------------------------------- STOMPServer Class ---------------

} // End ‘STOMPServer’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
