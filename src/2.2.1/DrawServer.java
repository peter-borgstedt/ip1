import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 2.2.1,
 * Datagram sockets with unicast.
 *
 * Send and retrieve data using connection-less socket (UDP) {@link DatagramSocket}
 * and {@link DatagramPacket}.
 *
 * References:
 * https://www.javatpoint.com/DatagramSocket-and-DatagramPacket
 */
public class DrawServer {
  @FunctionalInterface
  public interface OnIncomingDataListener {
    public void event(byte[] bytes);
  }

  private InetAddress remoteHost;
  private int remotePort;
  private DatagramSocket in;
  private DatagramSocket out;

  private OnIncomingDataListener onIncomingDataListener;

  /**
   * Initialize drawing server datagram socket connection.
   * @param localPort Local port to use
   * @param remoteHost Remote host address
   * @param remotePort Remote host address port
   */
  public DrawServer(int localPort, String remoteHost, int remotePort) throws IOException, UnknownHostException {
    this.remoteHost = InetAddress.getByName(remoteHost);
    this.remotePort = remotePort;
    this.in = new DatagramSocket(localPort);
    this.out = new DatagramSocket();
    start();
  }

  /**
   * Set Listener to be invoked when any incoming data is received.
   * @param onIncomingDataListener Listener to be invoked when data is retrieved
   */
  public void setOnIncomingDataListener(OnIncomingDataListener onIncomingDataListener) {
    this.onIncomingDataListener = onIncomingDataListener;
  }

  /**
   * Send data to remote host.
   * @param data Byte array to send
   */
  public void write(byte[] data) throws IOException {
    this.out.send(new DatagramPacket(data, data.length, remoteHost, remotePort));
  }

  /**
   * Thread listening on incoming datagram packets.
   */
  public void start() {
    new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          var block = new byte[36];
          DatagramPacket datagramPacket = new DatagramPacket(block, block.length);
          in.receive(datagramPacket);
          // We could just use the block as well as we know exactly the amount
          // of data we are going to retrieve
          onIncomingDataListener.event(datagramPacket.getData());
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
