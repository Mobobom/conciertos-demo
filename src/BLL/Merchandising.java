package BLL;

import java.math.BigDecimal;

public class Merchandising {

    private int id;
    private int conciertoId;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private String imagenUrl;

    public Merchandising() {
    }

    public Merchandising(int id, int conciertoId, String nombre, BigDecimal precio, int stock) {
        this(id, conciertoId, nombre, precio, stock, null);
    }

    public Merchandising(int id, int conciertoId, String nombre, BigDecimal precio, int stock,
                         String imagenUrl) {
        this.id = id;
        this.conciertoId = conciertoId;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.imagenUrl = imagenUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConciertoId() { return conciertoId; }
    public void setConciertoId(int conciertoId) { this.conciertoId = conciertoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    @Override
    public String toString() {
        return "Merchandising [id=" + id + ", conciertoId=" + conciertoId
                + ", nombre=" + nombre + ", precio=" + precio
                + ", stock=" + stock + ", imagenUrl=" + imagenUrl + "]";
    }
}
