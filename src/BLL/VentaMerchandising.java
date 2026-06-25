package BLL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaMerchandising {

    private final int detalleId;
    private final int compraId;
    private final LocalDateTime fecha;
    private final String concierto;
    private final String producto;
    private final String comprador;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal total;
    private final String metodoPago;
    private final String imagenUrl;

    public VentaMerchandising(int detalleId, int compraId, LocalDateTime fecha,
                              String concierto, String producto, String comprador,
                              int cantidad, BigDecimal precioUnitario, BigDecimal total,
                              String metodoPago, String imagenUrl) {
        this.detalleId = detalleId;
        this.compraId = compraId;
        this.fecha = fecha;
        this.concierto = concierto;
        this.producto = producto;
        this.comprador = comprador;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = total;
        this.metodoPago = metodoPago;
        this.imagenUrl = imagenUrl;
    }

    public int getDetalleId() { return detalleId; }
    public int getCompraId() { return compraId; }
    public LocalDateTime getFecha() { return fecha; }
    public String getConcierto() { return concierto; }
    public String getProducto() { return producto; }
    public String getComprador() { return comprador; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getTotal() { return total; }
    public String getMetodoPago() { return metodoPago; }
    public String getImagenUrl() { return imagenUrl; }
}
