# 🚜 Ipswich Town F.C. — Management System

> *"Official Management & Operations Platform for Ipswich Town F.C."*

<p align="center">
  <img src="src/main/resources/images/ipswich.png" alt="Ipswich Town F.C. Crest" width="160">
</p>

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,sqlite,maven,git,idea,eclipse" alt="My Skills" />
  </a>
</p>

---

## 📖 Índice
1. [Sobre el Proyecto](#-sobre-el-proyecto)
2. [Equipo de Desarrollo](#-equipo-de-desarrollo)
3. [Objetivos del Sistema](#-objetivos-del-sistema)
4. [Módulos y Funcionalidades](#-módulos-y-funcionalidades)
5. [Tecnologías y Arquitectura](#-tecnologías-y-arquitectura)
6. [Guía de Estilo Visual](#-guía-de-estilo-visual)
7. [Instalación y Configuración](#-instalación-y-configuración)

---

## 🌟 Sobre el Proyecto

**IPS2026-PL11-ING** es el sistema integral de gestión de escritorio para el **Ipswich Town F.C.** ("The Tractor Boys"), desarrollado en **Java Swing** y **SQLite** por el equipo **PL11** para la asignatura de *Ingeniería del Proceso Software (IPS)*.

La plataforma centraliza las operaciones del club en un entorno unificado, abarcando desde la administración de la plantilla y cuerpo técnico hasta la planificación de entrenamientos, gestión de ruedas de prensa y entrevistas, reserva de instalaciones y la tienda oficial de merchandising.

---

## 👥 Equipo de Desarrollo

Proyecto desarrollado para la asignatura Ingeniería del Proceso Software del Grado en Ingeniería Informática (Universidad de Oviedo):

## 👥 Equipo de Desarrollo

Proyecto desarrollado para la asignatura Ingeniería del Proceso Software del Grado en Ingeniería Informática (Universidad de Oviedo):

| 👤 Nombre | 📧 Email | 🐱 GitHub |
| :---: | :---: | :---: |
| <img src="https://avatars.githubusercontent.com/patriilaago" width="55" style="border-radius:50%"><br>**Patricia Lago Martín** | `UO294435@uniovi.es` | [![GitHub](https://img.shields.io/badge/GitHub-patriilaago-brightgreen?logo=github)](https://github.com/patriilaago) |
| <img src="https://avatars.githubusercontent.com/reyero-txt" width="55" style="border-radius:50%"><br>**Daniel Reyero Espinedo** | `UO293963@uniovi.es` | [![GitHub](https://img.shields.io/badge/GitHub-reyero--txt-orange?logo=github)](https://github.com/reyero-txt) |
| <img src="https://avatars.githubusercontent.com/AdriGarciaS" width="55" style="border-radius:50%"><br>**Adriana García** | `UO288675@uniovi.es` | [![GitHub](https://img.shields.io/badge/GitHub-AdriGarciaS-blue?logo=github)](https://github.com/AdriGarciaS) |

---

## 🎯 Objetivos del Sistema

- **Centralizar la gestión operativa:** Integrar empleados, prensa, instalaciones y ventas en un único punto de control.
- **Optimizar recursos del club:** Evitar solapamientos de agendas en campos de entrenamiento e itinerarios de ruedas de prensa.
- **Simplificar la interacción corporativa:** Ofrecer una interfaz ágil, moderna e inmersiva adaptada a la identidad visual oficial del Ipswich Town F.C.
- **Cero configuración de base de datos:** Despliegue inmediato mediante base de datos embebida SQLite con regeneración automática de esquemas.

---

## 🚀 Módulos y Funcionalidades

### 1. 👥 Gestión de Personal (`employee`)
- **Alta, modificación y baja de empleados:** Registro completo de jugadores, cuerpo técnico y personal administrativo.
- **Tipificación de roles:** Clasificación por contratos, categorías y estados de actividad dentro del club.

### 2. 📰 Gestión de Entrevistas y Prensa (`interview` / `slots`)
- **Creación de franjas de disponibilidad:** Asignación de *slots* horarios para entrevistas con medios acreditados mediante selecciones interactivas de fecha y hora (`JDateChooser` + tiempo con spinners).
- **Control de conflictos:** Validación automática de solapamientos con sesiones de entrenamiento, partidos o disponibilidad del jugador/entrenador.

### 3. 🏟️ Reserva de Instalaciones (`reservation`)
- **Gestión de campos y espacios:** Asignación de terrenos de juego, gimnasio y salas de prensa del complejo del club.

### 4. 📅 Agendas y Planificación (`schedule`)
- **Control de turnos y eventos:** Cuadrante de horarios de trabajo para técnicos y personal de campo.

### 5. 🛍️ Tienda Oficial de Merchandising (`store`)
- **Catálogo interactivo:** Venta de equipaciones oficiales, productos de merchandising y artículos del club.
- **Caja registradora:** Carrito de compra, desglose de precios y emisión de recibos detallados mediante formateo `MoneyFormat`.

---

## 💻 Tecnologías y Arquitectura

El proyecto está diseñado bajo un estricto patrón **MVC (Modelo-Vista-Controlador)**:

```text
IPS2026-PL11-ING/
├── pom.xml                                        # Configuración y dependencias de Maven
├── README.md                                      # Documentación principal del repositorio
├── CLAUDE.md                                      # Guía de estilo y directrices del proyecto
├── data/                                          # SQLite BD local (demo.db, no versionado)
├── documentation/                                 # Documentación de User Stories y Sprints
└── src/
    ├── main/java/com/ips2026/pl11/
    │   ├── App.java                               # Punto de entrada (Aplica Branding, carga BD y abre menú)
    │   ├── controller/                            # CONTROLADOR: Lógica de negocio
    │   │   ├── employee/                          # Gestión de empleados
    │   │   ├── interview/                         # Control de entrevistas
    │   │   ├── reservation/                       # Gestión de reservas
    │   │   ├── schedule/                          # Planificación de agendas
    │   │   ├── slots/                             # Control de franjas horarias
    │   │   └── store/                             # Lógica de caja y ventas
    │   ├── data/                                  # DATOS: Acceso a SQLite (DAOs)
    │   │   ├── employee/                          # Acceso a 'employees'
    │   │   ├── interview/                         # Acceso a 'interviews'
    │   │   ├── reservation/                       # Acceso a 'facility_reservations'
    │   │   ├── schedule/                          # Acceso a 'work_schedule'
    │   │   ├── slots/                             # Acceso a 'interview_slots'
    │   │   ├── store/                             # Acceso a 'merchandise'
    │   │   └── ConexionBD.java                    # Conexión JDBC común
    │   ├── model/                                 # MODELO: Clases de dominio POJO
    │   │   ├── employee/                          # Employee, EmployeeType
    │   │   ├── interview/                         # Interview
    │   │   ├── reservation/                       # Reservation
    │   │   ├── schedule/                          # WorkSchedule
    │   │   ├── slots/                             # InterviewSlot
    │   │   └── store/                             # Merchandise, Cart, SaleReceipt
    │   └── view/                                  # VISTA: Componentes Swing
    │       ├── common/                            # Branding, HeaderPanel, MoneyFormat
    │       ├── employee/                          # Diálogos de empleados
    │       ├── interview/                         # Diálogos de entrevistas
    │       ├── menu/                              # Menú principal y MenuCardButton
    │       ├── reservation/                       # Diálogos de reservas
    │       ├── schedule/                          # Vistas de agenda
    │       ├── slots/                             # Vistas de creación de slots
    │       └── store/                             # Vistas de tienda y caja
    └── main/resources/
        ├── images/ipswich.png                     # Escudo e icono corporativo
        └── db/                                    # schema.sql, data.sql, config.properties

```

---

## 🎨 Guía de Estilo Visual

Toda la aplicación utiliza la paleta oficial del **Ipswich Town F.C.** integrada mediante el motor Swing Nimbus en `Branding.java`:

* **Royal Blue (`#0044AA`):** Azul base corporativo para títulos, bordes y encabezados.
* **Crest Crimson Red (`#DE2C37`):** Rojo carmesí para detalles de acento, bordes activos y estados destructivos (`dangerMode`).
* **Dark Navy (`#002C6E`):** Azul marino oscuro para contrastes y estados de selección (*hover*).
* **Background (`#F4F6FA`):** Fondo gris claro neutro para máxima legibilidad.

---

## ⚙️ Instalación y Configuración

### Requisitos Previos

* **JDK 21** (Java Development Kit).
* **Maven 3.9+** (integrado automáticamente en IntelliJ IDEA y Eclipse).
* **Git** para clonar el repositorio.

### Pasos para Ejecutar

#### Opción A: Desde Terminal con Maven

```bash
# 1. Compilar el código
mvn compile

# 2. Ejecutar los tests unitarios e integrados
mvn test

# 3. Generar el archivo .jar ejecutable
mvn package

# 4. Ejecutar la aplicación
java -jar target/IPS2026-PL11-ING.jar

```

#### Opción B: Desde IntelliJ IDEA

1. Ve a `File` → `Open...` y selecciona la carpeta raíz del proyecto (donde está el `pom.xml`).
2. IntelliJ detectará automáticamente que es un proyecto Maven y descargará las dependencias necesarias.
3. Abre `src/main/java/com/ips2026/pl11/App.java` y haz clic en el botón de ejecución (**▶**) situado al lado del método `main`.

#### Opción C: Desde Eclipse

1. Ve a `File` → `Import...` → `Maven` → `Existing Maven Projects`.
2. Selecciona la carpeta raíz del proyecto y haz clic en `Finish`.
3. Eclipse descargará las dependencias automáticamente.
4. Haz clic derecho sobre `App.java` → `Run As` → `Java Application`.

```

```
