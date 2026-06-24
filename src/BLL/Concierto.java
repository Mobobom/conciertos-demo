package BLL;

import java.time.LocalDate;
import java.time.LocalTime;

public class Concierto {

    private int id;
    private String artista;
    private LocalDate fecha;
    private LocalTime hora;
    private String lugar;
    private int capacidadTotal;
    private int organizadorId;
    private String estado;
    private String posterUrl;

    private int disponibles;

    public Concierto() {
    }

    public Concierto(int id, String artista, LocalDate fecha, LocalTime hora,
                     String lugar, int capacidadTotal, int organizadorId, String estado) {
        this(id, artista, fecha, hora, lugar, capacidadTotal, organizadorId, estado, null);
    }

    public Concierto(int id, String artista, LocalDate fecha, LocalTime hora,
                     String lugar, int capacidadTotal, int organizadorId, String estado,
                     String posterUrl) {
        this.id = id;
        this.artista = artista;
        this.fecha = fecha;
        this.hora = hora;
        this.lugar = lugar;
        this.capacidadTotal = capacidadTotal;
        this.organizadorId = organizadorId;
        this.estado = estado;
        this.posterUrl = posterUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public int getCapacidadTotal() { return capacidadTotal; }
    public void setCapacidadTotal(int capacidadTotal) { this.capacidadTotal = capacidadTotal; }

    public int getOrganizadorId() { return organizadorId; }
    public void setOrganizadorId(int organizadorId) { this.organizadorId = organizadorId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    @Override
    public String toString() {
        return "Concierto [id=" + id + ", artista=" + artista + ", fecha=" + fecha
                + ", hora=" + hora + ", lugar=" + lugar
                + ", capacidadTotal=" + capacidadTotal + ", estado=" + estado
                + ", posterUrl=" + posterUrl
                + ", disponibles=" + disponibles + "]";
    }
}
