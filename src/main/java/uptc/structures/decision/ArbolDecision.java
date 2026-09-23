package uptc.structures.decision;

import uptc.model.Preferencia;

public class ArbolDecision {
    private NodoDecision raiz;
    public ArbolDecision() { construirReglasIniciales(); }
    public void construirReglasIniciales() {
        NodoDecision precio = new NodoDecision(p -> p.getPrecioMaximo() < Double.MAX_VALUE, null, null, p -> {});
        raiz = new NodoDecision(p -> p.getCategoriaId() != null, precio, precio, p -> {});
    }
    public Preferencia evaluar(Preferencia preferencia) {
        if (preferencia == null) return new Preferencia();
        raiz.recorrer(preferencia); return preferencia;
    }
    public NodoDecision getRaiz() { return raiz; }
}
