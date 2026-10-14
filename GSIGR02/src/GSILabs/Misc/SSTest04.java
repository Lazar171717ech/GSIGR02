/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
 * Genera un sistema de negocio, introduce datos y los exporta a un archivo ODS.
 * 
 * @author Urko Ines y Joel Vilchez
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
        
        // 2. Crear objetos de prueba (Dueños y Locales)
        Dueño carlos = new Dueño("Carlos Perez", "pass", LocalDate.of(1980, 5, 20));
        Dueño josefina = new Dueño("Josefina Garcia", "pass", LocalDate.of(1985, 8, 12));
        bSystem.nuevoUsuario(carlos);
        bSystem.nuevoUsuario(josefina);

     
        Bar bar1 = new Bar("Rincon las palmeras", new Direccion("Mataro", "Barcelona", "Travesia Cervantes", 174), "Bar de tapas");
        bar1.agregarEspecialidad("Desayuno"); 
        bar1.agregarEspecialidad("Cerveza"); 
        bar1.agregarEspecialidad("Albondigas");
        
        Bar bar2 = new Bar("Tasca andalucia", new Direccion("Tarrasa", "Barcelona", "Calle Cervantes", 161), "Tasca típica");
        bar2.agregarEspecialidad("Camarones"); 
        bar2.agregarEspecialidad("Embutidos"); 
        bar2.agregarEspecialidad("Catalana");
        
        Restaurante rest1 = new Restaurante("El buen asador", new Direccion("Madrid", "Madrid", "Gran Via", 50), "Asador", 40.5f, 100, 6);
        Pub pub1 = new Pub("La Noche", new Direccion("Bilbao", "Vizcaya", "Calle Somera", 12), "Copas", LocalTime.of(20, 0), LocalTime.of(4, 0));

        // 3. Introducir los datos en BusinessSystem
        bSystem.nuevoLocal(bar1);
        bSystem.nuevoLocal(bar2);
        bSystem.nuevoLocal(rest1);
        bSystem.nuevoLocal(pub1);

        // Asociación de dueños. Asignamos 2 dueños a bar1 para demostrar la cardinalidad múltiple.
        bSystem.asociarLocal(bar1, carlos); 
        bSystem.asociarLocal(bar1, josefina);
        bSystem.asociarLocal(bar2, carlos);
        bSystem.asociarLocal(rest1, carlos);
        bSystem.asociarLocal(pub1, josefina);

        // 4. Modelos de datos para jOpenDocument
        SpreadSheet doc;
        try {
            doc = SpreadSheet.create(3, 10, 10);
        } catch (Exception e) {
            System.err.println("Error al crear el SpreadSheet: " + e.getMessage());
            return;
        }
        
        // Configurar las 3 hojas
        org.jopendocument.dom.spreadsheet.Sheet sheetBar = doc.getSheet(0);
        sheetBar.setName("Bar");
        int rowBar = 0;

        org.jopendocument.dom.spreadsheet.Sheet sheetRest = doc.getSheet(1);
        sheetRest.setName("Restaurante");
        int rowRest = 0;

        org.jopendocument.dom.spreadsheet.Sheet sheetPub = doc.getSheet(2);
        sheetPub.setName("Pub");
        int rowPub = 0;

        // 5. Recuperar la información del sistema
        Local[] localesGuardados = bSystem.obtenerTodosLosLocales();
        sheetBar.setValueAt("NOMBRE DEL BAR", 0, rowBar);
        sheetBar.setValueAt("DIRECCIÓN DEL BAR", 1, rowBar);
        sheetBar.setValueAt("LOCALIDAD DEL BAR", 2, rowBar);
        sheetBar.setValueAt("PROVINCIA DEL BAR", 3, rowBar);
        sheetBar.setValueAt("DUEÑO 1 DEL BAR", 4, rowBar);
        sheetBar.setValueAt("ESPECIALIDAD 1 DEL BAR", 5, rowBar);
        sheetBar.setValueAt("ESPECIALIDAD 2 DEL BAR", 6, rowBar);
        sheetBar.setValueAt("ESPECIALIDAD 3 DEL BAR", 7, rowBar);
        sheetBar.setValueAt("DUEÑO 2 DEL BAR", 8, rowBar);
        sheetBar.setValueAt("DUEÑO 3 DEL BAR", 9, rowBar);
        rowBar++;    
        
        sheetRest.setValueAt("NOMBRE DEL RESTAURANTE", 0, rowRest);
        sheetRest.setValueAt("DIRECCIÓN DEL RESTAURANTE", 1, rowRest);
        sheetRest.setValueAt("LOCALIDAD DEL RESTAURANTE", 2, rowRest);
        sheetRest.setValueAt("PROVINCIA DEL RESTAURANTE", 3, rowRest);
        sheetRest.setValueAt("DUEÑO 1 DEL RESTAURANTE", 4, rowRest);
        sheetRest.setValueAt("PRECIO ESTIMADO DEL RESTAURANTE", 5, rowRest);
        sheetRest.setValueAt("DUEÑO 2 DEL RESTAURANTE", 6, rowRest);
        sheetRest.setValueAt("DUEÑO 3 DEL RESTAURANTE", 7, rowRest);
        rowRest++;

        sheetPub.setValueAt("NOMBRE DEL PUB", 0, rowPub);
        sheetPub.setValueAt("DIRECCIÓN DEL PUB", 1, rowPub);
        sheetPub.setValueAt("LOCALIDAD DEL PUB", 2, rowPub);
        sheetPub.setValueAt("PROVINCIA DEL PUB", 3, rowPub);
        sheetPub.setValueAt("DUEÑO 1 DEL PUB", 4, rowPub);
        sheetPub.setValueAt("HORA DE APERTURA DEL PUB", 5, rowPub);
        sheetPub.setValueAt("HORA DE CLAUSURA DEL PUB", 6, rowPub);
        sheetPub.setValueAt("DUEÑO 2 DEL PUB", 7, rowPub);
        sheetPub.setValueAt("DUEÑO 3 DEL PUB", 8, rowPub);        
        rowPub++;
        
        for (Local l : localesGuardados) {
            String nombre = l.getNombre();
            Direccion dir = l.getDireccion();
            String direccionStr = dir.getCalle() + " " + dir.getNumero();
            String localidadStr = dir.getLocalidad();
            String provinciaStr = dir.getProvincia();
            
            Dueño[] dueños = bSystem.obtenerDueñosDeLocal(l);
            String dueño1 = dueños.length > 0 ? dueños[0].getNick() : "N/A";
            String dueño2 = dueños.length > 1 ? dueños[1].getNick() : "N/A";
            String dueño3 = dueños.length > 2 ? dueños[2].getNick() : "N/A";

            if (l instanceof Bar) {
                Bar b = (Bar) l;
                List<String> especialidades = b.getEspecialidades();
                
                String e1 = especialidades.size() > 0 ? especialidades.get(0) : "N/A";
                String e2 = especialidades.size() > 1 ? especialidades.get(1) : "N/A";
                String e3 = especialidades.size() > 2 ? especialidades.get(2) : "N/A";
                
                sheetBar.ensureRowCount(rowBar + 1);
                // setValueAt(Dato, Columna, Fila)
                sheetBar.setValueAt(nombre, 0, rowBar);
                sheetBar.setValueAt(direccionStr, 1, rowBar);
                sheetBar.setValueAt(localidadStr, 2, rowBar);
                sheetBar.setValueAt(provinciaStr, 3, rowBar);
                sheetBar.setValueAt(dueño1, 4, rowBar);
                sheetBar.setValueAt(e1, 5, rowBar);
                sheetBar.setValueAt(e2, 6, rowBar);
                sheetBar.setValueAt(e3, 7, rowBar);
                sheetBar.setValueAt(dueño2, 8, rowBar);
                sheetBar.setValueAt(dueño3, 9, rowBar);
                rowBar++;
            
            } else if (l instanceof Restaurante) {
                Restaurante r = (Restaurante) l;
                sheetRest.ensureRowCount(rowRest + 1);
                sheetRest.setValueAt(nombre, 0, rowRest);
                sheetRest.setValueAt(direccionStr, 1, rowRest);
                sheetRest.setValueAt(localidadStr, 2, rowRest);
                sheetRest.setValueAt(provinciaStr, 3, rowRest);
                sheetRest.setValueAt(dueño1, 4, rowRest);
                sheetRest.setValueAt(r.getPrecioEstimado(), 5, rowRest);
                sheetRest.setValueAt(dueño2, 6, rowRest);
                sheetRest.setValueAt(dueño3, 7, rowRest);
                rowRest++;
            
            } else if (l instanceof Pub) {
                Pub p = (Pub) l;
                sheetPub.ensureRowCount(rowPub + 1);
                sheetPub.setValueAt(nombre, 0, rowPub);
                sheetPub.setValueAt(direccionStr, 1, rowPub);
                sheetPub.setValueAt(localidadStr, 2, rowPub);
                sheetPub.setValueAt(provinciaStr, 3, rowPub);
                sheetPub.setValueAt(dueño1, 4, rowPub);
                sheetPub.setValueAt(p.getHoraApertura().toString(), 5, rowPub);
                sheetPub.setValueAt(p.getHoraClausura().toString(), 6, rowPub);
                sheetPub.setValueAt(dueño2, 7, rowPub);
                sheetPub.setValueAt(dueño3, 8, rowPub);                
                rowPub++;
            }
        }

        // 6. Volcado al fichero ODS
        try {
            File outputFile = new File("test04.ods");
            doc.saveAs(outputFile);
            System.out.println("Archivo test04.ods generado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al generar el archivo ODS: " + e.getMessage());
        }
    }
}