package GSILabs.Misc;

import GSILabs.BSystem.BusinessSystem;
import java.io.File;

/**
 * Clase de prueba para el Ejercicio 6 de la Práctica 02.
 * Comprueba el funcionamiento de la importación masiva de bares desde un
 * archivo ODS hacia el sistema de negocio mediante el método importaBares().
 * El archivo de entrada esperado es P02Ej05.ods, ubicado en el directorio
 * raíz del proyecto.
 *
 * @author Equipo2
 */
public class SSTest05 {
    
    /**
     * Constructor por defecto de SSTest05.
     */
    public SSTest05() {
    }

    /**
     * Método principal que ejecuta la prueba de importación de bares.
     * Instancia el sistema de negocio, carga el archivo ODS indicado
     * e imprime por consola el número de bares incorporados con éxito.
     *
     * @param args los argumentos de la línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        BusinessSystem sistema = new BusinessSystem();

        File f = new File("P02Ej05.ods");

        int numeroBares = sistema.importaBares(f);

        System.out.println("Bares incorporados: " + numeroBares);
    }
}