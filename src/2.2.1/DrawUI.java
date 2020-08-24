import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 2.2.1,
 * Datagram sockets with unicast.
 *
 * Drawing board using a BufferedImage to optimize drawings.
 *
 * Minor hack in JFrame so resizing does not flicker with a black background,
 * a known problem with Swing.
 * 
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class DrawUI {
  @FunctionalInterface
  public interface OnDrawnListener {
    public void event(Point previousPoint, Point currentPoint, int pencelType, int pencelSize, Color color);
  }

  /** Paint brushes that can be used when drawing */
  enum Brush {
    OVAL, SQUARE, LINE
  }

  /** A nested class that contains logic for drawing on a surface. */
  class DrawingBoard extends JPanel {
    private transient BufferedImage img; // Surface to be drawn on
    private Color backgroundColor = Color.WHITE;

    /** Constructor */
    public DrawingBoard() {
      this.setBackground(backgroundColor);
      var mouseHandler = new MouseEvents();
      this.addMouseListener(mouseHandler);
      this.addMouseMotionListener(mouseHandler);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
      super.setBounds(x, y, width, height);
      this.init(width > 0 ? width : 1, height > 0 ? height : 1);
    }

    /** Initialize the drawing surface */
    public void init(int width, int height) {
      var newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      var g = newImg.createGraphics();
      clear(g, width, height);

      if (img != null) {
        paint(g);
      }
      this.img = newImg;
    }

    /**
     * Clear graphics.
     * @param g Graphics content that should cleared
     * @param width The width of clear surface
     * @param height The height of clear surface
     */
    private void clear(Graphics g, int width, int height) {
      g.setColor(backgroundColor);
      g.fillRect(0, 0, width, height);
    }

    /** Clear the drawing surface */
    public void clear() {
      var g = Graphics2D.class.cast(img.getGraphics());
      clear(g, img.getWidth(), img.getHeight());
    }

    private int getBrushSize() {
      try {
        return Integer.parseInt(brushSizeField.getText());
      } catch (NumberFormatException e) {
        // When this occurs the text input is not of numeric characters,
        // this happens because we hijacked the JTextField of the JSpinner
        // and the validation has not been processed, so if a user write some text
        // this is caught, however the JSpinner will validate it later and reset to
        // the previous value if wrong. When this happens get the previous value
        // by retrieving it from the JSpinner.
        return (int)brushSize.getValue(); // Has not yet been updated so it contains the previous value
      }
    }

    /**
     * Draw on surface.
     * @param previousPoint Previous drawing point
     * @param currentPoint Current drawing point
     * @param brush The brush to be used
     * @param pencelSize The pencel size
     * @param color The pencel color
     */
    public void draw(Point previousPoint, Point currentPoint, Brush brush, int pencelSize, Color color) {
      var bg = img.createGraphics();
      bg.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
      bg.setColor(color);

      if (brush == Brush.OVAL) {
        bg.fillOval(currentPoint.x, currentPoint.y, pencelSize, pencelSize);
      } else if (brush == Brush.SQUARE) {
        bg.fillRect(currentPoint.x, currentPoint.y, pencelSize, pencelSize);
      } else if (brush == Brush.LINE) {
        var x1 = previousPoint == null ? currentPoint.x : previousPoint.x;
        var y1 = previousPoint == null ? currentPoint.y : previousPoint.y;

        // Round tip and smooth corners
        // http://www.java2s.com/Code/Java/2D-Graphics-GUI/LineStyles.htm
        bg.setStroke(new BasicStroke(pencelSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        bg.draw(new Line2D.Float(x1, y1, currentPoint.x, currentPoint.y));
      }
      repaint();
    }

    @Override
    public void paint(Graphics g) {
      g.drawImage(img, 0, 0, null);
    }

    /** Custom mouse event handler used when drawing. */
    class MouseEvents extends MouseAdapter {
      private Point previousPoint = null;

      @Override
      public void mousePressed(MouseEvent event) {
        DrawingBoard.this.requestFocusInWindow();

        var brush = brushes.getItemAt(brushes.getSelectedIndex());
        var size = getBrushSize();
        var color = colorChooser.getColor();

        // Drawings and GUI stuff should be done in EDT which I've
        // been a bit lazy of doing, so here is an exception
        SwingUtilities.invokeLater(() -> {
          draw(previousPoint, event.getPoint(), brush, size, color);
          onDrawnListener.event(previousPoint, event.getPoint(), brush.ordinal(), size, color);
          previousPoint = event.getPoint();
        });
      }

      @Override
      public void mouseReleased(MouseEvent event) {
        previousPoint = null; // Only used when drawing with line
      }

      @Override
      public void mouseDragged(MouseEvent event) {
        mousePressed(event);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
    }
  }

  private JFrame frame = new JFrame();
  private DrawingBoard board = new DrawingBoard();
  private OnDrawnListener onDrawnListener;

  private JComboBox<Brush> brushes = new JComboBox<>(Brush.values());
  private JSpinner brushSize = new JSpinner(new SpinnerNumberModel(14, 1, 50, 1));

  // Get size directly from editor as the value updates when focus is lost which
  // emits a change event that is not fast enough to change the model, resulting in the first drawing
  // point using an old size value
  private JTextField brushSizeField = JSpinner.NumberEditor.class.cast(brushSize.getEditor()).getTextField();
  private JColorChooser colorChooser = new JColorChooser(Color.BLACK);

  /** Constructor */
  public DrawUI() {
    buildUI();
  }

  /** Draw the user interface */
  public void buildUI() {
    var toolbar = new JToolBar();
    toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
    toolbar.setFloatable(false);

    // Only show RGB panel
    for (var p : colorChooser.getChooserPanels()) {
      if (!p.getDisplayName().equals("RGB")) {
        colorChooser.removeChooserPanel(p);
      }
    }

    colorChooser.setOpaque(false);
    colorChooser.setPreviewPanel(new JPanel());
    colorChooser.setColor(Color.BLACK);

    var colorPreview = new JPanel();
    colorPreview.setPreferredSize(new Dimension(10, 10));
    colorPreview.setOpaque(true);
    colorPreview.setBackground(Color.BLACK);
    var colorButton = new JButton("Select color");

    var colorDialog = new JDialog(frame);
    colorDialog.setModal(true);
    colorDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    colorDialog.setResizable(false);
    colorDialog.add(colorChooser, BorderLayout.CENTER);

    var p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    var ok = new JButton("OK");

    ok.addActionListener(event -> {
      colorPreview.setBackground(colorChooser.getColor());
      colorDialog.setVisible(false);
    });

    p.add(ok);
    colorDialog.add(p, BorderLayout.SOUTH);
    colorDialog.pack();

    colorButton.addActionListener((event) -> {
      colorDialog.setLocationRelativeTo(frame); // Center to parent
      colorDialog.setVisible(true);
    });

    toolbar.add(brushes);
    toolbar.add(brushSize);
    toolbar.add(colorButton);
    toolbar.add(colorPreview);

    frame.add(toolbar, BorderLayout.NORTH);
    frame.add(board, BorderLayout.CENTER);

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(640, 480));
    frame.pack();
    frame.setResizable(false);
    frame.setLocationRelativeTo(null); // Center
    frame.setVisible(true);
  }

  /**
   * Is invoked when any drawing is dobe.
   * @param onDrawnListener Listener to be invoked
   */
  public void setOnDrawnListener(OnDrawnListener onDrawnListener) {
    this.onDrawnListener = onDrawnListener;
  }

  /**
   * Draw on surface.
   * @param previousPoint Previous drawing point
   * @param currentPoint Current drawing point
   * @param pencelType The pencel type
   * @param pencelSize The pencel size
   * @param color The pencel color
   */
  public void draw(Point previousPoint, Point currentPoint, int pencelType, int pencelSize, Color color) {
    board.draw(previousPoint, currentPoint, Brush.values()[pencelType], pencelSize, color);
  }
}
