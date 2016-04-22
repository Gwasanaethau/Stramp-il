// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;
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

  private InputStream receiver;
  private boolean active;

// ----------------------------------------- STOMPListener Class ---------------

  STOMPListener(InputStream receiver)
  {

    this.receiver = receiver;
    active = true;

  } // End ‘STOMPListener(InputStream)’ Constructor

// ----------------------------------------- STOMPListener Class ---------------

  public void run()
  {

    Printer.printDebug("Stub listener activated!");

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

        Printer.printDebug("Message received \033[1;35m↓\n←←←\033[0m\n" +
          new String(partialMessage, 0, count) +
          "\033[1;35m←←←\033[0m");
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

    } // End while
    Printer.printDebug("Thread exited!");

  } // End ‘run()’ Method

// ----------------------------------------- STOMPListener Class ---------------

} // End ‘STOMPListener’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
