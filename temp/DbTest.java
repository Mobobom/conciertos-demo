package temp;
import DLL.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
public class DbTest {
    public static void main(String[] args) {
        try {
            Connection conn = Conexion.getInstance().getConnectionOrThrow();
            System.out.println("DB OK: " + (conn != null && !conn.isClosed()));
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
