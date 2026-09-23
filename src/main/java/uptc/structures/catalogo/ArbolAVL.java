package uptc.structures.catalogo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uptc.model.Producto;

public class ArbolAVL implements ArbolCatalogo {
    private NodoArbolCatalogo raiz;
    private int tamano;
    public void insertar(Producto p) { if (p == null || p.getId() == null) throw new IllegalArgumentException("Producto e id obligatorios"); boolean nuevo = buscar(p.getId()).isEmpty(); raiz = insertar(raiz, p); if (nuevo) tamano++; }
    private NodoArbolCatalogo insertar(NodoArbolCatalogo n, Producto p) { if (n == null) return new NodoArbolCatalogo(p); int c=p.getId().compareTo(n.getProducto().getId()); if(c<0)n.setIzquierdo(insertar(n.getIzquierdo(),p)); else if(c>0)n.setDerecho(insertar(n.getDerecho(),p)); else {n.setProducto(p);return n;} return balancear(n); }
    public Optional<Producto> buscar(String id) { NodoArbolCatalogo n=raiz; while(n!=null){int c=id.compareTo(n.getProducto().getId());if(c==0)return Optional.of(n.getProducto());n=c<0?n.getIzquierdo():n.getDerecho();}return Optional.empty(); }
    public boolean eliminar(String id) { if(buscar(id).isEmpty())return false;raiz=eliminar(raiz,id);tamano--;return true; }
    private NodoArbolCatalogo eliminar(NodoArbolCatalogo n,String id){if(n==null)return null;int c=id.compareTo(n.getProducto().getId());if(c<0)n.setIzquierdo(eliminar(n.getIzquierdo(),id));else if(c>0)n.setDerecho(eliminar(n.getDerecho(),id));else{if(n.getIzquierdo()==null)return n.getDerecho();if(n.getDerecho()==null)return n.getIzquierdo();NodoArbolCatalogo s=n.getDerecho();while(s.getIzquierdo()!=null)s=s.getIzquierdo();n.setProducto(s.getProducto());n.setDerecho(eliminar(n.getDerecho(),s.getProducto().getId()));}return balancear(n);}
    public List<Producto> obtenerInOrder(){List<Producto> r=new ArrayList<>();inOrder(raiz,r);return r;}
    private void inOrder(NodoArbolCatalogo n,List<Producto> r){if(n!=null){inOrder(n.getIzquierdo(),r);r.add(n.getProducto());inOrder(n.getDerecho(),r);}}
    public List<Producto> buscarPorRangoPrecio(double min,double max){return obtenerInOrder().stream().filter(p->p.getPrecio()>=min&&p.getPrecio()<=max).toList();}
    public void vaciar(){raiz=null;tamano=0;} public int getTamano(){return tamano;} public boolean estaVacio(){return raiz==null;} public NodoArbolCatalogo getRaiz(){return raiz;}
    private int altura(NodoArbolCatalogo n){return n==null?0:n.getAltura();} private void actualizarAltura(NodoArbolCatalogo n){n.setAltura(1+Math.max(altura(n.getIzquierdo()),altura(n.getDerecho())));} private int factor(NodoArbolCatalogo n){return altura(n.getIzquierdo())-altura(n.getDerecho());}
    private NodoArbolCatalogo balancear(NodoArbolCatalogo n){actualizarAltura(n);int f=factor(n);if(f>1){if(factor(n.getIzquierdo())<0)n.setIzquierdo(rotarIzquierda(n.getIzquierdo()));return rotarDerecha(n);}if(f< -1){if(factor(n.getDerecho())>0)n.setDerecho(rotarDerecha(n.getDerecho()));return rotarIzquierda(n);}return n;}
    private NodoArbolCatalogo rotarDerecha(NodoArbolCatalogo y){NodoArbolCatalogo x=y.getIzquierdo(),t=x.getDerecho();x.setDerecho(y);y.setIzquierdo(t);actualizarAltura(y);actualizarAltura(x);return x;} private NodoArbolCatalogo rotarIzquierda(NodoArbolCatalogo x){NodoArbolCatalogo y=x.getDerecho(),t=y.getIzquierdo();y.setIzquierdo(x);x.setDerecho(t);actualizarAltura(x);actualizarAltura(y);return y;}
}
