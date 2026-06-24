package BLL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaMerchandising {

    private int detalleId;
    private int compraId;
    private LocalDateTime fecha;
    private String concierto;
    private String producto;
    private String comprador;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total;
    private String metodoPago;

    public VentaMerchandising(int detalleId, int compraId, LocalDateTime fecha,
                              String concierto, String producto, String comprador,
                              int cantidad, BigDecimal precioUnitario,
                              BigDecimal total, String metodoPago) {
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
}
