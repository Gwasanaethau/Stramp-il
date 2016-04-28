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
 * @version Déardaoin, 28ú Aibreán 2016
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
      listen();

    Printer.printDebug("Receiver deactivated!");

  } // End ‘run()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private void listen()
  {

    try
    {
      StringBuilder debugMessages = new StringBuilder(
        "Frame received \033[1;35m↓\n←←←\033[0m\n");

      // Finite automata state 1:
      String command;
      byte[] commandBytes = new byte[12]; // Length of ‘U-N-S-U-B-S-C-R-I-B-E-\n’
      byte index = 0;

      byte symbol = (byte) receiver.read();
      while (true)
      {
        if (symbol == -1)
        {
          Printer.printWarning("Connection closed from remote end.");
          active = false;
          return;
        } // End if
        else if (symbol == 0)
        {
          Printer.printError("Malformed STOMP frame received.");
          return;
        } // End else if
        else if (symbol == '\n')
          break;
        else
        {
          commandBytes[index++] = symbol;
          symbol = (byte) receiver.read();
        } // End else

      } // End while

      command = new String(commandBytes, 0, index);
      debugMessages.append(command + "\n");

      int contentLength = -1;
      HashMap<String, String> headers = new HashMap<String, String>();

      while (true) // (1)
      {
        // Finite automata state 2:
        String key;
        byte[] keyBytes = new byte[1024]; // 1kiB
        index = 0;

        symbol = (byte) receiver.read();

        if (symbol == '\n')
          break;

        while (true) // (2)
        {
          if (symbol == -1)
          {
            Printer.printWarning("Connection closed from remote end.");
            active = false;
            return;
          } // End if
          else if (symbol == 0 || symbol == '\n')
          {
            Printer.printError("Malformed STOMP frame received.");
            return;
          } // End else if
          else if (symbol == ':')
            break;
          else
          {
            keyBytes[index++] = symbol;
            symbol = (byte) receiver.read();
          } // End else

        } // End while (2)

        key = new String(keyBytes, 0, index);

        // Finite automata state 3:
        String value;
        byte[] valueBytes = new byte[1024]; // 1kiB
        index = 0;

        symbol = (byte) receiver.read();
        while (true) // (3)
        {
          if (symbol == -1)
          {
            Printer.printWarning("Connection closed from remote end.");
            active = false;
            return;
          } // End if
          else if (symbol == 0 || symbol == ':')
          {
            Printer.printError("Malformed STOMP frame received.");
            return;
          } // End else if
          else if (symbol == '\n')
            break;
          else
          {
            valueBytes[index++] = symbol;
            symbol = (byte) receiver.read();
          } // End else

        } // End while (3)

        value = new String(valueBytes, 0, index);

        if (key.equals("content-length"))
          contentLength = Integer.parseInt(value);

        headers.put(key, value);
        debugMessages.append(key + ":" + value + "\n");
      } // End while (1)

      debugMessages.append("\n");

      // Finite automata state 7:

      String body;
      byte[] bodyBytes;
      if (contentLength >= 1)
      {
        bodyBytes = new byte[contentLength];

        for (index = 0; index < contentLength; index++)
          bodyBytes[index] = (byte) receiver.read();

        if (receiver.read() != 0)
          Printer.printWarning(
            "content-length header mismatch (more data in frame)");

        body = new String(bodyBytes);
      } // End if
      else
      {
        bodyBytes = new byte[1024]; // 1kiB
        index = 0;

        symbol = (byte) receiver.read();
        while (true)
        {
          if (symbol == -1)
          {
            Printer.printWarning("Connection closed from remote end.");
            active = false;
            return;
          } // End if
          else if (symbol == 0)
            break;
          else
          {
            bodyBytes[index++] = symbol;
            symbol = (byte) receiver.read();
          } // End else

        } // End while

        body = new String(bodyBytes, 0, index);

      } // End else

      Printer.printDebug(debugMessages.toString() +
        body + "\n\033[1;35m←←←\033[0m");

      notifyClient(command, headers, body);

    } // End try

    catch (SocketException se)
    {
      Printer.printWarning("Connection closed from remote end.");
      active = false;
    } // End ‘SocketException’ catch

    catch (IOException ioe)
    {
      Printer.printError("Unable to parse frame.");
      active = false;
    } // End ‘IOException’ catch

  } // End ‘listen()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private void notifyClient(
    String command, HashMap<String, String> headers, String body)
  {

    if (command.equals("CONNECTED"))
      client.notifyConnected();
    else if (command.equals("ERROR"))
      client.notifyError(body);
    else if (command.equals("RECEIPT"))
    {
      String receiptID = headers.get("receipt-id");
      int sequenceNumber = Integer.parseInt(
        receiptID.substring(receiptID.lastIndexOf('-') + 1));
      client.notifyReceipt(sequenceNumber);
    } // End else if
    else if (command.equals("MESSAGE"))
      client.notifyMessage(body);

  } // End ‘notifyClient(String, HashMap<String, String>, String)’ Method

// ---------------------------------------- ClientReceiver Class ---------------

} // End ‘ClientReceiver’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
