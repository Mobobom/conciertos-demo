package DLL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Conexion {

    private static final Logger LOGGER = Logger.getLogger(Conexion.class.getName());

    private static final String URL =
        "jdbc:mysql://localhost:3306/ticketing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "ticketing";
    private static final String PASSWORD = "Db_ticketing_2026!";

    private static Connection conect;
    private static Conexion instance;

    private Conexion() {
        try {
            connectOrThrow();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos.", e);
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
            LOGGER.log(Level.SEVERE, "No se pudo obtener la conexion a la base de datos.", e);
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
        LOGGER.log(Level.INFO, "Conexion establecida con {0}", URL);
    }
}
