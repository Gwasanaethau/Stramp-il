// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

package strampáil;

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

/**
 * <code>Notifier</code> is a mechanism that allows clients to be alerted
 * when a <code>MESSAGE</code> frame arrives from the server.
 * Client programmes should implement this interface if they wish to be alerted.
 * The alternative is to poll the client interface asynchronously for the
 * presence of <code>MESSAGE</code>s in the retrieval system.
 *
 * @author Mark David Pokorny
 * @version Dé Domhnaigh, 24ú Aibreán 2016
 * @since Dé Domhnaigh, 24ú Aibreán 2016
 */
public interface Notifier
{

// ------------------------------------------ Notifier Interface ---------------

  /**
   * This method is triggered whenever a <code>MESSAGE</code>
   * arrives from the server.
   */
  void alert();

// ------------------------------------------ Notifier Interface ---------------

} // End ‘Notifier’ Interface

// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
