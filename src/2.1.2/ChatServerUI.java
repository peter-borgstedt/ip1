import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment: 2.1.2,
 * Stream sockets on server side.
 *
 * Graphical interface for displaying I/O events.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
class ChatServerUI {
  private final JFrame frame = new JFrame();
  private final JTextArea textArea = new JTextArea();

  ChatServerUI() {
    textArea.setEditable(false);

    var pane = new JScrollPane(textArea);
    pane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    frame.add(pane);

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(480, 240));
    frame.pack();
    frame.setLocationRelativeTo(null); // Center
  }

  /**
   * Write message to content.
   * @param str Text to be added
   */
  public void write(String str) {
    textArea.append(textArea.getText().isEmpty() ? str : "\n" + str);
  }

  /** Display UI */
  public void setVisible() {
    frame.setVisible(true);
  }

  /**
   * Set title of UI window.
   * @param host Host name
   * @param address Host address
   * @param port Host port 
   * @param connections Amount of connections
   */
  public void setTitle(String host, String address, int port, int connections) {
    frame.setTitle(String.format("Serving on %s (%s:%s) with %s connections", host, address, port, connections));
  }
}
