
package GSILabs.Misc;

import java.io.File;
import java.io.IOException;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Implementación del test 3 para hojas de cálculo.
 * Lee un archivo ODS existente (test02.ods), extrae los valores numéricos
 * de una celda con desplazamiento y los carga en una matriz para mostrarlos
 * por pantalla.
 * 
 * @author juang
 */
public class SSTest03 {

    /**
     * Constructor por defecto de SSTest03.
     */
    public SSTest03() {
    }

    /**
     * Método principal que ejecuta la prueba de lectura y volcado de datos desde el archivo ODS.
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        final int FILAS = 4;
        final int COLUMNAS = 6;
        int[][] matriz = new int[FILAS][COLUMNAS];

        try {
            File archivo = new File("test02.ods");
            if (!archivo.exists()) {
                System.err.println("El archivo test02.ods no existe.");
                return;
            }
            
            int desplazamientoFila = 5;
            int desplazamientoColumna = 3;

            SpreadSheet spreadSheet = SpreadSheet.createFromFile(archivo);
            Sheet hoja = spreadSheet.getSheet(0);

            for (int i = 0; i < FILAS; i++) {
                for (int j = 0; j < COLUMNAS; j++) {
                    int filaOrigen = i + desplazamientoFila;
                    int colOrigen = j + desplazamientoColumna;

                    Object valorCelda = hoja.getValueAt(colOrigen, filaOrigen);

                    /* Si el valor de la celda es vacio pone un 0, que de otra manera no se
                     podría generar por que la matriz se rellena con valores del 1 al 24 */
                    int valorEntero = 0;
                    if (valorCelda != null && !valorCelda.toString().isEmpty()) {
                        valorEntero = (int) Double.parseDouble(valorCelda.toString());
                    }

                    matriz[i][j] = valorEntero;
                }
            }
            
            for (int i = 0; i < FILAS; i++) {
                System.out.print("| ");
                for (int j = 0; j < COLUMNAS; j++) {
                    System.out.print(matriz[i][j] + " | ");
                }
                System.out.println("");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al leer el archivo");
        }
    }
}