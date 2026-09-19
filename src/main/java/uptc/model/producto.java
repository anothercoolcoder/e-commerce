package uptc.model;

public class producto {
    double precio;
    int id;

    public producto(double precio, int id) {
        this.precio = precio;
        this.id = id;
    }
    public producto() {
    }
    public double getPrecio() {
        return precio;
    }
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
}
