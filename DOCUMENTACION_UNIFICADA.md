# ShopTree — documentación técnica

Aplicación de escritorio de comercio electrónico para Programación III: catálogo, usuarios con roles, carrito, compras, búsqueda y recomendaciones personalizadas. Este documento describe el código **tal como está**; si algo no coincide con el código, manda el código.

## 1. Cómo ejecutar

| Comando | Qué hace |
|---|---|
| `mvn javafx:run` | Abre la aplicación |
| `mvn clean test` | Compila, ejecuta las pruebas y genera el reporte de cobertura en `target/site/jacoco/index.html` |
| `mvn clean verify` | Lo anterior y además **falla** si la cobertura es menor al 75 % |

Requisitos: JDK 25 y Maven. Usuarios del archivo semilla: `admin@tienda.com` / `admin123` (ADMIN) y `gamer@correo.com` / `cliente123` (CLIENTE).

## 2. Arquitectura por capas

```text
Vista            resources/uptc/fxml/*.fxml + css
   ↓
viewController   controladores JavaFX (un controlador por pantalla) + ShopContext
   ↓
controller       lógica de negocio: CRUD, carrito, compras, recomendaciones, estadísticas
   ├── controller.tree        árbol B+ y árbol de decisión
   └── structures.catalogo    árbol AVL
   ↓
persistence      DAO CSV / JSON + PersistenceManager (Singleton)
   ↓
Datos            data/productos.csv, data/usuarios.json, data/compras.json
```

Transversales: `model` (clases del dominio, sin JavaFX), `exception` (excepciones propias) y `utils` (validaciones, hash, idioma, formato).

Reglas que se cumplen en el código:

- La vista no lee ni escribe archivos: todo pasa por `controller` → `persistence`.
- Las reglas de negocio (validaciones, stock, roles, totales) están en `controller`, `utils.Validaciones` y `model`, no en la vista.
- `model`, `controller` y `persistence` no importan JavaFX.

| Paquete | Clases |
|---|---|
| `uptc` | `App` (arranque JavaFX) |
| `uptc.model` | `Producto`, `Categoria`, `Usuario`, `RolUsuario`, `Compra`, `DetalleCompra`, `ItemCarrito`, `Interaccion`, `TipoInteraccion` |
| `uptc.controller` | `ProductoController`, `CategoriaController`, `UsuarioController`, `CarritoController`, `CompraController`, `RecomendacionController`, `EstadisticasController` |
| `uptc.controller.tree` | `BPlusTree`, `CatalogoBPlusIndex`, `PriceKey`, `DecisionTree`, `DecisionNode`, `DecisionContext`, `RecommendationProfile` |
| `uptc.structures.catalogo` | `ArbolAVL`, `ArbolCatalogo` (su interfaz), `NodoArbolCatalogo` |
| `uptc.persistence` | `PersistenceManager`, `ProductoCsvDao`, `UsuarioJsonDao`, `CompraJsonDao` |
| `uptc.exception` | `ValidationException`, `EntityNotFoundException`, `PersistenceException`, `ProductoNoEncontradoException`, `StockInsuficienteException`, `CarritoVacioException`, `AutenticacionException` |
| `uptc.utils` | `Validaciones`, `PasswordHasher`, `I18n`, `Formato`, y los auxiliares visuales `ThemeManager`, `Theme`, `ImageLoader` |
| `uptc.viewController` | `ShopContext`, `ViewNavigator` y un controlador por pantalla (`Login`, `Register`, `Store`, `Detail`, `Cart`, `Checkout`, `Account`, `Admin`, `Statistics`) |
| `uptc.viewController.components` | `ProductCard`, `PriceLabel`, `RatingStars`, `Badge`, `FavoriteButton`, `InteractionTracker` |

Patrones: **MVC** (FXML = vista, `viewController` = controlador de vista, `controller` + `model` = modelo y negocio), **DAO** (un DAO por archivo), **Singleton** (`PersistenceManager`) y **Observer** (el carrito avisa a las pantallas cuando cambia).

## 3. Flujo principal

```text
App.start
  → ShopContext.initialize()   crea una sola vez los controladores de negocio
  → login.fxml                 UsuarioController.login
  → store.fxml                 recomendaciones + búsqueda + catálogo
  → detail.fxml                detalle de un producto
  → cart.fxml                  CarritoController: líneas, subtotal, IVA, envío, total
  → checkout.fxml              CompraController.checkout → compras.json
```

`ShopContext` guarda el estado de la sesión (usuario activo, producto seleccionado, carrito) y entrega los mismos controladores a todas las pantallas; por eso lo que se agrega en la tienda aparece en el carrito. `ViewNavigator.go("store")` cambia de pantalla.

