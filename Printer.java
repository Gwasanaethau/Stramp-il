// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * Provides static methods for printing info to the CLI.
 *
 * @author Mark David Pokorny
 * @version Dé Sathairn, 23ú Aibreán 2016
 * @since Dé hAoine, 22ú Aibreán 2016
 */
abstract class Printer
{

// ----------------------------------------------- Printer Class ---------------

  public static int debugLevel = 0;

// ----------------------------------------------- Printer Class ---------------

  static void printDebug(String message)
  {
    if (debugLevel <= STOMPClient.DEBUG)
      printGeneric(2, message);
  } // End ‘printInfo(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printInfo(String message)
  {
    if (debugLevel <= STOMPClient.INFO)
      printGeneric(4, message);
  } // End ‘printInfo(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printWarning(String message)
  {
    if (debugLevel <= STOMPClient.WARNING)
      printGeneric(3, message);
  } // End ‘printWarning(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  static void printError(String message)
  {
    if (debugLevel <= STOMPClient.ERROR)
      printGeneric(1, message);
  } // End ‘printError(String)’ Method

// ----------------------------------------------- Printer Class ---------------

  private synchronized static void printGeneric(int colour, String message)
  {
    System.err.println("\033[3" + colour + "m→\033[0m " + message);
  } // End ‘printGeneric(int, String)’ Method

// ----------------------------------------------- Printer Class ---------------

} // End ‘Printer’ Class

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
