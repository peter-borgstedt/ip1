import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 2.2.1,
 * Datagram sockets with unicast.
 * 
 * A drawing P2P application (client/server).
 *
 * Featuring possibility for using custom brush, size and colors.
 * Stream data using DatagramPacket (UDP) and conversion between int and bytes.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class Draw {
  private DrawServer server;
  private DrawUI ui;

  /**
   * Constructor; initializes the drawing server.
   * @param localPort Local port to use
   * @param remoteHost Remote host address
   * @param remotePort Remote host address port
   */
  public Draw(int localPort, String remoteHost, int remotePort) throws IOException, UnknownHostException {
    this.server = new DrawServer(localPort, remoteHost, remotePort);
    this.ui = new DrawUI();
    init();
  }

  /**
   * Initializes drawing application using datagram packets by
   * setting up some listeners on in and out going events.
   */
  private void init() {
    this.server.setOnIncomingDataListener((bytes) -> {
      var mtr = DrawUtilities.split(bytes, Integer.BYTES);
      var px = DrawUtilities.bytesToInt(mtr[0]); // 4 bytes
      var py = DrawUtilities.bytesToInt(mtr[1]); // 4 bytes
      var cx = DrawUtilities.bytesToInt(mtr[2]); // 4 bytes
      var cy = DrawUtilities.bytesToInt(mtr[3]); // 4 bytes
      var brush = DrawUtilities.bytesToInt(mtr[4]); // 4 bytes
      var brushSize = DrawUtilities.bytesToInt(mtr[5]); // 4 bytes
      var r = DrawUtilities.bytesToInt(mtr[6]); // 4 bytes
      var g = DrawUtilities.bytesToInt(mtr[7]); // 4 bytes
      var b = DrawUtilities.bytesToInt(mtr[8]); // 4 bytes

      var previousPoint = px == -1 ? null : new Point(px, py);
      var currentPoint = new Point(cx, cy);
      var color = new Color(r, g, b);

      this.ui.draw(previousPoint, currentPoint, brush, brushSize, color);
    });

    this.ui.setOnDrawnListener((previousPoint, currentPoint, pencelType, pencelSize, color) -> {
      var px = DrawUtilities.intToBytes(previousPoint == null ? -1 : previousPoint.x); // 4 bytes
      var py = DrawUtilities.intToBytes(previousPoint == null ? -1 : previousPoint.y); // 4 bytes
      var cx = DrawUtilities.intToBytes(currentPoint.x); // 4 bytes
      var cy = DrawUtilities.intToBytes(currentPoint.y); // 4 bytes
      var brush = DrawUtilities.intToBytes(pencelType); // 4 bytes
      var brushSize = DrawUtilities.intToBytes(pencelSize); // 4 bytes
      var r = DrawUtilities.intToBytes(color.getRed()); // 4 bytes
      var g = DrawUtilities.intToBytes(color.getGreen()); // 4 bytes
      var b = DrawUtilities.intToBytes(color.getBlue()); // 4 bytes

      try {
        this.server.write(DrawUtilities.merge(px, py, cx, cy, brush, brushSize, r, g, b)); // 36 bytes
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  public static void main(String args[]) {
    try {
      var localPort = Integer.parseInt(args[0]);
      var remoteHost = args[1];
      var remotePort = Integer.parseInt(args[2]);

      new Draw(localPort, remoteHost, remotePort);
    } catch (UnknownHostException e) {
      System.err.println("Could not establish host address");
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Command: [local port] [remote host] [remote port]");
      System.exit(1);
    }
  }
}