Ejemplo — agregar al carrito desde la tienda:

```text
clic en "Agregar al carrito" (ProductCard)
  → StoreViewController.addToCart
  → CarritoController.add(id, 1)
       ProductoController.find(id)   → árbol AVL
       valida producto activo y stock
       notifica a los listeners      → la tienda actualiza el contador del carrito
  → InteractionTracker registra la interacción CARRITO del usuario
```

## 4. Estructuras de datos

### 4.1 Árbol AVL — `structures.catalogo.ArbolAVL`

Árbol binario de búsqueda autobalanceado con clave `Producto.id`. **Dónde se usa:** `ProductoController.find(id)` y `exists(id)`, es decir, cada vez que el carrito, el checkout, las estadísticas o el recomendador necesitan un producto por su id.

- `insertar`, `buscar`, `eliminar`, `obtenerInOrder`, `vaciar`: O(log n) las tres primeras.
- Factor de balance = altura(izquierdo) − altura(derecho). Si sale de [-1, 1] se rota:
  - pesa a la izquierda → rotación simple a la derecha (o doble izquierda-derecha);
  - pesa a la derecha → rotación simple a la izquierda (o doble derecha-izquierda).
- Eliminar un nodo con dos hijos lo reemplaza por su sucesor (el menor del subárbol derecho).

### 4.2 Árbol B+ — `controller.tree.BPlusTree<K, V>`

Genérico. Los valores están solo en las hojas; los nodos internos guardan separadores; las hojas están enlazadas. Con orden *m*: un nodo interno tiene máximo *m* hijos y una hoja máximo *m − 1* claves; al superar el máximo el nodo se divide, y al eliminar un nodo que queda bajo su mínimo pide prestado a un hermano o se fusiona.

**Dónde se usa:** `CatalogoBPlusIndex` mantiene dos árboles B+ (orden 4):

| Índice | Clave | Consulta |
|---|---|---|
| por nombre | `nombre en minúsculas + "|" + id` | prefijo: todas las claves entre `"aud"` y `"aud" + último carácter` |
| por precio | `PriceKey(precio, id)` | rango de precio `[mínimo, máximo]` |

Ambas consultas son un **rango**: se baja una vez hasta la hoja del límite inferior y se avanza por las hojas enlazadas. El buscador de la tienda (`ProductoController.filter`) usa el índice por nombre; `searchPrice` usa el de precio.

¿Por qué AVL para el id y B+ para nombre y precio? El AVL es suficiente para encontrar **una** clave exacta; el B+ es mejor cuando se necesitan **muchas claves consecutivas**, porque sus hojas enlazadas ya están en orden.

### 4.3 Árbol de decisión — `controller.tree.DecisionTree`

Clasifica al usuario en un perfil a partir de su historial. Cada nodo interno es una pregunta de sí/no y cada hoja un `RecommendationProfile`:

```text
¿Tiene historial?
├─ no → EXPLORADOR_NUEVO        (los mejor calificados)
└─ sí → ¿Tiene una marca preferida?
        ├─ sí → MARCA_PREFERIDA          (productos de esa marca)
        └─ no → ¿Ha hecho 5 compras o más?
                ├─ sí → COMPRADOR_FRECUENTE   (ofertas de su categoría favorita)
                └─ no → ¿Su precio promedio supera $500.000?
                        ├─ sí → CATEGORIA_PREMIUM     (gama alta de su categoría)
                        └─ no → CATEGORIA_ECONOMICA   (gama baja de su categoría)
```

**Dónde se usa:** `RecomendacionController.forUser(userId, limite)`:

1. Reúne el historial del usuario: sus compras (de `compras.json`) y sus interacciones de la sesión (clic, carrito). Cada una pesa según `TipoInteraccion.getPeso()` (una compra pesa 10, un clic 3).
2. Lo resume en un `DecisionContext`: categoría con más peso, precio promedio, marca preferida (una marca presente en al menos dos productos y con al menos la mitad del peso) y número de compras.
3. El árbol clasifica el contexto en un perfil.
4. Se recomiendan los productos activos, con stock, no comprados antes y que cumplen el perfil, ordenados por calificación. Si el perfil no encuentra ninguno se recomiendan los mejor calificados.

La tienda muestra el resultado en "Recomendados para ti" junto con el camino recorrido por el árbol (`DecisionTree.explain`).

## 5. Reglas de negocio por controlador

