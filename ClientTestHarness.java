// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * A control harness to test the features of a {@link STOMPClient}.
 *
 * @author Mark David Pokorny
 * @version Dé Sathairn, 23ú Aibreán 2016
 * @since Déardaoin, 21ú Aibreán 2016
 */
public class ClientTestHarness
{

// ------------------------------------- ClientTestHarness Class ---------------

  /**
   * Sets up and initialises a test STOMP client, sends a request to port 51515
   * for a connection, receives test messages and then shuts itself down.
   *
   * @param args Not used (ignored).
   */
  public static void main(String[] args)
  {

    STOMPClient client = new STOMPClient(51515);
    if (client.handshake())
      client.connect();
    else
      client.close();

  } // End ‘main(String[] args)’ Method

// ------------------------------------- ClientTestHarness Class ---------------

} // End ‘ClientTestHarness’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
