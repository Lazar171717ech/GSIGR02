
package GSILabs.Misc;

import java.io.File;
import java.io.IOException;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Implementación del test 3 para hojas de cálculo.
 * Lee el archivo test02.ods generado por SSTest02, extrae la matriz de 4x6
 * números a partir de la cuarta fila y sexta columna (desplazamientos hard-coded),
 * y muestra los valores recuperados por consola.
 *
 * @author Equipo2
 */
public class SSTest03 {

    /**
     * Constructor por defecto de SSTest03.
     */
    public SSTest03() {
    }

    /**
     * Método principal que ejecuta la prueba de lectura y volcado de datos desde el archivo ODS.
     * Los desplazamientos están codificados de forma explícita para coincidir con los
     * usados en SSTest02 (fila=3, columna=5).
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        final int FILAS = 4;
        final int COLUMNAS = 6;
        int[][] matriz = new int[FILAS][COLUMNAS];

        try {
            File archivo = new File("test02.ods");
            if (!archivo.exists()) {
                System.err.println("El archivo test02.ods no existe. Ejecute primero SSTest02.");
                return;
            }
            
            // Desplazamientos idénticos a los de SSTest02
            int desplazamientoFila    = 3;
            int desplazamientoColumna = 5;

            SpreadSheet spreadSheet = SpreadSheet.createFromFile(archivo);
            Sheet hoja = spreadSheet.getSheet(0);

            for (int i = 0; i < FILAS; i++) {
                for (int j = 0; j < COLUMNAS; j++) {
                    int filaOrigen = i + desplazamientoFila;
                    int colOrigen  = j + desplazamientoColumna;

                    Object valorCelda = hoja.getValueAt(colOrigen, filaOrigen);

                    // Las celdas vacías se tratan como 0 (valor imposible en SSTest02, rango 1-24)
                    int valorEntero = 0;
                    if (valorCelda != null && !valorCelda.toString().isEmpty()) {
                        valorEntero = (int) Double.parseDouble(valorCelda.toString());
                    }

                    matriz[i][j] = valorEntero;
                }
            }
            
            System.out.println("Matriz leída de test02.ods:");
            for (int i = 0; i < FILAS; i++) {
                System.out.print("| ");
                for (int j = 0; j < COLUMNAS; j++) {
                    System.out.print(matriz[i][j] + " | ");
                }
                System.out.println();
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}