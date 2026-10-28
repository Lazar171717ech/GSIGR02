package Pruebas;

import java.io.File;
import GSILabs.BSystem.BusinessSystem;
import GSILabs.persistence.XMLParsingException;

/**
 * Clase de pruebas para verificar la funcionalidad de lectura y carga de ficheros XML
 * implementada en la Práctica 03 (Ejercicio 5).
 * 
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class P03Tester {

    public static void main(String[] args) {
        System.out.println("=== INICIO DE PRUEBAS DE LA PRÁCTICA 03 (XML) ===");

        // Rutas a tus ficheros XML de prueba (asegúrate de tenerlos creados en el proyecto)
        File ficheroValido = new File("resources/test_valido.xml");
        File ficheroInvalido = new File("resources/test_invalido.xml");

        // --- PRUEBA 1: parseXMLFile (Control Positivo) ---
        try {
            BusinessSystem sistema = BusinessSystem.parseXMLFile(ficheroValido);
            if (sistema != null) {
                System.out.println("[TEST 1 - ÉXITO] parseXMLFile ha parseado correctamente el fichero válido.");
            } else {
                System.out.println("[TEST 1 - FALLO] parseXMLFile devolvió null con un fichero válido.");
            }
        } catch (XMLParsingException e) {
            System.err.println("[TEST 1 - ERROR] Excepción inesperada: " + e.getMessage());
        }

        // --- PRUEBA 2: parseXMLFile (Control Negativo / Excepción) ---
        try {
            BusinessSystem.parseXMLFile(ficheroInvalido);
            System.out.println("[TEST 2 - FALLO] Se esperaba XMLParsingException y no fue lanzada.");
        } catch (XMLParsingException e) {
            System.out.println("[TEST 2 - ÉXITO] parseXMLFile ha lanzado correctamente la excepción esperada: " + e.getMessage());
        }

        // --- PRUEBA 3: loadXMLFile (Control Positivo) ---
        BusinessSystem sistemaActual = new BusinessSystem();
        boolean resultadoCarga = sistemaActual.loadXMLFile(ficheroValido);
        if (resultadoCarga) {
            System.out.println("[TEST 3 - ÉXITO] loadXMLFile ha cargado los datos en el sistema actual (true).");
        } else {
            System.out.println("[TEST 3 - FALLO] loadXMLFile devolvió false con un fichero válido.");
        }

        // --- PRUEBA 4: loadXMLFile (Control Negativo) ---
        boolean resultadoFallo = sistemaActual.loadXMLFile(ficheroInvalido);
        if (!resultadoFallo) {
            System.out.println("[TEST 4 - ÉXITO] loadXMLFile ha gestionado el error devolviendo false de forma robusta.");
        } else {
            System.out.println("[TEST 4 - FALLO] loadXMLFile aceptó un fichero inválido devolviendo true.");
        }

        System.out.println("=== FIN DE LAS PRUEBAS ===");
    }
}