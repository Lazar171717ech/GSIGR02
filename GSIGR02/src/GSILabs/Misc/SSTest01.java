
package GSILabs.Misc;
import java.io.File;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Implementación del test 1 para el paso a hojas de cálculo.
 * Creamos una matriz de 4 filas y 6 columnas con los números del 1 al 24,
 * creamos un archivo .ods y una hoja de cálculo, escribimos los contenidos
 * de la matriz y guardamos el archivo. En caso de error, se detiene la ejecución
 * y se muestra en pantalla.
 * 
 * @author Pablo Lazaro
 */
public class SSTest01 {  
    
    private static final String NOMBRE_ARCHIVO = "test01.ods";
    
    /**
     * Constructor por defecto de SSTest01.
     */
    public SSTest01() {
    }
    
    /**
     * Método principal que ejecuta la prueba de creación y escritura del archivo ODS.
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args){
        int[][] matriz = {
            {1, 2, 3, 4, 5, 6},
            {7, 8, 9, 10, 11, 12},
            {13, 14, 15, 16, 17, 18},
            {19, 20, 21, 22, 23, 24}
        };
        
        final int FILAS = matriz.length;
        final int COLUMNAS = matriz[0].length;
        
        try{
            File archivo = new File(NOMBRE_ARCHIVO);
            SpreadSheet spreadSheet = SpreadSheet.create(1,COLUMNAS,FILAS);
            Sheet hoja = spreadSheet.getSheet(0);

            for (int i=0;i<FILAS;i++){
                for (int j=0;j<COLUMNAS;j++){
                    hoja.setValueAt(matriz[i][j], j, i);
                }
            }

            spreadSheet.saveAs(archivo);
            System.out.println("El programa se ha ejecutado correctamente");
        } catch(Exception e) {
            System.err.println("ERROR al ejecutar el programa: "+e.getMessage());
            e.printStackTrace();
        }
    }
}