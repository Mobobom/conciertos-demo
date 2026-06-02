package BLL;

import java.math.BigDecimal;

public class Ticket {

    private int id;
    private int conciertoId;
    private int sectorId;
    private String codigo;
    private BigDecimal precio;
    private String estado;
    private int compraId;

    public Ticket() {
    }

    public Ticket(int id, int conciertoId, int sectorId, String codigo,
                  BigDecimal precio, String estado, int compraId) {
        this.id = id;
        this.conciertoId = conciertoId;
        this.sectorId = sectorId;
        this.codigo = codigo;
        this.precio = precio;
        this.estado = estado;
        this.compraId = compraId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConciertoId() { return conciertoId; }
    public void setConciertoId(int conciertoId) { this.conciertoId = conciertoId; }

    public int getSectorId() { return sectorId; }
    public void setSectorId(int sectorId) { this.sectorId = sectorId; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getCompraId() { return compraId; }
    public void setCompraId(int compraId) { this.compraId = compraId; }

    @Override
    public String toString() {
        return "Ticket [id=" + id + ", conciertoId=" + conciertoId + ", sectorId=" + sectorId
                + ", codigo=" + codigo + ", precio=" + precio + ", estado=" + estado
                + ", compraId=" + compraId + "]";
    }
}
