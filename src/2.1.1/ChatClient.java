import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment: 2.1.1,
 * Stream sockets on client side.
 *
 * Chat client featuring {@link PrintWriter} and {@link InputStreamReader}.
 * 
 * Uses class {@SocketAdapter} shared between several assignments.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class ChatClient {
  private SocketAdapter socket;

  /** Constructor */
  public ChatClient(String host, int port) throws IOException {
    this.socket = new SocketAdapter(new Socket(host, port));
    buildUI(host, port);
  }

  /**
   * Builds the user interface.
   * @param host Host address
   * @param port Host port
   */
  private void buildUI(String host, int port) {
    var frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setTitle(String.format("Connected to %s:%s", host, port));

    var panel = new JPanel(new GridBagLayout());
    var gc = new GridBagConstraints();
    gc.gridx = gc.gridy = 0;
    gc.fill = GridBagConstraints.BOTH;
    gc.weightx = 1;
    gc.weighty = 1;

    var area = new JTextArea();
    area.setEditable(false);

    var pane = new JScrollPane(area);
    pane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    panel.add(pane, gc);

    var field = new JTextField();
    field.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "send");
    field.getActionMap().put("send", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent event) {
        socket.write(field.getText());
        field.setText(null);
      }
    });

    field.setPreferredSize(new Dimension(0 /* Expands trough constraints */, 30));
    field.setBorder(BorderFactory.createCompoundBorder( // Add a nicer border
      BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
      BorderFactory.createEmptyBorder(4, 4, 4, 4)
    ));

    ++gc.gridy;
    gc.weighty = 0;
    panel.add(field, gc);

    frame.add(panel);
    frame.setPreferredSize(new Dimension(320, 240));
    frame.pack();
    frame.setLocationRelativeTo(null); // Center
    field.grabFocus(); // Set focus on field

    /** Listener on incoming messages */
    this.socket.addOnIncomingListener((socket, message) -> area.append(message + "\n"));

    /** Listener on socket error */
    this.socket.addOnErrorListener((socket, exception) -> {
      System.err.println("Connection was closed abruptly...");
      System.exit(1);
    });

    frame.setVisible(true);
  }

  public static void main(String[] args) {
    var host = "127.0.0.1"; // Default address
    var port = 2000; // Default port

    if (args.length > 0) {
      host = args[0];
    }

    if (args.length == 2) {
      port = Integer.parseInt(args[1]);
    }

    try {
      new ChatClient(host, port);
    } catch (UnknownHostException e) {
      System.err.println("Unknown host " + host);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Could not connect to " + host);
      System.exit(1);
    }
  }
}
