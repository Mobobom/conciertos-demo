package BLL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Compra {

    private int id;
    private int compradorId;
    private int conciertoId;
    private LocalDateTime fecha;
    private BigDecimal total;

    public Compra() {
    }

    public Compra(int id, int compradorId, int conciertoId,
                  LocalDateTime fecha, BigDecimal total) {
        this.id = id;
        this.compradorId = compradorId;
        this.conciertoId = conciertoId;
        this.fecha = fecha;
        this.total = total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCompradorId() { return compradorId; }
    public void setCompradorId(int compradorId) { this.compradorId = compradorId; }

    public int getConciertoId() { return conciertoId; }
    public void setConciertoId(int conciertoId) { this.conciertoId = conciertoId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    @Override
    public String toString() {
        return "Compra [id=" + id + ", compradorId=" + compradorId
                + ", conciertoId=" + conciertoId + ", fecha=" + fecha
                + ", total=" + total + "]";
    }
}
