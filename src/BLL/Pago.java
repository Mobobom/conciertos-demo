package BLL;

import java.math.BigDecimal;

public class Pago {

    private int id;
    private int compraId;
    private String metodo;
    private BigDecimal monto;

    public Pago() {
    }

    public Pago(int id, int compraId, String metodo, BigDecimal monto) {
        this.id = id;
        this.compraId = compraId;
        this.metodo = metodo;
        this.monto = monto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCompraId() { return compraId; }
    public void setCompraId(int compraId) { this.compraId = compraId; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    @Override
    public String toString() {
        return "Pago [id=" + id + ", compraId=" + compraId
                + ", metodo=" + metodo + ", monto=" + monto + "]";
    }
}
