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
 * @author alumno
 */
public class SSTest01 {  
    public static void main(String[] args){
        int[][] matriz = {
            {1, 2, 3, 4, 5, 6},
            {7, 8, 9, 10, 11, 12},
            {13, 14, 15, 16, 17, 18},
            {19, 20, 21, 22, 23, 24}
        };
        
        try{
            File archivo = new File("test01.ods");
            SpreadSheet spreadSheet = SpreadSheet.create(1,6,4);
            Sheet sheet = spreadSheet.getSheet(0);

            for (int i=0;i<4;i++){
                for (int j=0;j<6;j++){
                    sheet.setValueAt(matriz[i][j], j, i);
                }
            }

            spreadSheet.saveAs(archivo);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

