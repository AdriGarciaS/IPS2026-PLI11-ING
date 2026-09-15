# IPS2026-PL11-ING

Proyecto base del equipo PL11 para la asignatura IPS (curso 2026), desarrollado en tres sprints según las user stories.

Sigue el patrón **MVC** (Modelo-Vista-Controlador) y usa una base de datos **SQLite** (un único archivo, sin servidor que instalar) como ejemplo de acceso a datos. Al arrancar, la app abre una ventana con un botón "Mostrar datos base de datos" que consulta la base de datos y muestra el resultado en una tabla. A partir de aquí se irán añadiendo las funcionalidades de cada sprint.

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

Al ejecutarlo debería abrirse una ventana con el texto "La aplicación funciona correctamente" y un botón "Mostrar datos base de datos". La primera vez que arranca, se crea automáticamente el archivo `data/demo.db` con una tabla `personas` y unas filas de ejemplo — no hace falta instalar ni configurar nada de base de datos a mano.

### Opción B: desde IntelliJ IDEA

1. `File` → `Open...` y seleccionar la carpeta del proyecto (la que contiene el `pom.xml`).
2. IntelliJ detecta automáticamente que es un proyecto Maven y descarga las dependencias.
3. Abrir `src/main/java/com/ips2026/pl11/App.java` y pulsar el botón de ejecutar (▶) junto al método `main`.

### Opción C: desde Eclipse

1. `File` → `Import...` → `Maven` → `Existing Maven Projects`.
2. Seleccionar la carpeta del proyecto (la que contiene el `pom.xml`) y finalizar.
3. Eclipse descarga las dependencias automáticamente.
4. Clic derecho sobre `App.java` → `Run As` → `Java Application`.

## Estructura del proyecto (MVC)

```
IPS2026-PL11-ING/
├── pom.xml                                        # Configuración de Maven (dependencias, plugins, versión de Java)
├── .gitignore
├── README.md
├── data/                                           # Se crea sola al arrancar (contiene demo.db, no se versiona)
└── src/
    ├── main/java/com/ips2026/pl11/
    │   ├── App.java                                # Punto de entrada (main): arranca la BD y monta las capas
    │   ├── modelo/
    │   │   └── Persona.java                        # Modelo: solo datos (id, nombre, email)
    │   ├── vista/
    │   │   └── VentanaPrincipal.java                # Vista: la ventana Swing, botón y tabla de resultados
    │   ├── controlador/
    │   │   └── PersonaControlador.java              # Controlador: intermediario entre vista y datos
    │   └── datos/
    │       ├── ConexionBD.java                      # Abre la conexión SQLite y crea la BD/tabla de ejemplo
    │       └── PersonaDAO.java                      # Las consultas SQL a la tabla "personas"
    └── test/java/com/ips2026/pl11/
        ├── AppTest.java                             # Test de arranque (JUnit 5)
        └── modelo/PersonaTest.java                  # Test del modelo Persona
```

Cómo fluyen los datos: `VentanaPrincipal` (vista) nunca habla con la base de datos directamente. Cuando se pulsa el botón, le pregunta a `PersonaControlador` (controlador), que a su vez usa `PersonaDAO` (datos) para leer la tabla y devolver una lista de `Persona` (modelo). Este orden — vista → controlador → datos → modelo — es el que hay que seguir para añadir pantallas nuevas: una carpeta `modelo/` con las clases de datos, una `vista/` con las ventanas, un `controlador/` que las conecte, y `datos/` si esa pantalla necesita leer o escribir en la base de datos.

## Notas para el equipo

- El paquete base de todo el código Java es `com.ips2026.pl11`. Las clases nuevas van en el paquete que les corresponda según el patrón MVC de arriba (`modelo`, `vista`, `controlador`, `datos`), siguiendo la convención estándar de nombres de paquete en minúsculas.
- Los tests van en `src/test/java`, en el mismo subpaquete que la clase que prueban.
- La base de datos es SQLite: un único archivo (`data/demo.db`) que se crea y se rellena solo la primera vez que se ejecuta la app. No hace falta instalar MySQL, PostgreSQL ni ningún gestor de base de datos aparte.
- Si algo no compila o no importa bien en tu IDE, lo normal es que sea un problema de configuración local (versión de JDK distinta, caché de Maven, etc.) — comentadlo en el grupo antes de tocar el `pom.xml`.
