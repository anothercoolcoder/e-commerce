package uptc.service;
import java.util.*;
import uptc.dto.RecomendacionDto; import uptc.model.*; import uptc.structures.decision.ArbolDecision;
public class RecomendacionServiceImpl implements RecomendacionService {
 private final CatalogoService catalogo; private final HistorialService historial; private final ArbolDecision decisiones;
 public RecomendacionServiceImpl(CatalogoService c,HistorialService h,ArbolDecision d){catalogo=Objects.requireNonNull(c);historial=Objects.requireNonNull(h);decisiones=Objects.requireNonNull(d);}
 public List<RecomendacionDto> recomendar(int usuarioId,int limite){if(limite<=0)return List.of();List<Interaccion> eventos=historial.consultar(usuarioId);Map<Integer,Double> puntuacion=new HashMap<>();Set<Integer> comprados=new HashSet<>();for(Interaccion e:eventos){double p=e.getPeso()>0?e.getPeso():HistorialServiceImpl.peso(e.getTipo());puntuacion.merge(e.getProductoId(),p,Double::sum);if(e.getTipo()==TipoInteraccion.COMPRA)comprados.add(e.getProductoId());}
  Preferencia pref=preferencia(eventos,puntuacion);decisiones.evaluar(pref);List<RecomendacionDto> r=new ArrayList<>();for(Producto p:catalogo.listar()){if(!p.isActivo()||p.getStock()<=0||comprados.contains(p.getId()))continue;double score=puntuacion.getOrDefault(p.getId(),0d);if(pref.getCategoriaId()!=null&&pref.getCategoriaId()==p.getCategoriaId())score+=3;if(pref.getMarca()!=null&&pref.getMarca().equalsIgnoreCase(p.getMarca()))score+=2;if(score==0)score=.1;r.add(new RecomendacionDto(p,score,motivo(p,pref)));}r.sort(Comparator.comparingDouble(RecomendacionDto::getRelevancia).reversed());return r.subList(0,Math.min(limite,r.size())); }
 private Preferencia preferencia(List<Interaccion> eventos,Map<Integer,Double> score){
  Preferencia p=new Preferencia(); Map<Integer,Double> categorias=new HashMap<>(); Map<String,Double> marcas=new HashMap<>();
  for(Interaccion e:eventos){Producto producto=catalogo.buscar(e.getProductoId());if(producto==null)continue;double peso=e.getPeso()>0?e.getPeso():HistorialServiceImpl.peso(e.getTipo());categorias.merge(producto.getCategoriaId(),peso,Double::sum);if(producto.getMarca()!=null)marcas.merge(producto.getMarca(),peso,Double::sum);}
  categorias.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(x->p.setCategoriaId(x.getKey()));
  marcas.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(x->p.setMarca(x.getKey()));
  return p;
 }
 private String motivo(Producto p,Preferencia pref){if(pref.getCategoriaId()!=null&&pref.getCategoriaId()==p.getCategoriaId())return "Pertenece a una categoría de interés";return "Producto disponible del catálogo";}
}
