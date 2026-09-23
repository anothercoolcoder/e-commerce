package uptc.dto;
import uptc.model.Producto;
public class RecomendacionDto {
    private Producto producto; private double relevancia; private String motivo;
    public RecomendacionDto() { }
    public RecomendacionDto(Producto producto,double relevancia,String motivo){this.producto=producto;this.relevancia=relevancia;this.motivo=motivo;}
    public Producto getProducto(){return producto;} public void setProducto(Producto v){producto=v;}
    public double getRelevancia(){return relevancia;} public void setRelevancia(double v){relevancia=v;}
    public String getMotivo(){return motivo;} public void setMotivo(String v){motivo=v;}
}
