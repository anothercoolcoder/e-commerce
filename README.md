# e-commerce

## Sistema de Recomendación de Productos (E-commerce)
*Contexto*: Simular un motor de recomendación como el de Amazon, que organiza
productos y sugiere otros relacionados.
- Árbol usado (sugerida):
- Árbol de Decisión para clasificar preferencias del usuario.
- Árbol B+ para búsquedas rápidas en catálogo grande.
### Objetivos:
#### CRUD de productos y categorías.
- Registrar compras o interacciones del usuario.
- Generar recomendaciones basadas en historial.
#### Persistencia:
- JSON para datos de usuarios y compras.
- CSV para catálogo de productos.
#### Extras avanzados:
- Uso de estadísticas para mejorar recomendaciones.
- Interfaz en JavaFX con gráficos de tendencias.
- Soporte multiusuario (cada usuario tiene su propio historial).

## Requisitos minimos
### 1. Estructura de Capas:
- *model*: Clases del dominio (ej. Libro, Contacto, Producto).
- *controller*: Lógica del negocio (árboles, operaciones CRUD).
- *persistence*: Manejo de archivos JSON/XML/CSV.
- *utils*: Métodos auxiliares (validaciones, conversores).
- *viewController*: Controladores de JavaFX (FXML + lógica visual).
- *resources-data*: Archivos de persistencia de prueba.
- *i18n*: Archivos .properties para idiomas.
- *images*: Íconos, logos, portadas.
### 2. Buenas Prácticas:
- Uso de Maven para dependencias.
- JavaDoc en clases públicas.
- Patrones recomendados: MVC, DAO, Singleton (para gestor de persistencia).
- Manejo de excepciones personalizado.
### 3. Testing
- Construcción de pruebas unitarias con una cobertura mínima del 75% en lógica
de negocio.

# Autores
> Andres Niño, Cristian Diaz, Alejandro Rojas