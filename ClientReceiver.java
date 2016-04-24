// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.InputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>ClientReceiver</code> listens for STOMP frames from the server
 * and deals with them appropriately.
 *
 * @author Mark David Pokorny
 * @version Dé Domhnaigh, 24ú Aibreán 2016
 * @since Déardaoin, 21ú Aibreán 2016
 */
class ClientReceiver extends Thread
{

// ---------------------------------------- ClientReceiver Class ---------------

  private InputStream receiver;
  private boolean active;
  private ClientInterface client;

// ---------------------------------------- ClientReceiver Class ---------------

  ClientReceiver(InputStream receiver, ClientInterface client)
  {

    this.receiver = receiver;
    this.client = client;
    active = true;

  } // End ‘ClientReceiver(InputStream, ClientInterface)’ Constructor

// ---------------------------------------- ClientReceiver Class ---------------

  public void run()
  {

    Printer.printDebug("Receiver activated!");

    while (active)
    {
      String frame = listen();
      if (frame != null)
        // FIXME: Should be threaded? Will frames from the server queue?
        parseFrame(frame);
    } // End while

    Printer.printDebug("Receiver deactivated!");

  } // End ‘run()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private String listen()
  {
    /*
     * FIXME:
     * 1MiB buffer should be _PLENTY_!
     * But really this should be tailored to the frame as
     * STOMP doesn’t limit size of headers or body,
     * but that’s just extra work at the moment! ;-Þ
     */
    byte[] partialFrame = new byte[1024];
    int index = 0;
    String frame = null;

    try
    {

      byte symbol = -2;
      while (symbol != 0 && symbol != -1) // i.e. NUL or closed stream
      {
        symbol = (byte) receiver.read();
        partialFrame[index++] = symbol;
      } // End while

      if (symbol == -1)
      {
        Printer.printInfo("Closing receiver.");
        active = false;
      } // End if
      else
      {
        frame = new String(partialFrame, 0, index - 1); // Strip out NUL
        Printer.printDebug("Frame received \033[1;35m↓\n←←←\033[0m\n" +
          frame + "\n\033[1;35m←←←\033[0m");
      } // End else

    } // End try

    catch (SocketException se)
    {
      Printer.printInfo("Closing receiver.");
      active = false;
    } // End ‘SocketException’ catch

    catch (IOException ioe)
    {
      Printer.printError("Unable to parse frame.");
      active = false;
    } // End ‘IOException’ catch

    return frame;

  } // End ‘listen()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private void parseFrame(String frame)
  {
    int indexOfLastNewline = frame.indexOf('\n');
    String command = frame.substring(0, indexOfLastNewline);

    HashMap<String, String> headers = new HashMap<String, String>();
    boolean moreHeaders = true;

    while (moreHeaders)
    {

      int indexOfNextNewline = frame.indexOf('\n', indexOfLastNewline + 1);

      if (indexOfNextNewline - indexOfLastNewline == 1)
        moreHeaders = false;
      else
      {
        int indexOfColon = frame.indexOf(":", indexOfLastNewline);
        headers.put(
          frame.substring(indexOfLastNewline + 1, indexOfColon),
          frame.substring(indexOfColon + 1, indexOfNextNewline));
      } // End else

      indexOfLastNewline = indexOfNextNewline;

    } // End while

    String body = frame.substring(indexOfLastNewline + 1);

    notifyClient(command, headers, body);

  } // End ‘parseFrame(String)’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private void notifyClient(
    String command, HashMap<String, String> headers, String body)
  {

    if (command.equals("CONNECTED"))
      client.notifyConnected();
    else if (command.equals("ERROR"))
      client.notifyError();
    else if (command.equals("RECEIPT") &&
      headers.get("receipt-id").equals("strampáilDisconnect"))
        client.notifyDisconnected();

  } // End ‘notifyClient(String, HashMap<String, String>, String)’ Method

// ---------------------------------------- ClientReceiver Class ---------------

} // End ‘ClientReceiver’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
