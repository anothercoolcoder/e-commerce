package uptc.mapper;

import uptc.dto.ProductoDto;
import uptc.model.Producto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProductoMapper {

    public static ProductoDto toDto(Producto producto) {
        if (producto == null) {
            return null;
        }

        ProductoDto dto = new ProductoDto();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(toStockInteger(producto.getStock()));
        dto.setCategoriaId(producto.getCategoriaId());
        dto.setMarca(producto.getMarca());
        dto.setEtiquetas(toEtiquetaList(producto.getEtiquetas()));
        dto.setActivo(producto.isActivo());

        return dto;
    }

    public static Producto toEntity(ProductoDto dto) {
        if (dto == null) {
            return null;
        }

        Producto producto = new Producto();
        producto.setId(dto.getId());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoriaId(dto.getCategoriaId());
        producto.setMarca(dto.getMarca());
        producto.setEtiquetas(toEtiquetaText(dto.getEtiquetas()));
        producto.setActivo(dto.isActivo());

        return producto;
    }

    private static List<String> toEtiquetaList(String etiquetas) {
        if (etiquetas == null || etiquetas.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(etiquetas.split(","))
                .map(String::trim)
                .filter(etiqueta -> !etiqueta.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String toEtiquetaText(List<String> etiquetas) {
        if (etiquetas == null) {
            return "";
        }

        return etiquetas.stream()
                .filter(etiqueta -> etiqueta != null && !etiqueta.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private static int toStockInteger(double stock) {
        if (stock < Integer.MIN_VALUE || stock > Integer.MAX_VALUE || stock != Math.rint(stock)) {
            throw new IllegalArgumentException("El stock debe ser un número entero dentro del rango permitido.");
        }
        return (int) stock;
    }
}
