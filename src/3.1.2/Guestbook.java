import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
 * Assignment 3.1.2,
 * Database connection.
 *
 * A simple HTML browser for viewing raw HTML.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class Guestbook {
  private Pattern xmlTagPattern = Pattern.compile("<.*>");
  private Sql sql;

  private JFrame frame = new JFrame();
  private JTextField nameField = new JTextField();
  private JTextField emailField = new JTextField();
  private JTextField homepageField = new JTextField("http://");
  private JTextField commentField = new JTextField();

  private JButton button = new JButton("Post");
  private JTextArea area = new JTextArea("Loading posts...");

  /** Constructor */
  public Guestbook() {
    this.sql = new Sql("atlas.dsv.su.se", "db_16157620", "usr_16157620", "157620");
  }

  /** Builds the user interface */
  private void buildUI() {
    this.frame.setSize(640, 480);
    this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.frame.add(BorderLayout.CENTER, new JScrollPane(this.area));

    this.button.addActionListener((ActionEvent param1ActionEvent) -> {
      var name = this.removeHTML(this.nameField.getText());
      var email = this.removeHTML(this.emailField.getText());
      var homepage = this.removeHTML(this.homepageField.getText());
      var comment = this.removeHTML(this.commentField.getText());
      this.commentField.setText(null);

      addComment(name, email, homepage, comment);
      showComments();
    });


    this.area.setEditable(false);
    this.area.setFont(new Font("Monospaced", Font.PLAIN, 14));

    var panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    var gc = new GridBagConstraints();
    gc.gridx = gc.gridy = 0;
    gc.fill = GridBagConstraints.BOTH;

    gc.weightx = 0;
    gc.insets = new Insets(4, 4, 4, 4);
    panel.add(new JLabel("Name"), gc);

    gc.gridx++;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    panel.add(this.nameField, gc);

    gc.gridx = 0;
    gc.weightx = 0;
    gc.gridy++;
    gc.insets = new Insets(4, 4, 4, 4);
    panel.add(new JLabel("E-mail"), gc);

    gc.gridx++;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    panel.add(this.emailField, gc);

    gc.gridx = 0;
    gc.weightx = 0;
    gc.gridy++;
    gc.insets = new Insets(4, 4, 4, 4);
    panel.add(new JLabel("Homepage"), gc);

    gc.gridx++;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    panel.add(this.homepageField, gc);

    gc.gridx = 0;
    gc.weightx = 0;
    gc.gridy++;
    gc.insets = new Insets(4, 4, 4, 4);
    panel.add(new JLabel("Comment"), gc);

    gc.gridx++;
    gc.weightx = 1;
    gc.insets = new Insets(0, 0, 0, 0);
    panel.add(this.commentField, gc);

    gc.gridx = 0;
    gc.weightx = 0;
    gc.gridwidth = 2;
    gc.gridy++;
    panel.add(this.button, gc);

    this.frame.add(BorderLayout.NORTH, panel);

    this.frame.setSize(640, 480);
    this.frame.setVisible(true);
  }

  /**
   * Initialize the database by connecting and create table if not exists,
   * then retrieve message from the table (if any).
   */
  private void init() throws ReflectiveOperationException, SQLException {
    sql.connect(); // Connect to db
    initDb(); // Create table if it does not exist
    showComments(); // Retrieve messages from table
  }

  /**
   * Get current time as a formatted string.
   * @return current time as a string
   */
  /** Get current time as a formatted string */
  private String getTime() {
    var calendar = new GregorianCalendar();
    var year = String.format("%01d", calendar.get(GregorianCalendar.YEAR));
    var month = String.format("%01d", calendar.get(GregorianCalendar.MONTH));
    var day = String.format("%01d", calendar.get(GregorianCalendar.DAY_OF_MONTH));
    var hour = String.format("%01d", calendar.get(GregorianCalendar.HOUR_OF_DAY));
    var minute = String.format("%01d", calendar.get(GregorianCalendar.MINUTE));
    var second = String.format("%01d", calendar.get(GregorianCalendar.SECOND));
    return String.format("%s-%s-%s %s:%s:%s", year, month, day, hour, minute, second);
  }

  /**
   * Creates a guestbook table in the database if not exists.
   */
  private void initDb() throws SQLException {
    try {
      // StringBuilder is overkill for static strings but added it for code readability
      var query = new StringBuilder("create table if not exists guestbook ")
        .append("( \n")
        .append("id int primary key auto_increment, \n") // id (PK)
        .append("timestamp date not null, \n") // timestamp 
        .append("name varchar(80) not null, \n") // username
        .append("email varchar(80), \n") // email
        .append("homepage varchar(80), \n") // homepage
        .append("comment varchar(255) not null \n") // comment
        .append(")")
        .toString();

      this.sql.executeUpdate(query); // Perform DDL
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Add a new guestbook comment.
   * @param name The author name
   * @param email The author email
   * @param homepage The author homepage
   * @param comment The comment
   */
  private void addComment(String name, String email, String homepage, String comment) {
    try {
      var query = "insert into guestbook (timestamp, name, email, homepage, comment) values (?, ?, ?, ?, ?)";
      sql.executeUpdate(query, getTime(), name, email, homepage, comment); // Perform DML
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Retrieve all messages in the database and display them.
   */
  private void showComments() {
    this.area.setText(null);

    try {
      var query = "select * from guestbook order by timestamp"; // Perform DML
      var rs = this.sql.executeQuery(query);
      var md = rs.getMetaData();

      while (rs.next()) {
        this.area.append("\u250C" + IntStream.range(0, 38).mapToObj(i -> "\u2500").collect(Collectors.joining("")) + "\u2510\n");
        for (int i = 0; i < md.getColumnCount(); i++) {
          var column = md.getColumnName(i + 1);
          var value = rs.getObject(i + 1);
          this.area.append("\u2502" + String.format("%1$-38s", String.format("%s: %s", column, value)) + "\u2502\n");
        }
        this.area.append("\u2514" + IntStream.range(0, 38).mapToObj(i -> "\u2500").collect(Collectors.joining("")) + "\u2518\n\n");
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }

  /**
   * Remove all HTML tags. 
   * @param value Text that might contain HTML tags
   * @return text without tags
   */
  private String removeHTML(String value) {
    var m = xmlTagPattern.matcher(value);
    return m.replaceAll(""); // Remove
  }

  public static void main(String[] args) throws ReflectiveOperationException, SQLException {
    var guestbook = new Guestbook();
    guestbook.buildUI();
    guestbook.init();
  }
}
