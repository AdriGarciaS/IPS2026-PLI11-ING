# IPS2026-PL11-ING

Proyecto base del equipo PL11 para la asignatura IPS (curso 2026), desarrollado en tres sprints según las user stories.

Sigue el patrón **MVC** (Modelo-Vista-Controlador) y usa una base de datos **SQLite** (un único archivo, sin servidor que instalar) como ejemplo de acceso a datos. Al arrancar, la app abre la ventana del menú principal: una cabecera (hueco para el logo + título) y cuatro botones en una rejilla 2x2, que de momento no hacen nada, y un pie de página. A partir de aquí se irán conectando con las funcionalidades de cada sprint.

## Requisitos

Antes de nada, instalar en el ordenador:

- **JDK 21** (Java Development Kit). Se puede comprobar con `java -version` en una terminal.
- **Maven 3.9+**. Se puede comprobar con `mvn -v`. (IntelliJ y Eclipse traen Maven integrado, así que si vais a usar el IDE no hace falta instalarlo aparte).
- **Git**, para descargar el proyecto.

## Descargar el proyecto (para tus compañeros)

Clonar el repositorio con:

```bash
git clone https://github.com/AdriGarciaS/IPS2026-PLI11-ING
cd IPS2026-PLI11-ING
```

También se puede descargar como ZIP desde GitHub (botón verde "Code" → "Download ZIP") si alguien no quiere usar Git.

## Ejecutar el proyecto

### Opción A: desde terminal con Maven

```bash
# Compilar
mvn compile

# Ejecutar los tests
mvn test

# Generar el .jar ejecutable
mvn package

# Ejecutar el .jar generado
java -jar target/IPS2026-PL11-ING.jar
```

Al ejecutarlo debería abrirse la ventana del menú principal ("Football Club Management") con cuatro botones. La primera vez que arranca, se crea automáticamente el archivo `data/demo.db` con una tabla `personas` y unas filas de ejemplo — no hace falta instalar ni configurar nada de base de datos a mano.

### Opción B: desde IntelliJ IDEA

1. `File` → `Open...` y seleccionar la carpeta del proyecto (la que contiene el `pom.xml`).
2. IntelliJ detecta automáticamente que es un proyecto Maven y descarga las dependencias.
3. Abrir `src/main/java/com/ips2026/pl11/App.java` y pulsar el botón de ejecutar (▶) junto al método `main`.

### Opción C: desde Eclipse

1. `File` → `Import...` → `Maven` → `Existing Maven Projects`.
2. Seleccionar la carpeta del proyecto (la que contiene el `pom.xml`) y finalizar.
3. Eclipse descarga las dependencias automáticamente.
4. Clic derecho sobre `App.java` → `Run As` → `Java Application`.

## Base de datos

Al arrancar, la app ejecuta los scripts de `src/main/resources/db/`:

- `schema.sql`: creación de las tablas (usar siempre `CREATE TABLE IF NOT EXISTS`). Se ejecuta en cada arranque.
- `data.sql`: datos de ejemplo. Solo se cargan cuando la base de datos se crea desde cero.

Cada sentencia debe terminar en `;` y los comentarios van en líneas que empiecen por `--`.

En `config.properties`, la opción `bd.borrarAlCerrar` decide qué pasa al cerrar la app:

- `false` (por defecto): la base de datos `data/demo.db` se **mantiene** entre ejecuciones. Si cambiáis `schema.sql` o `data.sql`, borrad la carpeta `data/` para que se regenere.
- `true`: la base de datos se **borra** al cerrar, así que cada arranque empieza limpio con `schema.sql` + `data.sql`.

También se puede elegir al ejecutar, sin tocar el archivo:

```bash
java -Dbd.borrarAlCerrar=true -jar target/IPS2026-PL11-ING.jar
```

## Guía de estilo de las ventanas

Todas las ventanas deben tener el mismo aspecto. Para conseguirlo sin repetir código, el aspecto está centralizado en `view/common/Branding.java` y `view/common/HeaderPanel.java`:

- **Tema global**: `App` llama una vez a `Branding.installLookAndFeel()` (Nimbus con los colores del club). Todas las ventanas, diálogos (`JOptionPane`), tablas y botones lo heredan solos: **no cambiéis el look and feel ni pongáis colores "a mano"** en las ventanas.
- **Plantilla de una ventana nueva**:

