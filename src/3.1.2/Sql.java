import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 3.1.2,
 * Database connection.
 *
 * Connect and execute queries to an mysql database host.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class Sql {
  private String url;
  private Connection connection;
  private Map<String, PreparedStatement> statements = new ConcurrentHashMap<>();


  /**
   * Constructor.
   * @param host Database host
   * @param db Database name
   * @param username Database username
   * @param password Database password
   */
  public Sql(String host, String db, String username, String password) {
    this.url = String.format("jdbc:mysql://%s/%s?user=%s&password=%s", host, db, username, password);
  }

  /** Connect to database */
  public void connect() throws ReflectiveOperationException, SQLException {
    // https://dev.mysql.com/doc/connector-j/5.0/en/connector-j-usagenotes-connect-drivermanager.html
    Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
    this.connection = DriverManager.getConnection(this.url);
  }

  /**
   * Creates and cache (minor optimization) a prepared statement.
   * @param query Query to be run.
   * @return a prepared statement
   */
  private PreparedStatement getPreparedStatement (String query) throws SQLException {
    if (statements.containsKey(query)) {
      var statement = statements.get(query);
      statement.clearParameters();
      return statement;
    }

    var statement = this.connection.prepareStatement(query);
    statements.put(query, statement); // Cache
    return statement;
  }

  /**
   * Run a query.
   * @param query Query to be run
   * @param parameters Parameters used in query
   * @return records and metadata resulted from the query
   */
  public ResultSet executeQuery (String query, Object ...parameters) throws SQLException {
    var statement = getPreparedStatement(query);
    for (int i = 0; i < parameters.length; i++) {
      statement.setObject(i, parameters[i]);
      statement.setObject(i + 1, parameters[i]);
    }
    return statement.executeQuery();
  }

  /**
   * Execute update.
   * @param query
   * @param parameters
   * @return the amount of affected rows
   */
  public int executeUpdate (String query, Object ...parameters) throws SQLException {
    var statement = getPreparedStatement(query);
    for (int i = 0; i < parameters.length; i++) {
      statement.setString(i + 1, parameters[i].toString());
    }
    return statement.executeUpdate();
  }
}
