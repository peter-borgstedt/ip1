import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment: 2.1.2,
 * Stream sockets on server side.
 *
 * A chat server featuring {@link BufferedReader}, {@link PrintWriter} on socket streams.
 *
 * Uses class {@SocketAdapter} shared between several assignments.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class ChatServer implements AutoCloseable {
  private Set<SocketAdapter> connections = Collections.synchronizedSet(new HashSet<>());
  private ServerSocket serverSocket;
  private ChatServerUI ui;
  private String hostName;
  private String hostAddress;
  private int port;

  /** Listener on incoming messages */
  private SocketAdapter.OnIncomingListener onIncoming = (socket, message) -> {
    broadcast(message);
  };

  /** Listener on socket error */
  private SocketAdapter.OnErrorListener onError = (socket, exception) -> {
    var address = socket.getHostAddress();
    if (exception instanceof EOFException) { // End of stream (connection has been disconnected)
      broadcast(String.format("[%s] -> has disconnected", address, connections.size()));
    } else {
      broadcast(String.format("[%s] -> has been removed due to I/O error", address, connections.size()));
    }

    connections.remove(socket);
    ui.setTitle(hostName, hostAddress, port, connections.size());
  };

  /**
   * Constructor.
   * @param port Server port (to use)
   */
  ChatServer(int port) throws IOException {
    this.ui = new ChatServerUI();
    this.serverSocket = new ServerSocket(port);
    // https://crunchify.com/how-to-get-server-ip-address-and-hostname-in-java/
    this.hostName = InetAddress.getLocalHost().getHostName(); // Retrieved statically
    this.hostAddress = this.serverSocket.getInetAddress().getHostAddress();
    this.port = port;
  }

  /* Start serving */
  public void start() {
    ui.setVisible();
    serve();
  }

  /**
   * Broadcast message.
   * @param message message to be broadcasted
   */
  private void broadcast(String message) {
    ui.write("SERVER::BROADCAST: " + message);
    System.out.println("SERVER::BROADCAST: " + message);

    for (var c : connections) {
      c.write(message);
    }
  }

  /* Start listening to connections */
  private void serve () {
    ui.write(String.format("SERVER::SERVE: Listening for incoming connections (%s:%s)", hostAddress, port));
    ui.setTitle(hostName, hostAddress, port, connections.size());

    try {
      while (true) {
        var socket = new SocketAdapter(serverSocket.accept());
        socket.addOnIncomingListener(onIncoming);
        socket.addOnErrorListener(onError);
        broadcast(String.format("[%s] -> has connected", socket.getHostAddress()));

        this.connections.add(socket);
        ui.setTitle(hostName, hostAddress, port, connections.size());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Close connection. */
  @Override
  public void close() throws Exception {
    this.serverSocket.close();
  }

  public static void main(String[] args) {
    var port = args.length == 1 ? Integer.parseInt(args[0]) : 2000;
    try (var server = new ChatServer(port)) {
      server.start();
    } catch (BindException e) {
      System.err.println(String.format("Port %s is already in use", port));
      System.exit(1);
    } catch(Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
