/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.Misc;
import java.io.File;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
/**
 *
 * @author Pablo Lazaro
 * Implementacion del test 1 para el paso a hojas de calculo:
 * Creamos una matriz de 4 filas y 6 columnas con los numeros de 1 al 24
 * Creamos un archivo .ods y creamos una hoja de calculo
 * Abrimos la hoja 1 y vamos escribiendo los contenidos de la matriz
 * Por ultimo guardamos la hoja de calculo en el archivo creado
 * En caso de error detendra la ejecucion y muestra en pantalla el error
 */
public class SSTest01 {  
    
    private static final String NOMBRE_ARCHIVO = "test01.ods";
    
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

