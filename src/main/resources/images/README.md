# Imágenes de productos

La carpeta `productos/` contiene una imagen por producto del catálogo. El nombre del archivo es el valor de la columna `imagen` de `productos.csv` (por ejemplo `001.png`).

`ImageLoader` las carga desde el classpath y las conserva en memoria. Si un producto no tiene imagen, o el archivo no existe, la tarjeta se muestra sin imagen.
