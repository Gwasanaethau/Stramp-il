// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>STOMPListener</code> listens to transmissions from the remote end
 * and (currently) displays them on the CLI.
 *
 * @author Mark David Pokorny
 * @version Dé hAoine, 22ú Aibreán 2016
 * @since Dé hAoine, 22ú Aibreán 2016
 */
class STOMPListener extends Thread
{

// ----------------------------------------- STOMPListener Class ---------------

  private static final String PROG_NAME = "Strampáil Client (Receiver)";

  private BufferedInputStream receiver;
  private boolean active;

// ----------------------------------------- STOMPListener Class ---------------

  STOMPListener(BufferedInputStream receiver)
  {

    this.receiver = receiver;
    active = true;

  } // End ‘STOMPListener(BufferedInputStream)’ Constructor

// ----------------------------------------- STOMPListener Class ---------------

  public void run()
  {

    System.out.println("\033[35mStub listener activated!\033[0m");

    while (active)
    {

      byte[] partialMessage = new byte[1024];
      int count = 0;

      try
      {
        while (true)
        {
          byte symbol = (byte) receiver.read();

          if (symbol == 0)
            break;

          partialMessage[count++] = symbol;
        } // End while

        System.out.print("Message received: ");
        System.out.write(partialMessage, 0, count);
        System.out.println();
      } // End try

      catch (SocketException se)
      {
        System.err.println(PROG_NAME + ": " +
          "Closing receiver.");
        active = false;
      } // End ‘SocketException’ catch

      catch (IOException ioe)
      {
        System.err.println(PROG_NAME + ": " +
          "\033[31mUnable to parse message.\033[0m");
        ioe.printStackTrace();
        active = false;
      } // End ‘IOException’ catch

    } // End while
    System.out.println("Thread exited!");

  } // End ‘run()’ Method

// ----------------------------------------- STOMPListener Class ---------------

} // End ‘STOMPListener’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
