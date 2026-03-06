package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DbVerificationUtil {

    private static final Logger LOGGER = LogManager.getLogger(DbVerificationUtil.class);

    private DbVerificationUtil() {
    }

    public static List<Map<String, String>> queryRows(String jdbcUrl, String user, String password, String sql) {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnLabel(i).toLowerCase(), String.valueOf(rs.getObject(i)));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed: " + sql, e);
        }
        rows.forEach(row -> LOGGER.info("DB row: {}", row));
        return rows;
    }

    public static List<String> queryTableNames(String jdbcUrl, String user, String password, String schemaQuery) {
        List<String> tables = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             ResultSet rs = conn.createStatement().executeQuery(schemaQuery)) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed: " + schemaQuery, e);
        }
        LOGGER.info("DB tables: {}", tables);
        return tables;
    }
}
