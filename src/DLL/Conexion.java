package DLL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
        "jdbc:mysql://localhost:3306/ticketing?useSSL=false&serverTimezone=UTC";
    private static final String USER = "ticketing";
    private static final String PASSWORD = "Db_ticketing_2026!";

    private static Connection conect;
    private static Conexion instance;

    private Conexion() {
        try {
            connectOrThrow();
        } catch (SQLException e) {
            System.out.println("Conexion: error -> " + e.getMessage());
        }
    }

    public static synchronized Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (conect == null || conect.isClosed()) {
                connectOrThrow();
            }
        } catch (SQLException e) {
            System.out.println("Conexion: error -> " + e.getMessage());
        }
        return conect;
    }

    public synchronized Connection getConnectionOrThrow() throws SQLException {
        if (conect == null || conect.isClosed()) {
            connectOrThrow();
        }
        return conect;
    }

    private void connectOrThrow() throws SQLException {
        conect = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Conexion: conectado a " + URL);
    }
}