**`ProductoController`** — CRUD del catálogo.
`create`, `update` y `delete` exigen un usuario ADMIN (`Validaciones.admin`) y un producto válido (`Validaciones.producto`: id, SKU y nombre obligatorios, precio > 0, stock ≥ 0, descuento 0–100, calificación 0–5). Id y SKU son únicos. `delete` es eliminación lógica: marca el producto como inactivo para no romper las compras que lo referencian. `reduceStock` lo usa el checkout y no exige ADMIN. Cada cambio se guarda en el CSV.

**`CategoriaController`** — CRUD de categorías con jerarquía padre-hijo (en memoria; las categorías iniciales salen del catálogo). Solo ADMIN. No permite padre inexistente ni eliminar una categoría con subcategorías.

**`UsuarioController`** — `register` valida datos y correo, rechaza id o correo repetidos y guarda el hash SHA-256 de la contraseña. `login` exige usuario activo y contraseña correcta; el error es siempre "Credenciales inválidas".

**`CarritoController`** — `add` valida cantidad positiva, producto existente y activo, y stock suficiente (contando lo que ya hay en el carrito). `subtotal()` suma `precioFinal × cantidad`; `tax()` = 19 % del subtotal; `total()` = subtotal + IVA + envío ($12.000), o 0 si está vacío. Avisa a sus listeners en cada cambio.

**`CompraController`** — `checkout` rechaza el carrito vacío, revisa el stock de **todas** las líneas antes de descontar nada, descuenta stock, guarda el pedido con el precio pagado y vacía el carrito.

**`EstadisticasController`** — ventas totales, ticket promedio y unidades vendidas por categoría.

`Producto.precioFinal()` = `precio × (1 − descuento / 100)`.

## 6. Persistencia

| Archivo | DAO | Contenido |
|---|---|---|
| `data/productos.csv` | `ProductoCsvDao` | catálogo |
| `data/usuarios.json` | `UsuarioJsonDao` | usuarios |
| `data/compras.json` | `CompraJsonDao` | pedidos |

- `PersistenceManager.getInstance()` es el único punto de acceso a los DAO (Singleton). La carpeta es `data`; las pruebas la cambian con la propiedad de sistema `shoptree.data`.
- Si falta algún archivo (instalación nueva) se copia desde `src/main/resources/resources-data`.
- El CSV ubica las columnas por el nombre de la cabecera, por eso lee tanto el archivo semilla (con columnas extra) como el que guarda la aplicación. Los textos se guardan entre comillas para admitir comas.
- Los JSON usan Jackson. Al leer se aceptan los nombres del archivo semilla (`email`, `password`, `items`, fecha sin hora).
- Archivo inexistente → lista vacía. Contenido inválido → `PersistenceException`.

## 7. Excepciones

| Excepción | Tipo | Cuándo |
|---|---|---|
| `ValidationException` | comprobada | datos inválidos, duplicados, rol sin permiso |
| `EntityNotFoundException` | comprobada | categoría inexistente |
| `PersistenceException` | comprobada | error al leer o escribir archivos |
| `ProductoNoEncontradoException` | no comprobada | producto inexistente o inactivo |
| `StockInsuficienteException` | no comprobada | se piden más unidades que el stock |
| `CarritoVacioException` | no comprobada | checkout sin productos |
| `AutenticacionException` | no comprobada | credenciales inválidas |

Los controladores de vista las capturan y muestran `getMessage()` en una etiqueta de la pantalla.

## 8. Interfaz JavaFX

| FXML | Controlador | Se llega desde |
|---|---|---|
| `login.fxml` | `LoginViewController` | arranque, "Salir" |
| `register.fxml` | `RegisterViewController` | login |
| `store.fxml` | `StoreViewController` | login / registro |
| `detail.fxml` | `DetailViewController` | clic en una tarjeta |
| `cart.fxml` | `CartViewController` | botón Carrito |
| `checkout.fxml` | `CheckoutViewController` | carrito |
| `account.fxml` | `AccountViewController` | botón Mi cuenta |
| `admin.fxml` | `AdminViewController` | botón Admin (solo ADMIN) |
| `statistics.fxml` | `StatisticsViewController` | panel de administración |

- **Observer:** `StoreViewController` y `CartViewController` se suscriben al carrito (`addListener`) y se dan de baja al salir de la pantalla (`removeListener`).
- **Roles:** la tienda solo deja abrir el panel Admin a un ADMIN y, además, `ProductoController` vuelve a validar el rol: ocultar un botón no es seguridad.
- **Idiomas:** `I18n.text("clave")` lee `resources/uptc/i18n/messages_es|en|pt.properties`. Están traducidos el login y los textos principales de la tienda.
- **Temas:** `ThemeManager` carga `base.css`, `light.css` o `dark.css` (variables de color) y `components.css`.
- `module-info.java` abre `uptc.viewController` a `javafx.fxml` (inyección de `@FXML`) y `uptc.model` a Jackson.

