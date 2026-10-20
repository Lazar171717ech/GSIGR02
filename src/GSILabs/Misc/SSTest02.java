package GSILabs.Misc;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Implementación del test 2 para hojas de cálculo (Práctica 02).
 * Genera una matriz aleatoria de 4x6 números enteros y los escribe en un
 * archivo ODS dejando las primeras 5 filas (filas 1 a 5, índices 0 a 4) y
 * las primeras 3 columnas (columnas A a C, índices 0 a 2) vacías,
 * tal y como se especifica en el enunciado del Ejercicio 3 y se ilustra en la Figura 2.
 * El primer dato queda situado en la fila 6 (índice 5), columna D (índice 3).
 * El fondo de cada celda es azul si el valor es mayor o igual a 10, y rojo
 * en caso contrario.
 *
 * @author Equipo2
 */
public class SSTest02 {

    /**
     * Constructor por defecto de SSTest02.
     */
    public SSTest02() {
    }

    /**
     * Método principal que ejecuta la prueba de creación y formato del archivo ODS.
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        final int FILAS = 4;
        final int COLUMNAS = 6;
        int[][] matriz = new int[FILAS][COLUMNAS];

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                matriz[i][j] = (int) (Math.random() * 24) + 1;
            }
        }

        try {
            // Primeras 5 filas (índices 0-4) y 3 columnas (índices 0-2: A, B, C) vacías,
            // alineado estrictamente con la Figura 2 y el requisito del guión:
            // "dejando que las primeras 5 filas y 3 columnas estén vacías".
            // El primer elemento se sitúa en fila 6 (índice 5), columna D (índice 3).
            int desplazamientoFila    = 5;
            int desplazamientoColumna = 3;
            int totalFilas    = desplazamientoFila    + FILAS;
            int totalColumnas = desplazamientoColumna + COLUMNAS;
            File archivo = new File("test02.ods");

            SpreadSheet spreadSheet = SpreadSheet.create(1, totalColumnas, totalFilas);
            Sheet hoja = spreadSheet.getSheet(0);

            for (int i = 0; i < FILAS; i++) {
                for (int j = 0; j < COLUMNAS; j++) {
                    int valor = matriz[i][j];
                    int filaDestino = i + desplazamientoFila;
                    int colDestino  = j + desplazamientoColumna;

                    hoja.setValueAt(valor, colDestino, filaDestino);

                    MutableCell celda = hoja.getCellAt(colDestino, filaDestino);
                    if (valor >= 10) {
                        celda.setBackgroundColor(Color.BLUE);
                    } else {
                        celda.setBackgroundColor(Color.RED);
                    }
                }
            }
            spreadSheet.saveAs(archivo);
            System.out.println("Archivo test02.ods generado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al generar el archivo .ods: " + e.getMessage());
        }
    }
}
