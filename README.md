# Sistema web de inventario y ventas – Ferretería Industrial Los Olivos S.A.C.

Proyecto EFSRT – Grupo 08 – Cibertec (caso de estudio).
Aplicación web de 3 capas: **React** (presentación) → **Spring Boot** (API REST) → **SQL Server Express** (datos).

## Estructura
| Carpeta | Contenido |
|---|---|
| `01_Informe` | Informe del proyecto (.docx) |
| `02_Diseno_Tecnico` | Arquitectura, modelo entidad-relación, historias de usuario, prototipos (draw.io) |
| `03_Codigo_Fuente/backend` | API REST (Java 17, Spring Boot, Maven) |
| `03_Codigo_Fuente/frontend` | Aplicación React (Vite + Bootstrap) |
| `04_Base_de_Datos` | Scripts SQL Server (estructura y datos de prueba) |
| `05_Pruebas` | Plan de pruebas y evidencias |
| `06_Recursos` | Manual de usuario y guía de instalación |

## Flujo de ramas
`main` (estable) ← `develop` ← `feature/<historia>`; cambios por Pull Request con revisión de otro integrante.
Al cierre de cada sprint se crea una etiqueta (`v1.0`, `v2.0`, `v3.0`).

Ver `06_Recursos/Guia_Instalacion.md` para ejecutar el proyecto.