```java
// Ventana secundaria: JDialog MODAL cuyo dueño es el menú principal, así el
// menú queda bloqueado hasta que se cierra esta ventana.
public class MiNuevaVentana extends JDialog {
    public MiNuevaVentana(MiControlador controlador, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("Título de la ventana");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850, 500));
        contentPane = new JPanel(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);                                   // icono + fondo del club
        contentPane.add(getPnHeader(), BorderLayout.NORTH);      // new HeaderPanel("Título", "Subtítulo")
        contentPane.add(getPnBody(), BorderLayout.CENTER);       // panel con EmptyBorder(14, 14, 14, 14)
        Branding.setInitialSize(this, 1200, 760, owner);         // tamaño por defecto, centrada en el menú
    }
}
```

Y desde el menú principal se abre con `new MiNuevaVentana(controlador, this).setVisible(true);`.

- **Colores y fuente**: usar siempre las constantes de `Branding` (`NAVY`, `GOLD`, `BACKGROUND`, `TEXT_MUTED`, `BORDER`, `FONT_FAMILY`).
- **Ventanas secundarias**: siempre `JDialog` modal con el menú como dueño (el menú queda bloqueado mientras están abiertas). Solo el menú principal es un `JFrame` con `EXIT_ON_CLOSE`.
- **Tamaño**: usar `Branding.setInitialSize(...)` (por defecto 1200x760 las ventanas secundarias y 1024x720 el menú; se reduce sola si la pantalla es más pequeña).
- **Layouts**: nada de layout absoluto (`null`). Usar `BorderLayout`, `GridBagLayout`, `GridLayout` o `FlowLayout`, con márgenes (`EmptyBorder`) de 12–14 px. Toda ventana debe ser redimensionable y tener un tamaño mínimo.
- **Agrupar contenido** en paneles con `TitledBorder` (como "Catalog" y "Cart" en la venta de merchandising).
- **Botones de acción** abajo a la derecha (`FlowLayout.RIGHT`): primero el secundario (Close/Cancel) y después el principal, en negrita. Poner mnemónicos (`setMnemonic`).
- **Tablas**: no editables, selección de una fila, ordenables (`setAutoCreateRowSorter(true)`), altura de fila 24 y precios con `MoneyFormat.tableRenderer()`.
- **Mensajes**: siempre con `JOptionPane` y la ventana como padre; pedir confirmación antes de acciones que guardan datos.
- **Textos de la interfaz en inglés**.

## Estructura del proyecto (MVC)

```
IPS2026-PL11-ING/
├── pom.xml                                        # Configuración de Maven (dependencias, plugins, versión de Java)
├── .gitignore
├── README.md
├── CLAUDE.md                                      # Reglas del proyecto para Claude Code
├── data/                                           # Se crea sola al arrancar (contiene demo.db, no se versiona)
└── src/
    ├── main/java/com/ips2026/pl11/
    │   ├── App.java                                # Punto de entrada (main): tema del club, arranca la BD y abre el menú
    │   ├── model/                                  # MODELO: clases de datos, una subcarpeta por funcionalidad
    │   │   ├── example/Persona.java                # Ejemplo de referencia (id, nombre, email)
    │   │   └── store/                              # Venta de merchandising
    │   │       ├── Merchandise.java                # Producto (tabla MERCHANDISING)
    │   │       ├── Cart.java, CartLine.java        # Carrito y sus líneas
    │   │       └── SaleReceipt.java                # Resumen de una compra realizada
    │   ├── view/                                   # VISTA: ventanas Swing (estilo lazy de WindowBuilder)
    │   │   ├── common/                             # Compartido por TODAS las ventanas
    │   │   │   ├── Branding.java                   # Tema Nimbus con colores del club, logo, applyTo(ventana)
    │   │   │   ├── HeaderPanel.java                # Cabecera del club (logo + título)
    │   │   │   └── MoneyFormat.java                # Formato de precios en euros
    │   │   ├── menu/                               # Menú principal
    │   │   │   ├── VentanaPrincipal.java           # Cabecera + 4 botones + pie
    │   │   │   └── MenuCardButton.java             # Botones tipo tarjeta del menú
    │   │   └── store/                              # Venta de merchandising
    │   │       ├── StoreSalesWindow.java           # Catálogo + carrito
    │   │       └── CatalogTableModel.java, CartTableModel.java
    │   ├── controller/                             # CONTROLADOR: une vista y datos
    │   │   ├── example/PersonaControlador.java
    │   │   └── store/StoreSalesController.java
    │   └── data/                                   # DATOS: acceso a la base de datos
    │       ├── ConexionBD.java                     # Común: conexión SQLite y scripts de db/
    │       ├── example/PersonaDAO.java             # Consultas a la tabla "personas"
    │       └── store/
    │           ├── MerchandiseDAO.java             # Consultas a MERCHANDISING
    │           └── MerchandiseSaleDAO.java         # Registro de ventas en MERCHANDISING_SALE
    ├── main/resources/images/
    │   └── logo.png                                # Logo del club (icono de las ventanas y cabecera)
    ├── main/resources/db/
    │   ├── schema.sql                              # CREATE TABLE de todas las tablas
    │   ├── data.sql                                # Datos de ejemplo (dummy data)
    │   └── config.properties                       # Mantener la BD o borrarla al cerrar
    └── test/java/com/ips2026/pl11/
        ├── AppTest.java                             # Test de arranque (JUnit 5)
        ├── model/example/PersonaTest.java           # Test del modelo Persona (mismo subpaquete que la clase)
        ├── model/store/CartTest.java                # Carrito: unidades, límites de stock, total
        ├── controller/store/StoreSalesControllerTest.java  # Búsqueda, filtro por tipo y compra (con DAOs falsos)
        ├── data/TestDatabase.java                   # Ayuda para tests de DAO: BD temporal (nunca toca data/demo.db)
        └── data/store/MerchandiseDAOTest.java, MerchandiseSaleDAOTest.java  # Consultas y registro de ventas en SQLite
```

