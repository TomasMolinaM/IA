# Inteligencia Artificial - Resolución de Problemas y Juegos

Este repositorio contiene las implementaciones para dos problemas clásicos de Inteligencia Artificial, desarrollados como parte del plan de estudios de Ingeniería de Sistemas.

---

## 📋 Contenido del Proyecto

### 1. Coloreo de Mapas (Búsqueda en Espacio de Estados)
Se resuelve el problema de asignación de colores a un mapa de entre **16 y 30 regiones** adyacentes. El objetivo es asegurar que no existan dos regiones contiguas con el mismo color, utilizando un número limitado de recursos.

* **Algoritmo:** Búsqueda Primero en Profundidad (DFS).
   * Ascensión de Colinas (*Hill Climbing* - Búsqueda Local).
* **Parámetros de Entrada:** * Número de regiones ($N$, entre 16 y 30).
    * Número de colores disponibles ($K$, entre 2 y 10).
* **Restricciones:** Cumplimiento estricto de adyacencia (*Constraint Satisfaction Problem - CSP*).

### 2. Tres en Raya (Búsqueda Competitiva)
Implementación del juego clásico donde un agente inteligente se enfrenta a un oponente humano o a otro agente.

* **Algoritmo:** Minimax.
* **Optimización:** **Poda Alfa-Beta** para reducir el número de estados explorados en el árbol de juego, mejorando la eficiencia computacional sin perder la optimalidad.
* **Referencia:** Basado en los capítulos de búsqueda adversaria de *"Inteligencia Artificial: Un Enfoque Moderno"* de Stuart Russell y Peter Norvig.

---

## 🚀 Tecnologías Utilizadas

* **Lenguaje:** Java
* **Arquitectura:** Programación Orientada a Objetos (POO)
* **Herramientas:** Gestión de dependencias y compilación mediante estándares de la industria.

---

## 🛠️ Instalación y Ejecución

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/tu-usuario/nombre-del-repo.git](https://github.com/tu-usuario/nombre-del-repo.git)
2. **Compilar el proyecto:**
   ```bash
   Asegúrate de tener el JDK instalado.Bashjavac src/*.java -d bin
4. **Ejecutar:**
* Para el Coloreo de Mapas:Bashjava -cp bin MainMapas
* Para el Tres en Raya:Bashjava -cp bin MainTicTacToe

---

## 📖 Detalles de Implementación
### Grupo 1: 
* **Primero en Profundidad (DFS):** Algoritmo de búsqueda sistemática y completa. Explora las ramas de asignación de colores de manera exhaustiva. Integra Poda Temprana (Early Pruning) para descartar ramas matemáticamente imposibles. Si una asignación viola una restricción de adyacencia o no logra usar todos los colores solicitados al finalizar el mapa, el algoritmo realiza Backtracking para probar una combinación distinta, garantizando encontrar una solución óptima si existe.
* **Ascensión de Colinas (Hill Climbing):** Algoritmo heurístico de búsqueda local iterativa (Greedy). Inicia con un tablero coloreado de forma completamente aleatoria y evalúa su "vecindario" (cambiando un color conflictivo a la vez) buscando minimizar una función de costo: el número total de conflictos de adyacencia. Demuestra teórica y visualmente el problema del "Máximo Local", donde el algoritmo puede atascarse al no encontrar un movimiento que mejore inmediatamente el estado actual, especialmente cuando los recursos (colores) son muy limitados.

### Grupo 2: Minimax con Poda Alfa-Beta
El agente evalúa todas las jugadas posibles para maximizar su utilidad mínima.
* Alfa ($\alpha$): El mejor valor (el más alto) encontrado hasta ahora para el jugador MAX.
* Beta ($\beta$): El mejor valor (el más bajo) encontrado hasta ahora para el jugador MIN.
