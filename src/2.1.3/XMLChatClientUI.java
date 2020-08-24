import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment: 2.1.3,
 * Stream sockets and XML.
 *
 * Graphical interface for send and retrieving XML messages.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class XMLChatClientUI {
  private JTextArea contentArea = new JTextArea();

  private JTextField nameField = new JTextField("Peter Borgstedt");
  private JTextField emailField = new JTextField("pebo6883@student.su.se");
  private JTextField homepageField = new JTextField("http://www.google.com");
  private JTextField messageField = new JTextField("Hello XML-world!");

  private JButton button = new JButton();

  /** Constructor */
  public XMLChatClientUI() {
    buildUI();
  }

  /** Builds the user interface */
  private void buildUI() {
    var frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setTitle("");

    var panel = new JPanel(new GridBagLayout());

    var gc = new GridBagConstraints();
    gc.gridx = gc.gridy = 0;
    gc.fill = GridBagConstraints.BOTH;
    gc.weightx = 1;
    gc.weighty = 1;
    gc.gridwidth = 2;

    contentArea.setEditable(false);

    var pane = new JScrollPane(contentArea);
    pane.setBorder(BorderFactory.createCompoundBorder( // Add a nicer border
      BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
      BorderFactory.createEmptyBorder(4, 4, 4, 4)
    ));
    panel.add(pane, gc);

    gc.weighty = 0;
    gc.gridwidth = 1;

    var fields = new LinkedHashMap<String, JComponent>();
    fields.put("Name", nameField);
    fields.put("Email", emailField);
    fields.put("Homepage", homepageField);
    fields.put("Message", messageField);

    for (var entry : fields.entrySet()) {
      gc.gridy++;
      gc.gridx = 0;
      gc.weightx = 0;
      gc.insets = new Insets(4, 10, 4, 10);

      var label = new JLabel(entry.getKey());
      label.setHorizontalAlignment(SwingConstants.RIGHT);
      panel.add(label, gc);
      gc.gridx++;
      gc.weightx = 1;
      gc.insets = new Insets(4, 0, 4, 4);

      var field = entry.getValue();
      field.setPreferredSize(new Dimension(0 /* Expands trough constraints */, 30));
      field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(2, 4, 2, 2)
      ));

      panel.add(field, gc);
    }

    this.button.setText("Send");
    this.button.setPreferredSize(new Dimension(100, 30));

    var buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(this.button);

    gc.gridy++;
    gc.gridx = 0;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    gc.gridwidth = 2;
    panel.add(buttonPanel, gc);

    frame.add(panel);

    frame.setPreferredSize(new Dimension(640, 480));
    frame.pack();
    frame.setLocationRelativeTo(null); // Center

    messageField.grabFocus();

    frame.setVisible(true);
  }

  /**
   * Append text to the content being displayed.
   * @param text Text to be added in the bottom
   */
  public void append(String text) {
    contentArea.append(text);
    contentArea.setCaretPosition(this.contentArea.getText().length());
  }

  /** Clear input fields */
  public void clear() {
    messageField.setText(null);
  }

  /**
   * Get values from all input fields
   * @return mapped values for each field input
   */
  public Map<String, String> getProperties() {
    var m = new HashMap<String, String>();
    m.put("name", nameField.getText());
    m.put("email", emailField.getText());
    m.put("homepage", homepageField.getText());
    m.put("message", messageField.getText());
    return m;
  }

  /** Add a listener which will be invoked when the send button is clicked on */
  public void addActionListener(ActionListener l) {
    this.button.addActionListener(l);
  }
}
