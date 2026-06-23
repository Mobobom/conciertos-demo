package BLL;

import java.math.BigDecimal;

public class CompraMerchandisingResultado {

    private int compraId;
    private int pagoId;
    private int compraMerchandisingId;
    private BigDecimal total;
    private Merchandising merchandising;
    private int cantidad;

    public CompraMerchandisingResultado(int compraId, int pagoId, int compraMerchandisingId,
                                        BigDecimal total, Merchandising merchandising, int cantidad) {
        this.compraId = compraId;
        this.pagoId = pagoId;
        this.compraMerchandisingId = compraMerchandisingId;
        this.total = total;
        this.merchandising = merchandising;
        this.cantidad = cantidad;
    }

    public int getCompraId() { return compraId; }
    public int getPagoId() { return pagoId; }
    public int getCompraMerchandisingId() { return compraMerchandisingId; }
    public BigDecimal getTotal() { return total; }
    public Merchandising getMerchandising() { return merchandising; }
    public int getCantidad() { return cantidad; }

    @Override
    public String toString() {
        return "CompraMerchandisingResultado [compraId=" + compraId + ", pagoId=" + pagoId
                + ", compraMerchandisingId=" + compraMerchandisingId + ", total=" + total
                + ", merchandising=" + merchandising + ", cantidad=" + cantidad + "]";
    }
}

