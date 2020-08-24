import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment: 2.1.4
 * Stream sockets and Images.
 *
 * A image chat client using {@link ObjectInputStream}, {@link ObjectOutputStream}
 * and {@link BufferedImage}.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class ImageChatClient {
  private Socket socket;
  private ObjectOutputStream out;

  private JPanel images = new JPanel();

  /** Constructor */
  public ImageChatClient(String host, int port) throws IOException, UnknownHostException {
    this.socket = new Socket(host, port);
    this.out = new ObjectOutputStream(socket.getOutputStream());
    startReceiving();

    buildUI(host, port);
  }

  /** Builds the user interface */
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
    gc.gridwidth = 2;

    images.setLayout(new BoxLayout(images, BoxLayout.PAGE_AXIS));
    images.setBackground(new Color(250, 250, 250));

    var pane = new JScrollPane(images);
    pane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

    panel.add(pane, gc);
    var fc = new JFileChooser();
    fc.setFileFilter(new FileNameExtensionFilter("Image", "png", "jpg", "jpeg", "gif"));

    var browseButton = new JButton("Send an image");
    browseButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
    browseButton.addActionListener((event) -> {
      var result = fc.showOpenDialog(frame);
      if (result == JFileChooser.APPROVE_OPTION) {
        try {
          var bytes = Files.readAllBytes(fc.getSelectedFile().toPath());
          out.writeObject(new Storage(bytes));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    ++gc.gridy;
    gc.gridwidth = 1;
    gc.weighty = 0;
    gc.weightx = 1;
    gc.insets = new Insets(8, 8, 8, 8);
    panel.add(browseButton, gc);

    frame.add(panel);
    frame.setPreferredSize(new Dimension(480, 640));
    frame.pack();
    frame.setLocationRelativeTo(null); // Center
    browseButton.grabFocus(); // Set focus on field

    frame.setVisible(true);
  }

  /** Starts listening to incoming messages (images) */
  private void startReceiving() {
    new Thread(() -> {
      try {
        while(true) {
          var ois = new ObjectInputStream(socket.getInputStream());
          var storage = Storage.class.cast(ois.readObject());
          var bufferImage = ImageIO.read(new ByteArrayInputStream(storage.getData()));

          SwingUtilities.invokeLater(() -> addImage(bufferImage));
        }
      } catch (ClassNotFoundException | IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  /**
   * Adds an image to the content.
   * @param image Image to be added
   */
  private void addImage(BufferedImage image) {
    var imageLabel = new JLabel(new ImageIcon(image));
    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

    var imagePanel = new JPanel();
    imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
    imagePanel.add(imageLabel);

    if (images.getComponentCount() > 0) {
      // Remove the previous filler (a vertical glue) that pushes images up rather
      // than spreading the space between them
      images.remove(images.getComponentCount() - 1);

      // https://docs.oracle.com/javase/tutorial/uiswing/components/separator.html
      // Need to set explicitly an height otherwise the separator will fill spaces
      JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
      separator.setMaximumSize( new Dimension(Integer.MAX_VALUE, 1) );

      // Add some separation between images
      images.add(Box.createVerticalStrut(5));
      images.add(separator);
      images.add(Box.createVerticalStrut(5));
    }

    images.add(imagePanel);
    images.add(Box.createVerticalGlue()); // Add vertical (space) filler

    images.revalidate(); // Update UI changes

    // Scroll to bottom (after UI changes)
    // https://stackoverflow.com/a/6132046
    images.scrollRectToVisible(new Rectangle(0, images.getPreferredSize().height, 1, 1));
  }

  public static void main(String[] args) {
    var host = "atlas.dsv.su.se";
    var port = 4848;

    if (args.length > 0) {
      host = args[0];
    }

    if (args.length == 2) {
      port = Integer.parseInt(args[1]);
    }

    try {
      new ImageChatClient(host, port);
    } catch (UnknownHostException e) {
      System.err.println(String.format("Unknown host %s", host));
      System.exit(1);
    } catch (IOException e) {
      System.err.println(String.format("Could not connect to %s", host));
      System.exit(1);
    }
  }
}
