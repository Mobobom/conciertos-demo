package BLL;

import java.math.BigDecimal;
import java.util.LinkedList;

public class CompraResultado {

    private int compraId;
    private int pagoId;
    private BigDecimal total;
    private LinkedList<Ticket> tickets;

    public CompraResultado(int compraId, int pagoId, BigDecimal total, LinkedList<Ticket> tickets) {
        this.compraId = compraId;
        this.pagoId = pagoId;
        this.total = total;
        this.tickets = tickets;
    }

    public int getCompraId() { return compraId; }
    public int getPagoId() { return pagoId; }
    public BigDecimal getTotal() { return total; }
    public LinkedList<Ticket> getTickets() { return tickets; }

    @Override
    public String toString() {
        return "CompraResultado [compraId=" + compraId + ", pagoId=" + pagoId
                + ", total=" + total + ", tickets=" + tickets + "]";
    }
}
