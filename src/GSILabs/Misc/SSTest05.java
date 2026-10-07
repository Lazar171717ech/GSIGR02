/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.Misc;

import GSILabs.BSystem.BusinessSystem;
import java.io.File;
import java.io.IOException;

/**
 * Clase de prueba para el Ejercicio 6 de la Práctica 02.
 * Su propósito es comprobar el funcionamiento de la importación masiva 
 * de bares desde un archivo en formato ODS hacia el sistema de negocio
 * utilizando la funcion importaBares().
 * @author alumno
 */
public class SSTest05 {
    /**
     * Método principal que ejecuta la prueba de importación de bares.
     * Instancia el sistema de negocio, carga el archivo ODS correspondiente
     * e imprime por consola el número de bares incorporados con éxito.
     * 
     * @param args los argumentos de la línea de comandos (no utilizados)
     * @throws IOException si ocurre algún error de lectura o entrada/salida con el fichero
     */
    public static void main(String[] args) throws IOException {

        BusinessSystem sistema = new BusinessSystem();

        File f = new File("P05Ej02.ods");

        int numeroBares = sistema.importaBares(f);

        System.out.println("Bares incorporados: " + numeroBares);
    }
}
