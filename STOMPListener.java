// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.InputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>STOMPListener</code> listens to transmissions from the remote end
 * and (currently) displays them on the CLI.
 *
 * @author Mark David Pokorny
 * @version Dé Sathairn, 23ú Aibreán 2016
 * @since Dé hAoine, 22ú Aibreán 2016
 */
class STOMPListener extends Thread
{

// ----------------------------------------- STOMPListener Class ---------------

  private InputStream receiver;
  private boolean active;
  private STOMPClient client;

// ----------------------------------------- STOMPListener Class ---------------

  STOMPListener(InputStream receiver, STOMPClient client)
  {

    this.receiver = receiver;
    this.client = client;
    active = true;

  } // End ‘STOMPListener(InputStream)’ Constructor

// ----------------------------------------- STOMPListener Class ---------------

  public void run()
  {

    Printer.printDebug("Stub listener activated!");

    while (active)
    {
      String serverMessage = listen();
      if (serverMessage != null)
        parseMessage(serverMessage);
    } // End while

    Printer.printDebug("Thread exited!");

  } // End ‘run()’ Method

// ----------------------------------------- STOMPListener Class ---------------

  private String listen()
  {

    byte[] partialMessage = new byte[1024];
    int count = 0;
    String message = null;

    try
    {
      while (true)
      {
        byte symbol = (byte) receiver.read();

        if (symbol == 0)
          break;
        else if (symbol == -1)
        {
          active = false;
          return message;
        } // End else if

        partialMessage[count++] = symbol;
      } // End while

      message = new String(partialMessage, 0, count);
      Printer.printDebug("Message received \033[1;35m↓\n←←←\033[0m\n" +
        message + "\033[1;35m←←←\033[0m");
    } // End try

    catch (SocketException se)
    {
      Printer.printInfo("Closing receiver.");
      active = false;
    } // End ‘SocketException’ catch

    catch (IOException ioe)
    {
      Printer.printError("Unable to parse message.");
      ioe.printStackTrace();
      active = false;
    } // End ‘IOException’ catch

    return message;

  } // End ‘listen()’ Method

// ----------------------------------------- STOMPListener Class ---------------

  private void parseMessage(String message)
  {
    StringBuilder command = new StringBuilder();
    int index = 0;
    while (true)
    {
      char character = message.charAt(index++);
      if (character == '\n')
        break;
      command.append(character);
    } // End while

    HashMap<String, String> headers = new HashMap<String, String>();
    while (true)
    {
      StringBuilder key = new StringBuilder();
      while (true)
      {
        char character = message.charAt(index);
        if (character == ':' || character == '\n')
          break;
        key.append(character);
        index++;
      } // End while
      StringBuilder value = new StringBuilder();
      while (true)
      {
        char character = message.charAt(index++);
        if (character == '\n')
          break;
        value.append(character);
      } // End while
      if (key.length() == 0) // i.e. Two \n’s received in a row…
        break;
      headers.put(key.toString(), value.toString());
    } // End while

    StringBuilder body = new StringBuilder();
    while (true)
    {
      if (index >= message.length())
        break;
      char character = message.charAt(index++);
      body.append(character);
    } // End while

    if (command.toString().equals("CONNECTED"))
      client.registerSTOMPConnection(headers);

  } // End ‘parseMessage(String)’ Method

// ----------------------------------------- STOMPListener Class ---------------

} // End ‘STOMPListener’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
