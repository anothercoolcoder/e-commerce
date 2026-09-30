package uptc.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import uptc.exception.PersistenceException;
import uptc.model.Producto;

/**
 * DAO CSV del catálogo de productos.
 *
 * <p>Las columnas se ubican por el nombre que tienen en la cabecera, no por su
 * posición. Por eso lee tanto el archivo semilla (que trae columnas adicionales
 * como subcategoría o fecha de alta) como el archivo que guarda la aplicación.</p>
 */
public class ProductoCsvDao {
    private static final String HEADER =
            "id,sku,nombre,categoria,marca,precio,descuento,stock,calificacion,imagen,descripcion,activo";

    private final Path file;

    public ProductoCsvDao(Path file) {
        this.file = file;
    }

    /** Lee todos los productos, o devuelve una lista vacía si el archivo aún no existe. */
    public List<Producto> load() throws PersistenceException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<Producto> products = new ArrayList<>();
            Map<String, Integer> columns = null;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) {
                    continue;
                }
                List<String> values = split(lines.get(i));
                if (columns == null) {
                    columns = readHeader(values);
                    continue;
                }
                if (values.size() < columns.size()) {
                    throw new PersistenceException("Fila CSV incompleta en línea " + (i + 1));
                }
                products.add(toProducto(values, columns));
            }
            return products;
        } catch (IOException | NumberFormatException e) {
            throw new PersistenceException("No se pudo leer el catálogo CSV: " + e.getMessage(), e);
        }
    }

    /** Reemplaza el archivo por la lista completa de productos. */
    public void save(List<Producto> products) throws PersistenceException {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            List<String> lines = new ArrayList<>();
            lines.add(HEADER);
            for (Producto p : products) {
                lines.add(String.join(",",
                        quote(p.getId()), quote(p.getSku()), quote(p.getNombre()), quote(p.getCategoriaId()),
                        quote(p.getMarca()), Double.toString(p.getPrecio()), Double.toString(p.getDescuento()),
                        Double.toString(p.getStock()), Double.toString(p.getCalificacion()), quote(p.getImagen()),
                        quote(p.getDescripcion()), Boolean.toString(p.isActivo())));
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar el catálogo CSV: " + e.getMessage(), e);
        }
    }

    /** Asocia cada nombre de columna con su posición en la fila. */
    private Map<String, Integer> readHeader(List<String> header) throws PersistenceException {
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            String name = header.get(i).replace("﻿", "").trim().toLowerCase(Locale.ROOT);
            columns.put(name.equals("descuento_pct") ? "descuento" : name, i);
        }
        if (!columns.containsKey("id") || !columns.containsKey("nombre") || !columns.containsKey("precio")) {
            throw new PersistenceException("El CSV no tiene la cabecera esperada (id, nombre, precio...)");
        }
        return columns;
    }

    private Producto toProducto(List<String> values, Map<String, Integer> columns) {
        boolean active = !columns.containsKey("activo") || Boolean.parseBoolean(text(values, columns, "activo"));
        return new Producto(
                text(values, columns, "id"),
                text(values, columns, "sku"),
                text(values, columns, "nombre"),
                text(values, columns, "descripcion"),
                number(values, columns, "precio"),
                number(values, columns, "descuento"),
                number(values, columns, "stock"),
                text(values, columns, "categoria"),
                text(values, columns, "marca"),
                number(values, columns, "calificacion"),
                text(values, columns, "imagen"),
                active);
    }

    private String text(List<String> values, Map<String, Integer> columns, String column) {
        Integer index = columns.get(column);
        return index == null ? "" : values.get(index);
    }

    private double number(List<String> values, Map<String, Integer> columns, String column) {
        String value = text(values, columns, column);
        return value.isBlank() ? 0 : Double.parseDouble(value);
    }

    /** Encierra el texto entre comillas para que pueda contener comas; las comillas internas se duplican. */
    private String quote(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    /** Separa una línea por comas respetando los campos entre comillas. */
    private List<String> split(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                boolean escapedQuote = insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"';
                if (escapedQuote) {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }
}
