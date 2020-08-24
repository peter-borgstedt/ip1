import java.awt.Dimension;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 2.2.2,
 * datagram sockets with multicast.
 *
 * A connection (online) checker using multicast (UDP).
 * 
 * Note, this was written with Java 13, however after upgrading to
 * Java 14 some methods are now depricated, I've added some comment which
 * methods should be used instead.
 *
 * References:
 * https://examples.javacodegeeks.com/core-java/net/multicastsocket-net/java-net-multicastsocket-example/
 * https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class Online {
  // https://stackoverflow.com/a/28131137
  private static Pattern PROPERTY_REGEXP = Pattern.compile("(?<key>\\b\\w+):(?<value>.*?(?=\\s\\w+:|$))");
  private static int DEFAULT_PORT = 2000;
  private static String DEFAULT_HOST = "234.235.236.237";

  private String name;
  private String comment;

  private InetAddress localHost;
  private InetAddress multicastHost;

  private MulticastSocket in;
  private MulticastSocket out;

  private Set<String> connections = new HashSet<>();

  private JFrame frame = new JFrame();
  private JTextArea area = new JTextArea();

  /**
   * Initialize a multicast socket connection.
   * @param name Name of user
   * @param comment Comment from user
   */
  public Online(String name, String comment) throws IOException {
    this.name = name;
    this.comment = comment;

    this.localHost = InetAddress.getLocalHost();
    this.multicastHost = InetAddress.getByName(DEFAULT_HOST);

    this.in = new MulticastSocket(DEFAULT_PORT);
    // Deprecated in Java 14, instead joinGroup(SocketAddress, NetworkInterface)
    // should be used
    in.joinGroup(multicastHost);

    this.out = new MulticastSocket();
    // Deprecated in Java 14, instead setNetworkInterface(NetworkInterface)
    // should be used
    out.setInterface(InetAddress.getLocalHost());

    buildUI();
    startReceiving();
    startSending();
    startUpdateUI();

    frame.setTitle(String.format("GROUP: %s JOINED AT PORT: %s", DEFAULT_HOST, DEFAULT_PORT));
  }

  /**
   * Build simple GUI for loggings of current connections.
   */
  private void buildUI() {
    area.setEditable(false);
    frame.add(area);

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(640, 480));
    frame.pack();

    frame.setLocationRelativeTo(null); // Center
    frame.setVisible(true);
  }

  /**
   * Thread with logic that receives any data sent from multicast host.
   */
  private void startReceiving() {
    new Thread(() -> {
      while(true) {
        try {
          // Our custom protocol OTTP (Online Text Transfer Protocol)
          var message = String.format("From: %s Host: %s Comment: %s", this.name, this.localHost, this.comment).getBytes(StandardCharsets.UTF_8);
          this.out.send(new DatagramPacket(message, message.length, multicastHost, DEFAULT_PORT));

          Thread.sleep(1000); // Wait 1 second
        } catch (InterruptedException | IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  /**
   * Thread with logic that sends data (pings) multicast host.
   */
  private void startSending() {
    new Thread(() -> {
      while(true) {
        try {
          var block = new byte[4096];
          var packet = new DatagramPacket(block, block.length);
          this.in.receive(packet);

          var data = new String(packet.getData());
          connections.add(data);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  /**
   * Thread with logic that updates UI with connections retrieved from multicast host
   */
  private void startUpdateUI() {
    new Thread(() -> {
      while(true) {
        try {
          area.setText(null); // Clear text

          connections.forEach(connection -> {
            try {
              var tokens = getTokens(connection);
              var from = tokens.get("from");
              var host = tokens.get("host");
              var comment = tokens.get("comment");

              if (from == null || host == null || comment == null) {
                throw new NullPointerException();
              }

              // Only append if all values exists
              area.append(String.format("%s --- %s --- %s\n", from, host, comment));
            } catch (Exception e) {
              area.append("COULD NOT PARSE MESSAGE\n");
            }
          });

          connections.clear(); // Remove all connections and wait for ping
          Thread.sleep(5000); // Wait 5 second
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  /**
   * Extract values from a text using tokens
   * @param str The string to extract text from
   * @param keys Searchable keys (tokens) to match upon
   * @return map with relative values for tokens
   */
  private Map<String, String> getTokens(String str) {
    var properties = new HashMap<String, String>();

    // Using regular expression instead of the recommended StringTokenizer,
    // handling values with more words like 'Comment: "one two three"' gets to complex
    var matcher = PROPERTY_REGEXP.matcher(str);

    while (matcher.find()) {
      var key = matcher.group("key").toLowerCase();
      var value = matcher.group("value").trim();
      properties.put(key, value);
    }
    return properties;
  }

  public static void main(String[] args) {
    if (args.length == 2) {
      try {
        var name = args[0];
        var comment = args[1];

        new Online(name, comment);
      } catch (UnknownHostException e) {
        System.err.println("Could not establish host address");
        System.exit(1);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    } else {
      System.err.println("Command: [name] [message]");
    }
  }
}
