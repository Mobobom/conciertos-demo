package DLL;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Conexion {

    private static final Logger LOGGER = Logger.getLogger(Conexion.class.getName());

    private static final String CONFIG_PATH = "config/app.properties";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties config = cargarConfiguracion();
        String host = config.getProperty("host", "localhost");
        String puerto = config.getProperty("puerto", "3306");
        String base = config.getProperty("base", "ticketing");
        String useSSL = config.getProperty("useSSL", "false");
        URL = "jdbc:mysql://" + host + ":" + puerto + "/" + base
            + "?useSSL=" + useSSL + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        USER = config.getProperty("usuario", "ticketing");
        PASSWORD = config.getProperty("contrasena", "");
    }

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

    private static Properties cargarConfiguracion() {
        Properties config = new Properties();
        try (InputStream in = new FileInputStream(CONFIG_PATH)) {
            config.load(in);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo leer {0}, se usaran valores por defecto.", CONFIG_PATH);
        }
        return config;
    }
}
