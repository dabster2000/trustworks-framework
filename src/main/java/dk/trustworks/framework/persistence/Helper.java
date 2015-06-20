package dk.trustworks.framework.persistence;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides utility methods for the benchmark tests.
 */
public final class Helper {

    private static Helper instance;

    private final DataSource mysql;

    private final Sql2o sql2o;

    private Helper() {
        Properties properties = new Properties();
        try (InputStream in = Helper.class.getResourceAsStream("server.properties")) {
            properties.load(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mysql = Helper.newDataSource(
                properties.getProperty("mysql.uri"),
                properties.getProperty("mysql.user"),
                properties.getProperty("mysql.password"));

        sql2o = new Sql2o(mysql);
    }

    public static Helper createHelper() {
        return instance == null?instance = new Helper():instance;
    }

    public Sql2o getDatabase() {
        return sql2o;
    }

    /**
     * Constructs a new SQL data source with the given parameters.  Connections
     * to this data source are pooled.
     *
     * @param uri the URI for database connections
     * @param user the username for the database
     * @param password the password for the database
     * @return a new SQL data source
     */
    private static DataSource newDataSource(String uri,
                                    String user,
                                    String password) {

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                uri, user, password);
        //
        // This constructor modifies the connection pool, setting its connection
        // factory to this.  (So despite how it may appear, all of the objects
        // declared in this method are incorporated into the returned result.)
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                connectionFactory, null);

        GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        connectionPool.setMaxTotal(256);
        connectionPool.setMaxIdle(256);

        poolableConnectionFactory.setPool(connectionPool);

        return new PoolingDataSource<>(connectionPool);
    }
}
