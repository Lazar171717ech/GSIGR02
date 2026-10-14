
package GSILabs.Misc;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Implementación del test 2 para hojas de cálculo.
 * Genera una matriz aleatoria de números y los escribe en una hoja ODS
 * aplicando desplazamientos y colores de fondo según el valor de cada celda.
 * 
 * @author juang
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
            int desplazamientoFila = 5;
            int desplazamientoColumna = 3;
            int totalFilas = desplazamientoFila + FILAS;
            int totalColumnas = desplazamientoColumna + COLUMNAS;
            File archivo = new File("test02.ods");

            SpreadSheet spreadSheet = SpreadSheet.create(1, totalColumnas, totalFilas);
            Sheet hoja = spreadSheet.getSheet(0);

            for (int i = 0; i < FILAS; i++) {
                for (int j = 0; j < COLUMNAS; j++) {
                    int valor = matriz[i][j];
                    int filaDestino = i + desplazamientoFila;
                    int colDestino = j + desplazamientoColumna;

                    hoja.setValueAt(valor, colDestino, filaDestino);

                    MutableCell celda = hoja.getCellAt(colDestino, filaDestino);
                    if (valor >= 10) {
                        celda.setBackgroundColor(Color.BLUE);
                    } else {
                        celda.setBackgroundColor(Color.RED);
                    }
                }
            }
            // El archivo se guarda en el directorio principal del proyecto
            spreadSheet.saveAs(archivo);
        } catch (IOException e) {
            System.err.println("Error al generar el archivo .ods:");
        }
    }
}