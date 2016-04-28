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
 * @version Dé hAoine, 29ú Aibreán 2016
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

      String command = parseCommand();
      if (command == null)
        return;

      debugMessages.append(command + "\n");

      HashMap<String, String> headers = parseHeaders(debugMessages);

      debugMessages.append("\n");

      int contentLength = -1;
      if (headers.containsKey("content-length"))
        contentLength = Integer.parseInt(headers.get("content-length"));

      String body = parseBody(contentLength);

      Printer.printDebug(debugMessages.toString() +
        body + "\n\033[1;35m←←←\033[0m");

      notifyClient(command, headers, body);

    } // End try

    catch (SocketException se)
    {
      remoteClosed();
    } // End ‘SocketException’ catch

    catch (IOException ioe)
    {
      Printer.printError("Unable to parse frame.");
      active = false;
    } // End ‘IOException’ catch

  } // End ‘listen()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private String parseCommand() throws IOException
  {

    String command = null;
    byte[] commandBytes = new byte[12]; // Length of ‘UNSUBSCRIBE\n’.
    byte index = 0;

    byte symbol = (byte) receiver.read();
    while (symbol != -1 && symbol != 0 && symbol != '\n')
    {
      commandBytes[index++] = symbol;
      symbol = (byte) receiver.read();
    } // End while

    if (symbol == -1)
      remoteClosed();
    else if (symbol == 0)
      malformedSTOMP();
    else
      command = new String(commandBytes, 0, index);

    return command;

  } // End ‘parseCommand()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private HashMap<String, String> parseHeaders(
    StringBuilder debugMessages) throws IOException
  {

    HashMap<String, String> headers = new HashMap<String, String>();

    while (true)
    {
      String key = parseKey();

      if (key == null)
        break;

      String value = parseValue();

      headers.put(key, value);
      debugMessages.append(key + ":" + value + "\n");
    } // End while

    return headers;

  } // End ‘parseHeaders(StringBuilder)’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private String parseKey() throws IOException
  {

    String key = null;
    byte[] keyBytes = new byte[1024]; // 1kiB
    int index = 0;

    byte symbol = (byte) receiver.read();
    if (symbol == '\n')
      return null;

    while (symbol != -1 && symbol != 0 && symbol != '\n' && symbol != ':')
    {
      keyBytes[index++] = symbol;
      symbol = (byte) receiver.read();
    } // End while

    if (symbol == -1)
      remoteClosed();
    else if (symbol == 0 || symbol == '\n')
      malformedSTOMP();
    else
      key = new String(keyBytes, 0, index);

    return key;

  } // End ‘parseKey()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private String parseValue() throws IOException
  {

    String value = null;
    byte[] valueBytes = new byte[1024]; // 1kiB
    int index = 0;

    byte symbol = (byte) receiver.read();
    while (symbol != -1 && symbol != 0 && symbol != ':' && symbol != '\n')
    {
      valueBytes[index++] = symbol;
      symbol = (byte) receiver.read();
    } // End while

    if (symbol == -1)
      remoteClosed();
    else if (symbol == 0 || symbol == ':')
      malformedSTOMP();
    else
      value = new String(valueBytes, 0, index);

    return value;

  } // End ‘parseValue()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private String parseBody(int length) throws IOException
  {

    String body = null;
    byte[] bodyBytes;

    if (length >= 1)
    {
      bodyBytes = new byte[length];

      for (int index = 0; index < length; index++)
        bodyBytes[index] = (byte) receiver.read();

      if (receiver.read() != 0)
        Printer.printWarning(
          "content-length header mismatch (more data in frame)");

      body = new String(bodyBytes);
    } // End if
    else
    {
      bodyBytes = new byte[1024]; // 1kiB
      int index = 0;

      byte symbol = (byte) receiver.read();
      while (symbol != -1 && symbol != 0)
      {
        bodyBytes[index++] = symbol;
        symbol = (byte) receiver.read();
      } // End while

      if (symbol == -1)
        remoteClosed();

      body = new String(bodyBytes, 0, index);
    } // End else

    return body;

  } // End ‘parseBody(int)’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private void remoteClosed()
  {
    Printer.printWarning("Connection closed from remote end.");
    active = false;
  } // End ‘remoteClosed()’ Method

// ---------------------------------------- ClientReceiver Class ---------------

  private static void malformedSTOMP()
  {
    Printer.printError("Malformed STOMP frame received.");
  } // End ‘malformedSTOMP()’ Method

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
