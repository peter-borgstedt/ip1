import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 3.2.2,
 * Email receiving.
 *
 * A simple mail client that only receives and show brief details of INBOX.
 * 
 * Notice!
 * Turn of any web or email protection that may be running in antivirus software,
 * this can block this wrong running (which it did for me).
 *
 * References:
 * https://www.tutorialspoint.com/javamail_api/index.htm
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class EmailReceiver {
  /** Constructor */
  public EmailReceiver() {
    buildUI();
  }

  /**
   * Builds the user interface.
   */
  public void buildUI() {
    var frame = new JFrame();

    var host = new JTextField("pop.yandex.com");
    var username = new JTextField("iprogramming@yandex.com");
    var password = new JTextField("effectivejava");
    var messages = new JTextArea();
    messages.setEditable(false);

    var panel = new JPanel(new GridBagLayout());
    var gc = new GridBagConstraints();
    gc.fill = GridBagConstraints.BOTH;
    gc.weightx = gc.weighty = 0;
    gc.gridx = gc.gridy = 0;
    gc.gridwidth = 1;

    gc.insets = new Insets(4, 4, 4, 4);
    panel.add(new JLabel("Server"), gc);
    gc.gridx++;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    panel.add(host, gc);

    gc.gridy++;
    gc.gridx = 0;
    gc.weightx = 0;
    gc.insets = new Insets(4, 4, 4, 4);
    panel.add(new JLabel("Username"), gc);
    gc.gridx++;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    panel.add(username, gc);

    gc.gridy++;
    gc.gridx = 0;
    gc.weightx = 0;
    gc.insets = new Insets(4, 4, 4, 4);
    panel.add(new JLabel("Password"), gc);
    gc.gridx++;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    panel.add(password, gc);

    var button = new JButton("Receive");
    button.addActionListener((ev) -> {
      try {
        System.out.println("Start");
        var cards = receive(host.getText(), username.getText(), password.getText());
        cards.forEach((card) -> messages.append(card));
        System.out.println("End");
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    gc.gridy++;
    gc.gridx = 0;
    gc.gridwidth = 2;
    gc.weighty = 1;
    panel.add(new JScrollPane(messages), gc);

    gc.weighty = 0;
    gc.gridy++;
    panel.add(button, gc);

    frame.add(panel);

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(850, 500));
    frame.pack();
    frame.setLocationRelativeTo(null); // Center
    frame.setVisible(true);
  }

  /**
   * Start retrieving emails for account.
   * @param host Mail host
   * @param username User name
   * @param password User password
   * @return List of emails
   */
  public List<String> receive(String host, String username, String password)
  throws MessagingException, IOException {
    var properties = new Properties();
    properties.put("mail.store.protocol", "pop3");
    properties.put("mail.pop3.port", "995");
    properties.put("mail.pop3.starttls.enable", "true");

    var session = Session.getDefaultInstance(properties);
    session.setDebug(true);

    var cards = new ArrayList<String>();
    try (var store = session.getStore("pop3s")) {
      store.connect(host, username, password);

      try (
        var inbox = store.getFolder("INBOX");
        var br = new BufferedReader(new InputStreamReader(System.in));
      ) {
        inbox.open(Folder.READ_ONLY);
        var messages = inbox.getMessages();

        for (var message : messages) {
          cards.add(createCard(message));
        }
      }
    }
    return cards;
  }

  /**
   * Create a string representation of the content in a message.
   * @param message Email message
   * @return message as string
   */
  public String createCard(Message message) throws MessagingException, IOException {
    var sb = new StringBuilder();
    sb.append(IntStream.range(0, 80).mapToObj((i) -> "=").collect(Collectors.joining()) + "\n");

    sb.append(String.format("From: %s\n", Arrays.stream(message.getFrom())
      .map(f -> InternetAddress.class.cast(f))
      .map(f -> f.toString())
      .collect(Collectors.joining(", "))));

    sb.append(String.format("Subject: %s\n", message.getSubject()));
    sb.append(String.format("ContentType: %s\n", message.getContentType().split(";")[0]));
    sb.append(IntStream.range(0, 80).mapToObj((i) -> "=").collect(Collectors.joining()) + "\n");

    return sb.toString();
  }

  public static void main(String[] paramArrayOfString) {
    new EmailReceiver();
  }
}