## 9. Pruebas

JUnit 5, sin mocks: cada prueba usa objetos reales y archivos en una carpeta temporal (`@TempDir`). Estructura *preparar → actuar → comprobar*.

| Clase de prueba | Qué demuestra |
|---|---|
| `tree.ArbolAVLTest` | insertar, buscar, reemplazar, las 4 rotaciones, los 3 casos de eliminación, rebalanceo al eliminar, altura logarítmica |
| `tree.BPlusTreeTest` | inserción con divisiones, separador en la raíz, rangos, reemplazo, eliminación con préstamo y fusión (órdenes 3 a 7), casos límite |
| `tree.CatalogoBPlusIndexTest` | prefijos, rangos de precio, nombres repetidos, eliminar, vaciar |
| `tree.DecisionTreeTest` | cada hoja del árbol, contexto con nulos, camino explicado |
| `controller.ProductoControllerTest` | producto válido/inválido, duplicados, búsquedas, actualización, desactivación, stock, roles |
| `controller.CarritoControllerTest` | agregar, cantidad inválida, stock, inexistente, inactivo, eliminar, vaciar, subtotal, impuesto, total, Observer |
| `controller.CompraControllerTest` | checkout, precio pagado, persistencia, carrito vacío, stock insuficiente, historial por usuario |
| `controller.UsuarioControllerTest` | registro, hash, duplicados, login correcto/incorrecto, usuario inactivo |
| `controller.CategoriaControllerTest` | CRUD, jerarquía, roles |
| `controller.RecomendacionControllerTest` | sin historial, cada perfil, exclusión de comprados/agotados, historial por usuario |
| `controller.EstadisticasControllerTest` | ventas, ticket promedio, unidades por categoría |
| `persistence.PersistenceDaoTest` | guardar/leer CSV y JSON, formato semilla, archivo inexistente, contenido inválido |
| `persistence.PersistenceManagerTest` | Singleton y carpeta configurable |
| `persistence.SeedCatalogIntegrationTest` | carga de los datos semilla reales (240 productos, 5 usuarios, 49 compras) |
| `utils.*Test`, `model.ModeloTest` | validaciones, hash, idiomas, formato de moneda, precio final |
| `view.FxmlViewsTest` | carga real de las 9 pantallas FXML con sus controladores (se omite si no hay pantalla) |

## 10. Cobertura con JaCoCo

Configuración en `pom.xml` (plugin `jacoco-maven-plugin` 0.8.14):

| Ejecución | Fase Maven | Qué hace |
|---|---|---|
| `prepare-agent` | `initialize` | conecta el agente que registra el código ejecutado por las pruebas |
| `report` | `test` | genera `target/site/jacoco/index.html` |
| `check` | `verify` | falla el build si LINE o INSTRUCTION del proyecto quedan bajo `jacoco.minimo` (0.75) |

**Qué se mide:** toda la lógica de negocio — `model`, `controller`, `controller.tree`, `structures`, `persistence`, `exception` y `utils`.

**Qué se excluye y por qué:** solo el código que únicamente dibuja la interfaz y necesita una ventana abierta: `App`, `viewController/**` y los auxiliares visuales `utils.ThemeManager`, `utils.Theme` y `utils.ImageLoader`. No se excluye ninguna clase de negocio.

Medición del 30 de septiembre de 2026 (`mvn clean verify`, 169 pruebas): líneas **95,4 %**, instrucciones **97,0 %**, ramas **95,1 %**. Solo `controller` + árboles: líneas 99,7 %. Si no se excluyera nada (contando también el código visual), líneas 82,7 %. El dato vigente es siempre el de `target/site/jacoco/index.html`.

Para comprobar que el umbral funciona: `mvn verify -Djacoco.minimo=0.99` debe terminar en `BUILD FAILURE`.

## 11. Limitaciones conocidas

- Las interacciones (clic, carrito) viven solo durante la sesión; lo que se persiste por usuario son las compras. El campo `interacciones` que trae `usuarios.json` semilla no se usa.
- La búsqueda de la tienda es por **prefijo del nombre** (es la consulta que resuelve el árbol B+), no por texto contenido.
- Las categorías se administran en memoria; no tienen archivo propio.
- El panel de administración permite cambiar stock y desactivar productos; crear productos y administrar categorías solo está disponible en la capa de negocio (y en sus pruebas).
- "Continuar como invitado" entra con el primer usuario CLIENTE del archivo.
