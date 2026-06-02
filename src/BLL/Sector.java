package BLL;

import java.math.BigDecimal;

public class Sector {

    private int id;
    private int conciertoId;
    private String tipo;
    private String nombre;
    private int capacidad;
    private BigDecimal precio;
    private int disponibles;

    public Sector() {
    }

    public Sector(int id, int conciertoId, String tipo, String nombre,
                  int capacidad, BigDecimal precio) {
        this.id = id;
        this.conciertoId = conciertoId;
        this.tipo = tipo;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.precio = precio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConciertoId() { return conciertoId; }
    public void setConciertoId(int conciertoId) { this.conciertoId = conciertoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    @Override
    public String toString() {
        return "Sector [id=" + id + ", conciertoId=" + conciertoId + ", tipo=" + tipo
                + ", nombre=" + nombre + ", capacidad=" + capacidad
                + ", precio=" + precio + ", disponibles=" + disponibles + "]";
    }
}