Cómo fluyen los datos: una vista nunca habla con la base de datos directamente. Le pregunta a su controlador (por ejemplo `PersonaControlador`), que a su vez usa el DAO (`PersonaDAO`) para leer la tabla y devolver objetos del modelo (`Persona`). Las clases `Persona*` se mantienen como ejemplo de referencia, aunque ya no se usan desde el menú principal. Este orden — vista → controlador → datos → modelo — es el que hay que seguir para añadir pantallas nuevas.

### Dónde poner los archivos nuevos

Cada funcionalidad (user story) tiene **su propia subcarpeta con el mismo nombre en cada capa**. Por ejemplo, la venta de merchandising es `store` en las cuatro: `model/store`, `view/store`, `controller/store` y `data/store`. Para una funcionalidad nueva (por ejemplo, gestión de equipos), cread `team/` en las capas que necesite:

```
model/team/Team.java
view/team/TeamWindow.java
controller/team/TeamController.java
data/team/TeamDAO.java
```

- Nombre de la subcarpeta: una palabra en inglés, en minúsculas y en singular (`store`, `team`, `player`, `ticket`...).
- Lo que usan **varias** funcionalidades va en `view/common` (o `model/common`, etc.), no se copia en cada carpeta.
- `data/ConexionBD.java` es común a todos los DAO y se queda en la raíz de `data/`.
- Las ventanas nuevas se abren desde un botón del menú (`view/menu/VentanaPrincipal.java`).
- Los tests van en `src/test/java` con el mismo subpaquete que la clase que prueban (por ejemplo `model/store/CartTest.java`).

## Notas para el equipo

- El paquete base de todo el código Java es `com.ips2026.pl11`. Las clases nuevas van en la capa MVC que les corresponda (`model`, `view`, `controller`, `data`) y dentro, en la subcarpeta de su funcionalidad (ver "Dónde poner los archivos nuevos"), siguiendo la convención estándar de nombres de paquete en minúsculas.
- Las ventanas Swing se escriben con la generación de código **lazy** de Eclipse WindowBuilder (un atributo privado por componente y un getter `getXxx()` que lo crea la primera vez), para poder seguir editándolas desde la pestaña *Design*.
- Los tests van en `src/test/java`, en el mismo subpaquete que la clase que prueban.
- La base de datos es SQLite: un único archivo (`data/demo.db`) que se crea y se rellena solo la primera vez que se ejecuta la app. No hace falta instalar MySQL, PostgreSQL ni ningún gestor de base de datos aparte.
- Si algo no compila o no importa bien en tu IDE, lo normal es que sea un problema de configuración local (versión de JDK distinta, caché de Maven, etc.) — comentadlo en el grupo antes de tocar el `pom.xml`.
