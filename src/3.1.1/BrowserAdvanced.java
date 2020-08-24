import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;


/**
 * IP1 (IB906C), VT 2020 Internet programming, stationary units.
 *
 * Assignment 3.1.1,
 * Web server connection.
 *
 * A simple HTML browser for viewing decoded HTML (v3.2 - no javascript support).
 * Notice! JavaFX seem to support HTML 5 and javascript.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class BrowserAdvanced {
  private JFrame frame = new JFrame("A HTML 3.2 browser (press Enter to search)");
  private JEditorPane editorPane = new JEditorPane(); // No javascript support
  private JTextField field = new JTextField("http://www.google.com");

  /** Constructor */
  public BrowserAdvanced() {
    buildUI();
  }

  /** Builds the user interface */
  private void buildUI() {
    editorPane.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
    editorPane.setEditable(false);

    // JPaneEditor (HTMLWriter) does not decode UTF-8 characters, read more:
    // https://stackoverflow.com/a/8326566
    editorPane.setContentType("text/html");

    editorPane.addHyperlinkListener((HyperlinkEvent e) -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        var url = e.getURL().toString();
        this.field.setText(url);
        readUrl(url);
      }
    });

    var scrollPane = new JScrollPane(this.editorPane);
    scrollPane.setBorder(null);

    field.setPreferredSize(new Dimension(0 /* Expands trough constraints */ , 30));
    field.addKeyListener(new KeyAdapter() {
      @Override public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
          readUrl(BrowserAdvanced.this.field.getText());
        }
      }
    });

    frame.add(field, BorderLayout.NORTH);
    frame.add(scrollPane, BorderLayout.CENTER);

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
  private void readUrl (String url) {
    try {
      editorPane.setPage(url.trim());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new BrowserAdvanced();
  }
}
