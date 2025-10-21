package helpers.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Service class for H2 database operations in OpenL WebStudio.
 * Used for validation of database configuration and user management in multi-user mode.
 */
public class DBService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBService.class);

    /**
     * Get JDBC connection to H2 database running inside the container.
     * The database is accessible via mapped container port.
     *
     * @param containerMappedPort The host port mapped to container's 8095 port
     * @return Connection to H2 database
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection(int containerMappedPort) throws SQLException {
        // H2 database URL format for remote connection
        String dbUrl = String.format("jdbc:h2:tcp://localhost:%d/opt/openl/users-db/db", containerMappedPort);
        String dbUser = "";
        String dbPassword = "";

        LOGGER.debug("Connecting to H2 database at: {}", dbUrl);
        Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        LOGGER.info("Successfully connected to H2 database");
        return connection;
    }

    /**
     * Check if user exists in OPENL_USERS table.
     *
     * @param conn Connection to H2 database
     * @param username Username to check
     * @return true if user exists, false otherwise
     * @throws SQLException if query fails
     */
    public static boolean userExists(Connection conn, String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM OPENL_USERS WHERE USERNAME = ?";
        LOGGER.debug("Checking if user exists: {}", username);

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    LOGGER.info("User '{}' exists: {}", username, count > 0);
                    return count > 0;
                }
            }
        }
        return false;
    }

    /**
     * Get user details from database.
     *
     * @param conn Connection to H2 database
     * @param username Username to retrieve
     * @return ResultSet with user details (caller must close it)
     * @throws SQLException if query fails
     */
    public static ResultSet getUserDetails(Connection conn, String username) throws SQLException {
        String query = "SELECT * FROM OPENL_USERS WHERE USERNAME = ?";
        LOGGER.debug("Retrieving user details for: {}", username);

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, username);
        return stmt.executeQuery();
    }

    /**
     * Get current connection pool size from H2 system tables.
     * Note: This queries H2's internal settings, not the HikariCP pool size.
     *
     * @param conn Connection to H2 database
     * @return Current maximum pool size setting
     * @throws SQLException if query fails
     */
    public static int getConnectionPoolSize(Connection conn) throws SQLException {
        // Query H2 settings table for max connections
        String query = "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MAX_CONNECTIONS'";
        LOGGER.debug("Querying H2 connection pool size");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int poolSize = rs.getInt(1);
                LOGGER.info("H2 max connections: {}", poolSize);
                return poolSize;
            }
        }
        return -1; // Could not retrieve
    }

    /**
     * Count total number of users in OPENL_USERS table.
     *
     * @param conn Connection to H2 database
     * @return Total user count
     * @throws SQLException if query fails
     */
    public static int getUserCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM OPENL_USERS";
        LOGGER.debug("Counting total users");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                LOGGER.info("Total users in database: {}", count);
                return count;
            }
        }
        return 0;
    }

    /**
     * Get all groups for a specific user.
     *
     * @param conn Connection to H2 database
     * @param username Username to check
     * @return ResultSet with group details (caller must close it)
     * @throws SQLException if query fails
     */
    public static ResultSet getUserGroups(Connection conn, String username) throws SQLException {
        String query = "SELECT g.* FROM OPENL_GROUPS g " +
                       "JOIN OPENL_USER2GROUP ug ON g.GROUPNAME = ug.GROUPNAME " +
                       "WHERE ug.LOGINNAME = ?";
        LOGGER.debug("Retrieving groups for user: {}", username);

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, username);
        return stmt.executeQuery();
    }

    /**
     * Close database connection safely.
     *
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                LOGGER.debug("Database connection closed");
            } catch (SQLException e) {
                LOGGER.error("Error closing database connection", e);
            }
        }
    }

    /**
     * Execute a custom SQL query and return ResultSet.
     * Caller is responsible for closing the ResultSet and Statement.
     *
     * @param conn Connection to H2 database
     * @param query SQL query to execute
     * @return ResultSet with query results
     * @throws SQLException if query fails
     */
    public static ResultSet executeQuery(Connection conn, String query) throws SQLException {
        LOGGER.debug("Executing custom query: {}", query);
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * Execute a custom SQL update/insert/delete statement.
     *
     * @param conn Connection to H2 database
     * @param sql SQL statement to execute
     * @return Number of affected rows
     * @throws SQLException if statement fails
     */
    public static int executeUpdate(Connection conn, String sql) throws SQLException {
        LOGGER.debug("Executing update statement: {}", sql);
        try (Statement stmt = conn.createStatement()) {
            int affectedRows = stmt.executeUpdate(sql);
            LOGGER.info("Statement affected {} rows", affectedRows);
            return affectedRows;
        }
    }
}
