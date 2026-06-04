package temp;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbSetup {
    private static final String URL =
            "jdbc:mysql://localhost:3306/ticketing"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC"
                    + "&allowMultiQueries=true";
    private static final String USER = "ticketing";
    private static final String PASSWORD = "Db_ticketing_2026!";

    public static void main(String[] args) throws Exception {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            runScript(connection, "db/create_ticketing.sql");
            runScript(connection, "db/populate_ticketing.sql");
        }
        System.out.println("DB schema and demo data loaded.");
    }

    private static void runScript(Connection connection, String path) throws Exception {
        String sql = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            while (true) {
                try {
                    if (!statement.getMoreResults() && statement.getUpdateCount() == -1) {
                        break;
                    }
                } catch (SQLException e) {
                    throw new SQLException("Error after running " + path + ": " + e.getMessage(), e);
                }
            }
        }
        System.out.println("Applied " + path);
    }
}
