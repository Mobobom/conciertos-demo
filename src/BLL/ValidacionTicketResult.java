package BLL;

public class ValidacionTicketResult {

    private boolean valido;
    private String mensaje;
    private Ticket ticket;

    public ValidacionTicketResult(boolean valido, String mensaje, Ticket ticket) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.ticket = ticket;
    }

    public boolean isValido() { return valido; }
    public String getMensaje() { return mensaje; }
    public Ticket getTicket() { return ticket; }

    @Override
    public String toString() {
        return "ValidacionTicketResult [valido=" + valido + ", mensaje=" + mensaje
                + ", ticket=" + ticket + "]";
    }
}
