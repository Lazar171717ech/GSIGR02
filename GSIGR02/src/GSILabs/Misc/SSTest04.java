package GSILabs.Misc;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Dueño;
import GSILabs.BModel.Local;
import GSILabs.BModel.Pub;
import GSILabs.BModel.Restaurante;
import GSILabs.BSystem.BusinessSystem;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Clase ejecutable para el Ejercicio 5 de la Práctica 02.
 * Genera un sistema de negocio con bares, restaurantes y pubs de ejemplo,
 * y exporta la información completa a un archivo ODS con tres hojas (Bar, Restaurante, Pub),
 * a razón de una fila por cada local, gestionando atributos escalares y de cardinalidad múltiple
 * (dueños, especialidades y capacidades) alineado estrictamente con la estructura de la Figura 3.
 *
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class SSTest04 {

    /**
     * Constructor por defecto de SSTest04.
     */
    public SSTest04() {
    }

    /**
     * Método principal que ejecuta la lógica de creación del sistema,
     * población de locales y dueños, y volcado a un archivo ODS con múltiples hojas.
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        
        // 1. Instanciar el sistema principal
        BusinessSystem bSystem = new BusinessSystem();
        
        // 2. Crear dueños de prueba
        Dueño carlos = new Dueño("Carlos Perez", "pass", LocalDate.of(1980, 5, 20));
        Dueño josefina = new Dueño("Josefina Garcia", "pass", LocalDate.of(1985, 8, 12));
        bSystem.nuevoUsuario(carlos);
        bSystem.nuevoUsuario(josefina);

        // 3. Crear locales de prueba (bares con especialidades, un restaurante y un pub)
        Bar bar1 = new Bar("Rincon las palmeras", new Direccion("Mataro", "Barcelona", "Travesia Cervantes", 174), "Bar de tapas");
        bar1.agregarEspecialidad("Desayuno"); 
        bar1.agregarEspecialidad("Cerveza"); 
        bar1.agregarEspecialidad("Albondigas");
        
        Bar bar2 = new Bar("Tasca andalucia", new Direccion("Tarrasa", "Barcelona", "Calle Cervantes", 161), "Tasca tipica");
        bar2.agregarEspecialidad("Camarones"); 
        bar2.agregarEspecialidad("Embutidos"); 
        bar2.agregarEspecialidad("Catalana");
        
        Restaurante rest1 = new Restaurante("El buen asador", new Direccion("Madrid", "Madrid", "Gran Via", 50), "Asador tradicional", 40.5f, 100, 6);
        Pub pub1 = new Pub("La Noche", new Direccion("Bilbao", "Vizcaya", "Calle Somera", 12), "Copas y ambiente musical", LocalTime.of(20, 0), LocalTime.of(4, 0));

        // 4. Dar de alta los locales en el sistema
        bSystem.nuevoLocal(bar1);
        bSystem.nuevoLocal(bar2);
        bSystem.nuevoLocal(rest1);
        bSystem.nuevoLocal(pub1);

        // 5. Asociar dueños a locales (bar1 tiene 2 dueños para demostrar cardinalidad múltiple)
        bSystem.asociarLocal(bar1, carlos); 
        bSystem.asociarLocal(bar1, josefina);
        bSystem.asociarLocal(bar2, carlos);
        bSystem.asociarLocal(rest1, carlos);
        bSystem.asociarLocal(pub1, josefina);

        // 6. Crear el SpreadSheet con 3 hojas: Bar, Restaurante, Pub
        SpreadSheet doc;
        try {
            doc = SpreadSheet.create(3, 15, 10);
        } catch (Exception e) {
            System.err.println("Error al crear el SpreadSheet: " + e.getMessage());
            return;
        }
        
        org.jopendocument.dom.spreadsheet.Sheet sheetBar = doc.getSheet(0);
        sheetBar.setName("Bar");
        int rowBar = 0;

        org.jopendocument.dom.spreadsheet.Sheet sheetRest = doc.getSheet(1);
        sheetRest.setName("Restaurante");
        int rowRest = 0;

        org.jopendocument.dom.spreadsheet.Sheet sheetPub = doc.getSheet(2);
        sheetPub.setName("Pub");
        int rowPub = 0;

        // 7. Escribir filas de cabecera completas
        Local[] localesGuardados = bSystem.obtenerTodosLosLocales();
        
        sheetBar.setValueAt("NOMBRE",         0, rowBar);
        sheetBar.setValueAt("DIRECCION",      1, rowBar);
        sheetBar.setValueAt("LOCALIDAD",      2, rowBar);
        sheetBar.setValueAt("PROVINCIA",      3, rowBar);
        sheetBar.setValueAt("DUENO",          4, rowBar);
        sheetBar.setValueAt("ESPECIALIDAD 1", 5, rowBar);
        sheetBar.setValueAt("ESPECIALIDAD 2", 6, rowBar);
        sheetBar.setValueAt("ESPECIALIDAD 3", 7, rowBar);
        sheetBar.setValueAt("DESCRIPCION",    8, rowBar);
        rowBar++;    
        
        sheetRest.setValueAt("NOMBRE",           0, rowRest);
        sheetRest.setValueAt("DIRECCION",        1, rowRest);
        sheetRest.setValueAt("LOCALIDAD",        2, rowRest);
        sheetRest.setValueAt("PROVINCIA",        3, rowRest);
        sheetRest.setValueAt("DUENO",            4, rowRest);
        sheetRest.setValueAt("PRECIO ESTIMADO",  5, rowRest);
        sheetRest.setValueAt("CAPACIDAD TOTAL",  6, rowRest);
        sheetRest.setValueAt("CAPACIDAD MESA",   7, rowRest);
        sheetRest.setValueAt("DESCRIPCION",      8, rowRest);
        rowRest++;

        sheetPub.setValueAt("NOMBRE",           0, rowPub);
        sheetPub.setValueAt("DIRECCION",        1, rowPub);
        sheetPub.setValueAt("LOCALIDAD",        2, rowPub);
        sheetPub.setValueAt("PROVINCIA",        3, rowPub);
        sheetPub.setValueAt("DUENO",            4, rowPub);
        sheetPub.setValueAt("HORA APERTURA",    5, rowPub);
        sheetPub.setValueAt("HORA CLAUSURA",    6, rowPub);
        sheetPub.setValueAt("DESCRIPCION",      7, rowPub);
        rowPub++;
        
        // 8. Escribir una fila por cada local, repartiéndolos por tipo de hoja
        for (Local l : localesGuardados) {
            String nombre       = l.getNombre();
            Direccion dir       = l.getDireccion();
            String direccionStr = dir.getCalle() + " " + dir.getNumero();
            String localidadStr = dir.getLocalidad();
            String provinciaStr = dir.getProvincia();
            String descStr      = l.getDescripcion() != null ? l.getDescripcion() : "";
            
            Dueño[] duenos = bSystem.obtenerDueñosDeLocal(l);
            StringBuilder sbDuenos = new StringBuilder();
            for (int dIdx = 0; dIdx < duenos.length; dIdx++) {
                if (dIdx > 0) sbDuenos.append(", ");
                sbDuenos.append(duenos[dIdx].getNick());
            }
            String duenosStr = sbDuenos.length() > 0 ? sbDuenos.toString() : "N/A";

            if (l instanceof Bar) {
                Bar b = (Bar) l;
                List<String> especialidades = b.getEspecialidades();
                
                sheetBar.ensureRowCount(rowBar + 1);
                sheetBar.setValueAt(nombre,       0, rowBar);
                sheetBar.setValueAt(direccionStr, 1, rowBar);
                sheetBar.setValueAt(localidadStr, 2, rowBar);
                sheetBar.setValueAt(provinciaStr, 3, rowBar);
                sheetBar.setValueAt(duenosStr,    4, rowBar);
                for (int col = 0; col < especialidades.size(); col++) {
                    sheetBar.setValueAt(especialidades.get(col), 5 + col, rowBar);
                }
                sheetBar.setValueAt(descStr, 5 + Math.max(3, especialidades.size()), rowBar);
                rowBar++;
            
            } else if (l instanceof Restaurante) {
                Restaurante r = (Restaurante) l;
                sheetRest.ensureRowCount(rowRest + 1);
                sheetRest.setValueAt(nombre,                      0, rowRest);
                sheetRest.setValueAt(direccionStr,                1, rowRest);
                sheetRest.setValueAt(localidadStr,                2, rowRest);
                sheetRest.setValueAt(provinciaStr,                3, rowRest);
                sheetRest.setValueAt(duenosStr,                   4, rowRest);
                sheetRest.setValueAt(r.getPrecioEstimado(),       5, rowRest);
                sheetRest.setValueAt(r.getCapacidadMaximaTotal(), 6, rowRest);
                sheetRest.setValueAt(r.getCapacidadMaximaMesa(),  7, rowRest);
                sheetRest.setValueAt(descStr,                     8, rowRest);
                rowRest++;
            
            } else if (l instanceof Pub) {
                Pub p = (Pub) l;
                sheetPub.ensureRowCount(rowPub + 1);
                sheetPub.setValueAt(nombre,                        0, rowPub);
                sheetPub.setValueAt(direccionStr,                  1, rowPub);
                sheetPub.setValueAt(localidadStr,                  2, rowPub);
                sheetPub.setValueAt(provinciaStr,                  3, rowPub);
                sheetPub.setValueAt(duenosStr,                     4, rowPub);
                sheetPub.setValueAt(p.getHoraApertura().toString(),5, rowPub);
                sheetPub.setValueAt(p.getHoraClausura().toString(),6, rowPub);
                sheetPub.setValueAt(descStr,                       7, rowPub);
                rowPub++;
            }
        }

        // 9. Guardar el fichero ODS resultante
        try {
            File outputFile = new File("test04.ods");
            doc.saveAs(outputFile);
            System.out.println("Archivo test04.ods generado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al generar el archivo ODS: " + e.getMessage());
        }
    }
}
