package BLL;

import java.math.BigDecimal;

public class CompraMerchandising {

    private int id;
    private int compraId;
    private int merchandisingId;
    private int cantidad;
    private BigDecimal precioUnitario;

    public CompraMerchandising() {
    }

    public CompraMerchandising(int id, int compraId, int merchandisingId,
                               int cantidad, BigDecimal precioUnitario) {
        this.id = id;
        this.compraId = compraId;
        this.merchandisingId = merchandisingId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCompraId() { return compraId; }
    public void setCompraId(int compraId) { this.compraId = compraId; }

    public int getMerchandisingId() { return merchandisingId; }
    public void setMerchandisingId(int merchandisingId) { this.merchandisingId = merchandisingId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    @Override
    public String toString() {
        return "CompraMerchandising [id=" + id + ", compraId=" + compraId
                + ", merchandisingId=" + merchandisingId + ", cantidad=" + cantidad
                + ", precioUnitario=" + precioUnitario + "]";
    }
}

