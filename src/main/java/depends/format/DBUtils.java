package depends.format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBUtils {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private Connection conn = null;
   // private PreparedStatement statement = null;

    private String sqlAccount = null;
    private String sqlPassword = null;
    private String sqlIP = null;
    private String sqlPort = null;
    public String dbName = null;

    public DBUtils(String dbConfigDir) {
        if (dbConfigDir == null) return;
        dbConfigurationReader(dbConfigDir);
        String databaseUrl = "jdbc:mysql://" + sqlIP + ":" + sqlPort + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Database connection is running...");
            conn = DriverManager.getConnection(databaseUrl, sqlAccount, sqlPassword);
            System.out.println("Database connection complete.");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC DRIVER not found! Please check if the driver is deployed successfully.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed! Maybe cause by wrong url/name/password.");
            e.printStackTrace();
        }
    }

    private void dbConfigurationReader(String dbConfigDir) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(dbConfigDir)));
            String[] rawConfigs = content.replace("\"", "").replace("{", "").replace("}", "").trim().split(",");
            for (String rawConfig : rawConfigs) {
                String value = rawConfig.split(":")[1].trim();
                if (rawConfig.contains("sql-account")) {
                    sqlAccount = value;
                } else if (rawConfig.contains("sql-password")) {
                    sqlPassword = value;
                } else if (rawConfig.contains("sql-ip")) {
                    sqlIP = value;
                } else if (rawConfig.contains("sql-port")) {
                    sqlPort = value;
                } else if (rawConfig.contains("db-name")) {
                    dbName = value;
                }
            }
        } catch (IOException e) {
            System.err.println("An error happened during reading configurations at " + dbConfigDir + "!");
            e.printStackTrace();
        }
    }

    public PreparedStatement getStatement(String sql) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(sql);
        return statement;
    }

    public static String generateInsertSql(String tableName, String[] attributes) {
        if (attributes.length == 0) return "";
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + "(" + attributes[0]);
        int num = attributes.length;
        for (int i = 1; i < num; i++) {
            sql.append(", ").append(attributes[i]);
        }
        sql.append(")").append(" VALUES (");
        for (int i = 0; i < num - 1; i++) {
            sql.append("?, ");
        }
        sql.append("?)");
        return sql.toString();
    }

    public void executeStatement(PreparedStatement statement) throws SQLException {
        if (statement != null) {
            statement.executeUpdate();
        }
    }

   // public void closeAll() {
   //     try {
   //         if (conn != null) {
   //             conn.close();
   //         }
   //         if (statement != null) {
   //             statement.close();
   //         }
   //     } catch (SQLException e) {
   //         System.err.println("Connection close goes wrong. Connection cannot be closed properly.");
   //         e.printStackTrace();
   //     }
   // }

    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Connection close goes wrong. The connection cannot be closed properly.");
            e.printStackTrace();
        }
    }

    public void closeStatement(PreparedStatement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            System.err.println("Statement close goes wrong. The statement cannot be closed properly.");
        }
    }
}
