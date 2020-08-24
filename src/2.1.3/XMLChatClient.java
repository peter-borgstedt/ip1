import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment: 2.1.3,
 * Stream sockets and XML.
 *
 * A XML chat client featuring {@link PrintWriter}, {@link InputStreamReader},
 * {@link SAXBuilder}, {@link Document} and {@link XMLOutputter}.
 *
 * Uses class {@SocketAdapter} shared between several assignments.
 *
 * External libraries:
 * jdom-2.0.6.jar (http://www.jdom.org/)
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class XMLChatClient {
  private final static String MESSAGE_DTD = "https://atlas.dsv.su.se/~pierre/courses/05_ass/ip1/2/2.1.3/message.dtd";
  private final static String DEFAULT_HOST = "atlas.dsv.su.se";
  private final static int DEFAULT_PORT = 9494;

  private SocketAdapter socket;
  private SAXBuilder builder;
  private XMLOutputter xmlOutCompact;
  private XMLOutputter xmlOutPretty;

  private XMLChatClientUI ui;

  /**
   * Initialize the XML chat client by connecting to the host.
   * @param host Host address
   * @param port Host port
   */
  public XMLChatClient(String host, int port) throws IOException, UnknownHostException {
    this.builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
    this.socket = new SocketAdapter(new Socket(host, port));
    // Compact layout for sending (less data to send)
    this.xmlOutCompact = new XMLOutputter(Format.getCompactFormat().setLineSeparator(""));
    // Pretty layout for printing (easier to read)
    this.xmlOutPretty = new XMLOutputter(Format.getPrettyFormat());
    this.ui = new XMLChatClientUI();

    // Send XML message
    ui.addActionListener((event) -> {
      try {
        var doc = XMLChatClient.createDoc(ui.getProperties());
        socket.write(xmlOutCompact.outputString(doc));
        print(doc, "OUTPUT");

        ui.clear(); // Clear fields
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    // Receive XML message
    socket.addOnIncomingListener((socket, data) -> {
      // The server seems to be sending connected and disconnected statuses
      // so we need to validate the incoming content to see what kind of message
      // it is before processing it
      if (data.startsWith("<?xml")) { // Is a XML message
        try {
          var doc = builder.build(new StringReader(data));
          print(doc, "INPUT");

          var m = XMLChatClient.parseDoc(doc);
          ui.append(String.format("%s (%s): %s\n", m.get("name"), m.get("email"), m.get("body")));
        } catch (IOException e) {
          ui.append(String.format("Failed to parse message: %s, catched error: %s\n", data, e));
        }
      } else if(data.startsWith("CLIENT ")) {
        // A client has connected or disconnected
        ui.append(String.format("%s\n", data));
      } else {
        System.err.println(String.format("Unsupported data format: %s", data));
      }
    });
  }

  /**
   * Print XML message to the console.
   * @param doc XML document
   * @param title Title of message
   * @throws IOException
   */
  private void print(Document doc, String title) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(IntStream.range(0, 25).mapToObj((i) -> "=").collect(Collectors.joining()) + "\n");
    sb.append(title + "\n");
    sb.append(IntStream.range(0, 25).mapToObj((i) -> "=").collect(Collectors.joining()) + "\n");
    sb.append(xmlOutPretty.outputString(doc));
    System.out.println(sb.toString());
  }

  /**
   * Create XML document using values of a map.
   * @param m Map with properties
   * @return created XML document
   */
  private static Document createDoc(Map<String, String> m) {
    var protocol = new Element("protocol")
      .addContent(new Element("type").addContent("CTTP"))
      .addContent(new Element("version").addContent("1.0"))
      .addContent(new Element("command").addContent("MESS"));

    var id = new Element("id")
      .addContent(new Element("name").addContent(m.get("name")))
      .addContent(new Element("email").addContent(m.get("email")))
      .addContent(new Element("homepage").addContent(m.get("homepage")))
      .addContent(new Element("host").addContent("unknown"));

    var header = new Element("header")
      .addContent(protocol)
      .addContent(id);

    var body = new Element("body").addContent(m.get("message"));

    var message = new Element("message");
      message.addContent(header);
      message.addContent(body);

    var type = new DocType("message", MESSAGE_DTD);
    return new Document(message, type);
  }

  /**
   * Parse a XML document into a map of key and values.
   * @param doc XML document
   * @return a map with key and values retrieved from the XML document
   */
  private static Map<String, String> parseDoc(Document doc) {
    var message = doc.getRootElement();
    var id = message.getChild("header").getChild("id");

    var m = new HashMap<String, String>(3);
    m.put("body", message.getChildText("body"));
    m.put("name", id.getChildText("name"));
    m.put("email", id.getChildText("email"));
    return m;
  }

  public static void main(String[] args) {
    var host = DEFAULT_HOST;
    var port = DEFAULT_PORT;

    if (args.length > 0) {
      host = args[0];
    }

    if (args.length == 2) {
      port = Integer.parseInt(args[1]);
    }

    try {
      new XMLChatClient(host, port);
    } catch (UnknownHostException e) {
      System.err.println("Unknown host: " + host);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Could not connect to: " + host);
      System.exit(1);
    }
  }
}
