import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 2.1.1, 2.1.2, 2.1.3,
 * Socket stream.
 *
 * A shared class for handling sending and receiving data in socket streaming.
 */
public class SocketAdapter extends Thread {
  private List<OnIncomingListener> onIncomingListeners = new ArrayList<>();
  private List<OnErrorListener> onErrorListeners = new ArrayList<>();

  private final Socket socket;
  private final BufferedReader in;
  private final PrintWriter out;

  /**
   * Constructor.
   * @param socket Socket to be used
   */
  public SocketAdapter(Socket socket) throws IOException {
    this.socket = socket;
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.out = new PrintWriter(socket.getOutputStream(), true);
    this.start();
  }

  /** Functional interface for listening on incoming events */
  @FunctionalInterface
  public interface OnIncomingListener {
    public void event(SocketAdapter cs, String data) throws Exception;
  }

  /** Functional interface for listening on error events */
  @FunctionalInterface
  public interface OnErrorListener {
    public void event(SocketAdapter cs, Exception e);
  }

  /**
   * Send text event.
   * @param content Text to be sent
   */
  public synchronized void write(String content) {
    out.println(content);
  }

  /** Get host address */
  public String getHostAddress() {
    return this.socket.getInetAddress().getHostAddress();
  }

  /**
   * Add listener to be invoked on incoming events.
   * @param l Listener to be invoked on incoming events
   */
  public void addOnIncomingListener(OnIncomingListener l) {
    this.onIncomingListeners.add(l);
  }

  /**
   * Add listener to be invoked on error events.
   * @param l Listener to be invoked on error events
   */
  public void addOnErrorListener(OnErrorListener l) {
    this.onErrorListeners.add(l);
  }

  /**
   * Start listening on incoming events.
   */
  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        var data = in.readLine();
        if (data == null) { // End of stream
          throw new EOFException("End of the stream has been reached");
        }

        for (var l : onIncomingListeners) {
          l.event(this, data); 
        }
      } catch (Exception e) {
        if (onErrorListeners.isEmpty()) {
          e.printStackTrace();
        } else {
          onErrorListeners.forEach(l -> l.event(this, e));
        }
        return; // End thread
      }
    }
  }
}