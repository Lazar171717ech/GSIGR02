# GSIGR02 — Gestión de Sistemas de Información (UPNA)

Proyecto de prácticas de la asignatura **Gestión de Sistemas de Información** (Curso 2026/2027) desarrollado por el **Grupo 02**:
* Joel Vílchez
* Urko Inés
* Aimar Ijurko
* Iker Fernández
* Pablo Lázaro
* Pablo Mosquera
* Juan Garbayo

## Estructura del Proyecto

* `src/GSILabs/BModel`: Modelo de dominio, implementación de `XMLRepresentable` (Práctica 01 y Práctica 03 - Ejercicio 1) y de `Serializable` para su envío por RMI (Práctica 04 - Ejercicio 2).
* `src/GSILabs/BSystem`: Lógica de negocio (`BusinessSystem`), importación ODS y persistencia/carga XML (Prácticas 01, 02 y 03 - Ejercicios 2 y 5) y fachada remota `PublicBusinessSystem` (Práctica 04 - Ejercicio 1).
* `src/GSILabs/persistence`: `XMLParser` y `XMLParsingException` para el análisis y validación de representaciones XML (Práctica 03 - Ejercicios 3 y 4).
* `src/GSILabs/serializable`: Interfaz `XMLRepresentable`.
* `src/GSILabs/connect`: Interfaces remotas (`LocalFinder`, `ClientGateway`, `AdminGateway`), servidor `BusinessServer` y clientes `ClientHub` y `AdminHub` (Práctica 04).
* `test/Pruebas`: Clases de prueba (`P03Tester`, `P03Ej01Tester`, `P03HardcoreTester`, `P04Tester`).
* `P03Ej01Gr02.md`: Informe justificativo del diseño de relaciones en XML (Práctica 03 - Ejercicio 1, < 400 palabras).
* `Gr02P04Ej01.pdf`: Justificación de la clase `PublicBusinessSystem` (Práctica 04 - Ejercicio 1).
* `Gr02P04Ej02.pdf`: Cambios realizados en `GSILabs.BModel` para RMI (Práctica 04 - Ejercicio 2).

## Práctica 04: cómo ejecutarlo

1. Ejecutar `GSILabs.connect.BusinessServer` (puerto 1099 por defecto).
2. Ejecutar `GSILabs.connect.ClientHub` (tag `ClientGateway`) y `GSILabs.connect.AdminHub` (tag `AdminGateway`). Piden dirección, puerto y tag por consola.
3. Para probar entre máquinas, lanzar el servidor en una y los clientes en otra, indicando la IP del servidor.