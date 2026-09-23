package uptc.structures.decision;
import java.util.function.Consumer;
import uptc.model.Preferencia;
public class NodoDecision {
    private final ReglaPreferencia regla; private final NodoDecision si; private final NodoDecision no; private final Consumer<Preferencia> accion;
    public NodoDecision(ReglaPreferencia regla, NodoDecision si, NodoDecision no, Consumer<Preferencia> accion){this.regla=regla;this.si=si;this.no=no;this.accion=accion;}
    public void recorrer(Preferencia p){ if(accion!=null) accion.accept(p); else if(regla.cumple(p) && si!=null) si.recorrer(p); else if(no!=null) no.recorrer(p); }
    public ReglaPreferencia getRegla(){return regla;} public NodoDecision getSi(){return si;} public NodoDecision getNo(){return no;}
}
