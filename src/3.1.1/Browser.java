import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 3.1.1,
 * Web server connection.
 *
 * A simple HTML browser for viewing raw HTML.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class Browser {
  private JFrame frame;
  private JTextArea area;
  private JTextField field;

  /** Constructor */
  public Browser() {
    buildUI();
  }

  /** Builds the user interface */
  private void buildUI() {
    this.frame = new JFrame("A simple (raw) HTML browser (press Enter to search)");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    this.area = new JTextArea();
    area.setEditable(false);

    var pane = new JScrollPane(this.area);
    pane.setBorder(null);

    this.field = new JTextField("http://www.google.com");
    field.setPreferredSize(new Dimension(0 /* Expands trough constraints */ , 30));
    field.addKeyListener(new KeyAdapter() {
      @Override public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
          readUrl(Browser.this.field.getText().trim());
        }
      }
    });

    frame.add(field, BorderLayout.NORTH);
    frame.add(pane, BorderLayout.CENTER);

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(640, 480));
    frame.pack();
    frame.setLocationRelativeTo(null); // Center
    frame.setVisible(true);
  }

  /**
   * Read content from URL. 
   * @param url URL to be read
   */
  public void readUrl(String url) {
    try (var br = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8))) {
      var buffer = new StringBuilder();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        buffer.append(line + "\n");
      }

      this.area.setText(buffer.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new Browser();
  }
}
