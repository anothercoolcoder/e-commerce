package uptc.structures.catalogo;

import uptc.model.Producto;

public class NodoArbolCatalogo {

    private Producto producto;
    private NodoArbolCatalogo izquierdo;
    private NodoArbolCatalogo derecho;
    private int altura;

    public NodoArbolCatalogo(Producto producto) {
        this.producto = producto;
        this.izquierdo = null;
        this.derecho = null;
        this.altura = 1;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public NodoArbolCatalogo getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoArbolCatalogo izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoArbolCatalogo getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoArbolCatalogo derecho) {
        this.derecho = derecho;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }
}