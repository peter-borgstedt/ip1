import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
 * Assignment 3.2.1,
 * Email sending.
 *
 * A simple mail client that only sends emails.
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
public class EmailSender {
  private Properties sessionProperties = new Properties();

  private JFrame frame = new JFrame();

  private JTextField smtpHostField = new JTextField("smtp.yandex.com"); // Outgoing mail
  private JTextField smtpHostPortField = new JTextField("465");

  private JTextField userField = new JTextField("iprogramming@yandex.com");
  private JTextField passwordField = new JTextField("effectivejava");

  private JTextField toField = new JTextField("iprogramming@yandex.com");
  private JTextField fromField = new JTextField("iprogramming@yandex.com");

  private JTextField subjectField = new JTextField("Hejsan!");
  private JTextArea messageField = new JTextArea("Tja! Läget. Testar lite! Mvh. Peter");

  private JButton button = new JButton("Send");

  /** Constructor */
  public EmailSender() {
    // https://stackoverflow.com/a/32713693
    sessionProperties.put("mail.smtp.auth", "true");
    sessionProperties.put("mail.smtp.port", "465");
    sessionProperties.put("mail.smtp.ssl.enable", "true");

    this.setup();
    this.buildUI();
  }

  /** Setup miscellaneous event listeners */
  private void setup() {
    this.smtpHostPortField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped (KeyEvent e) {
        if (!Character.isDigit(e.getKeyChar())) { // Only allow digits
          e.consume() ;
        }
      }
    });

    this.button.addActionListener((ActionEvent param1ActionEvent) -> {
      try {
        var session = createSession();
        session.setDebug(true);

        sendMail(createSession());
        showMessageDialog(this.frame, "Message has been successfully sent", "Info", INFORMATION_MESSAGE);
      } catch(Exception e) {
        e.printStackTrace();
        showMessageDialog(this.frame, e.getMessage(), "Error", ERROR_MESSAGE);
      } 
    });
  }

  /** Build the graphic user interface and display it */
  private void buildUI() {
    this.messageField.setFont(new Font("Monospaced", Font.PLAIN, 14));

    var panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    var gc = new GridBagConstraints();
    gc.weightx = 0.5;
    gc.fill = 1;
    gc.gridx = gc.gridy = 0;

    gc.insets = new Insets(3, 3, 3, 3);
    panel.add(new JLabel("Server"), gc);
    gc.gridx++;
    panel.add(new JLabel("Port"), gc);
    gc.gridx++;

    gc.gridy++;
    gc.gridx = 0;
    gc.insets = new Insets(0, 0, 3, 0);
    panel.add(this.smtpHostField, gc);
    gc.gridx++;
    panel.add(this.smtpHostPortField, gc);

    gc.gridy++;
    gc.gridx = 0;
    gc.insets = new Insets(3, 3, 3, 3);
    panel.add(new JLabel("Username"), gc);
    gc.gridx++;
    panel.add(new JLabel("Password"), gc);

    gc.gridy++;
    gc.gridx = 0;
    gc.insets = new Insets(0, 0, 23, 0);
    panel.add(this.userField, gc);
    gc.gridx++;
    panel.add(this.passwordField, gc);


    gc.gridy++;
    gc.gridx = 0;
    gc.insets = new Insets(3, 3, 3, 3);
    panel.add(new JLabel("To"), gc);
    gc.gridx++;
    panel.add(new JLabel("From"), gc);

    gc.gridy++;
    gc.gridx = 0;
    gc.insets = new Insets(0, 0, 3, 0);
    panel.add(this.fromField, gc);
    gc.gridx++;
    panel.add(this.toField, gc);

    gc.gridy++;
    gc.gridx = 0;
    gc.insets = new Insets(3, 3, 3, 3);
    panel.add(new JLabel("Message"), gc);

    var buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(this.button);

    this.frame.add(BorderLayout.NORTH, panel);
    this.frame.add(BorderLayout.CENTER, new JScrollPane(this.messageField));
    this.frame.add(BorderLayout.SOUTH, buttonPanel);

    this.frame.setSize(640, 480);
    this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.frame.setLocationRelativeTo(null);
    this.frame.setVisible(true);
  }

  /** Create a SMTP session with given details */
  private Session createSession() {
    sessionProperties.put("mail.smtp.host", this.smtpHostField.getText());

    return Session.getDefaultInstance(sessionProperties, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        var username = EmailSender.this.userField.getText();
        var password = EmailSender.this.passwordField.getText();
        return new PasswordAuthentication(username, password);
      }
    });
  }


  /**
   * Send a message using the current SMTP (outgoing) session.
   * @param session The session to be used
   */
  private void sendMail(Session session) throws MessagingException {
    var message = createMessage(session);
    Transport.send(message); // Send message to outgoing SMTP server
  }

  /**
   * Create a message using the current SMTP (outgoing session.
   * @param session The session to be used
   * @return created message
   */
  private MimeMessage createMessage(Session session) throws MessagingException {
    var message = new MimeMessage(session);

    // From header
    message.setFrom(new InternetAddress(this.fromField.getText())); // This is the address connected to the session after authentication

    // To header (an array of recipient addresses)
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.toField.getText()));

    // Subject header
    message.setSubject(this.subjectField.getText());
    message.setText(this.messageField.getText());

    return message;
  }

  public static void main(String[] args) {
    new EmailSender();
  }
}
