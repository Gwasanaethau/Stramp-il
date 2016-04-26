// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.util.HashMap;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>FrameParser</code> parses STOMP frames that the {@link ClientReceiver}
 * receives and performs actions on them appropriately.
 *
 * @author Mark David Pokorny
 * @version Dé Máirt, 26ú Aibreán 2016
 * @since Dé Máirt, 26ú Aibreán 2016
 */
class FrameParser extends Thread
{

// ------------------------------------------- FrameParser Class ---------------

  private ClientInterface client;
  private String frame;

// ------------------------------------------- FrameParser Class ---------------

  FrameParser(ClientInterface client, String frame)
  {
    this.client = client;
    this.frame = frame;
  } // End ‘FrameParser(ClientInterface)’ Constructor

// ------------------------------------------- FrameParser Class ---------------

  public void run()
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

  } // End ‘run()’ Method

// ------------------------------------------- FrameParser Class ---------------

  private void notifyClient(
    String command, HashMap<String, String> headers, String body)
  {

    if (command.equals("CONNECTED"))
      client.notifyConnected();
    else if (command.equals("ERROR"))
      client.notifyError();
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

// ------------------------------------------- FrameParser Class ---------------

} // End ‘FrameParser’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
