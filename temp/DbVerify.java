package temp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbVerify {
    private static final String URL =
            "jdbc:mysql://localhost:3306/ticketing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "ticketing";
    private static final String PASSWORD = "Db_ticketing_2026!";

    public static void main(String[] args) throws Exception {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM usuario) AS usuarios, "
                + "(SELECT COUNT(*) FROM concierto) AS conciertos, "
                + "(SELECT COUNT(*) FROM sector) AS sectores, "
                + "(SELECT COUNT(*) FROM ticket) AS tickets";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            if (result.next()) {
                System.out.println("usuarios=" + result.getInt("usuarios"));
                System.out.println("conciertos=" + result.getInt("conciertos"));
                System.out.println("sectores=" + result.getInt("sectores"));
                System.out.println("tickets=" + result.getInt("tickets"));
            }
        }
    }
}
