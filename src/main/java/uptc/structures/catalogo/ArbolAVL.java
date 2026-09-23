package uptc.structures.catalogo;
import java.util.*;
import uptc.model.Producto;
public class ArbolAVL implements ArbolCatalogo {
    private static class Nodo { Producto p; Nodo izq,der; int altura=1; Nodo(Producto p){this.p=p;} }
    private Nodo raiz;
    public void insertar(Producto p){if(p==null)throw new IllegalArgumentException("El producto no puede ser null");raiz=insertar(raiz,p);}
    private Nodo insertar(Nodo n,Producto p){if(n==null)return new Nodo(p);if(p.getId()<n.p.getId())n.izq=insertar(n.izq,p);else if(p.getId()>n.p.getId())n.der=insertar(n.der,p);else{n.p=p;return n;}return balancear(n);}
    public Producto buscar(int id){Nodo n=raiz;while(n!=null){if(id==n.p.getId())return n.p;n=id<n.p.getId()?n.izq:n.der;}return null;}
    public void actualizar(Producto p){if(buscar(p.getId())==null)throw new NoSuchElementException("Producto inexistente: "+p.getId());insertar(p);}
    public Producto eliminar(int id){Producto old=buscar(id);if(old!=null)raiz=eliminar(raiz,id);return old;}
    private Nodo eliminar(Nodo n,int id){if(n==null)return null;if(id<n.p.getId())n.izq=eliminar(n.izq,id);else if(id>n.p.getId())n.der=eliminar(n.der,id);else{if(n.izq==null)return n.der;if(n.der==null)return n.izq;Nodo s=n.der;while(s.izq!=null)s=s.izq;n.p=s.p;n.der=eliminar(n.der,s.p.getId());}return balancear(n);}
    public List<Producto> inOrder(){List<Producto> r=new ArrayList<>();inOrder(raiz,r);return r;}
    private void inOrder(Nodo n,List<Producto> r){if(n!=null){inOrder(n.izq,r);r.add(n.p);inOrder(n.der,r);}}
    public List<Producto> buscarPorRango(double min,double max){List<Producto> r=new ArrayList<>();for(Producto p:inOrder())if(p.getPrecio()>=min&&p.getPrecio()<=max)r.add(p);return r;}
    private int h(Nodo n){return n==null?0:n.altura;} private void actualizarAltura(Nodo n){n.altura=1+Math.max(h(n.izq),h(n.der));} private int factor(Nodo n){return h(n.izq)-h(n.der);}
    private Nodo balancear(Nodo n){actualizarAltura(n);int f=factor(n);if(f>1){if(factor(n.izq)<0)n.izq=rotarIzq(n.izq);return rotarDer(n);}if(f< -1){if(factor(n.der)>0)n.der=rotarDer(n.der);return rotarIzq(n);}return n;}
    private Nodo rotarDer(Nodo y){Nodo x=y.izq,t=x.der;x.der=y;y.izq=t;actualizarAltura(y);actualizarAltura(x);return x;} private Nodo rotarIzq(Nodo x){Nodo y=x.der,t=y.izq;y.izq=x;x.der=t;actualizarAltura(x);actualizarAltura(y);return y;}
}
