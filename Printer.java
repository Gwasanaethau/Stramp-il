// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * Provides static methods for printing info to the CLI.
 *
 * @author Mark David Pokorny
 * @version Dé Céadaoin, 27ú Aibreán 2016
 * @since Dé hAoine, 22ú Aibreán 2016
 */
public abstract class Printer implements Constants
{

// ----------------------------------------------- Printer Class ---------------

  /** The level of debug information to be displayed. */
  public static int debugLevel = 0;

// ----------------------------------------------- Printer Class ---------------

  private Printer(){}

// ----------------------------------------------- Printer Class ---------------

  /**
   * Prints a debug message to the CLI (if debug level set).
   *
   * @param message The message to be printed.
   */
  public static void printDebug(String message)
  {
    if (debugLevel <= DEBUG)
      printGeneric(2, message);
  } // End ‘printInfo(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  /**
   * Prints an info message to the CLI (if info/debug level set).
   *
   * @param message The message to be printed.
   */
  public static void printInfo(String message)
  {
    if (debugLevel <= INFO)
      printGeneric(4, message);
  } // End ‘printInfo(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  /**
   * Prints a warning message to the CLI (if debug/info/warning level set).
   *
   * @param message The message to be printed.
   */
  public static void printWarning(String message)
  {
    if (debugLevel <= WARNING)
      printGeneric(3, message);
  } // End ‘printWarning(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  /**
   * Prints an error message to the CLI (if debug/info/warning/error level set).
   *
   * @param message The message to be printed.
   */
  public static void printError(String message)
  {
    if (debugLevel <= ERROR)
      printGeneric(1, message);
  } // End ‘printError(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  private static void printGeneric(int colour, String message)
  {
    System.err.println("\033[3" + colour + "m→\033[0m " + message);
  } // End ‘printGeneric(int, String)’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printTCPError(String command)
  {
    Printer.printError("Cannot send " + command +
      " frame due to absence of a TCP connection." +
      " Ensure that you can connect to the server with TCP" +
      " first before trying to send STOMP messages.");
  } // End ‘printTCPError(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printDisconnectError()
  {
    Printer.printError("DISCONNECT has been issued," +
      " no more STOMP frames are allowed to be sent!");
  } // End ‘printDisconnectError()’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printIOError(String command)
  {
    Printer.printError("Cannot send " + command + " frame due to I/O issue.");
  } // End ‘printIOError(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printSTOMPError()
  {
    Printer.printError("STOMP connection closed! A STOMP connection needs" +
      " to be established first.");
  } // End ‘printSTOMPError(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printSendFrame(String frame)
  {
    Printer.printDebug("Sending frame \033[1;33m↓\n→→→\033[0m\n" +
      frame + "\n\033[1;33m→→→\033[0m");
  } // End ‘printSendFrame(String)’ Method

// ----------------------------------------------- Printer Class ---------------

} // End ‘Printer’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
