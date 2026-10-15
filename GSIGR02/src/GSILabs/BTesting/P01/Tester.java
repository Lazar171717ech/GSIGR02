package GSILabs.BTesting.P01;

import GSILabs.BModel.*;
import GSILabs.BSystem.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Clase ejecutable para la batería de pruebas (T1 - T10) y controles positivos de la Práctica.
 * @author Equipo2
 */
public class Tester {

    /**
     * Constructor por defecto de Tester.
     */
    public Tester() {
    }

    /**
     * Método principal que ejecuta la batería de pruebas de integración y validaciones.
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("=== INICIANDO BATERÍA DE PRUEBAS Y CONTROLES POSITIVOS ===\n");

        BusinessSystem bs = new BusinessSystem();

        Direccion dir1 = new Direccion("Pamplona", "Navarra", "Calle Mayor", 1);
        Direccion dir2 = new Direccion("Pamplona", "Navarra", "Calle Estafeta", 2);
        Direccion dir3 = new Direccion("Pamplona", "Navarra", "Paseo Sarasate", 3);

        Bar bar1 = new Bar("El Txoko", dir1, "Bar de pinchos");
        Bar bar2 = new Bar("La Antigua", dir2, "Bar tradicional");
        Bar barLibre = new Bar("Bar Libre", dir3, "Bar en dirección libre");

        Cliente c1 = new Cliente("juan88", "pass123", LocalDate.of(1988, 5, 10));
        Cliente c2 = new Cliente("pablo210", "pass456", LocalDate.of(1995, 8, 20));
        Dueño d1 = new Dueño("boss_pepe", "admin1", LocalDate.of(1970, 1, 1));
        Dueño d2 = new Dueño("boss_maria", "admin2", LocalDate.of(1975, 2, 2));
        Dueño d3 = new Dueño("boss_luis", "admin3", LocalDate.of(1980, 3, 3));
        Dueño d4 = new Dueño("boss_carlos", "admin4", LocalDate.of(1985, 4, 4));

        // --- CONTROLES POSITIVOS (Verificar que lo válido SÍ funciona) ---
        boolean p1 = bs.nuevoUsuario(c1);
        System.out.println("[Control Positivo] Alta de cliente válido (c1): " + (p1 ? "ÉXITO" : "FALLO"));

        boolean p2 = bs.nuevoUsuario(d1);
        System.out.println("[Control Positivo] Alta de dueño válido (d1): " + (p2 ? "ÉXITO" : "FALLO"));

        boolean p3 = bs.nuevoLocal(bar1);
        System.out.println("[Control Positivo] Alta de local válido (bar1): " + (p3 ? "ÉXITO" : "FALLO"));

        boolean p4 = bs.asociarLocal(bar1, d1);
        System.out.println("[Control Positivo] Asociación de primer dueño a bar1: " + (p4 ? "ÉXITO" : "FALLO"));

        // --- BATERÍA DE PRUEBAS T1 - T10 ---
        bs.nuevoUsuario(c2);
        Usuario usuarioEncontrado = bs.obtenerUsuario("pablo210");
        boolean t1 = (usuarioEncontrado != null && usuarioEncontrado.equals(c2));
        System.out.println("Test1 (Localizar usuario por ID): " + (t1 ? "ÉXITO" : "FALLO"));

        Usuario usuarioFantasma = bs.obtenerUsuario("juan99");
        boolean t2 = (usuarioFantasma == null);
        System.out.println("Test2 (Buscar usuario inexistente devuelve null): " + (t2 ? "ÉXITO" : "FALLO"));

        Bar barCopia = new Bar("Copia Txoko", dir1, "Intento de copia");
        boolean añadidoBarCopia = bs.nuevoLocal(barCopia);
        boolean t3 = (añadidoBarCopia == false);
        System.out.println("Test3 (No permitir locales en la misma dirección): " + (t3 ? "ÉXITO" : "FALLO"));

        bs.nuevoLocal(bar2);
        bs.eliminarLocal(bar2);
        Bar barNuevoDir2 = new Bar("Nuevo Bar Estafeta", dir2, "Renovado");
        boolean añadidoNuevoBar = bs.nuevoLocal(barNuevoDir2);
        boolean t4 = (añadidoNuevoBar == true);
        System.out.println("Test4 (Permitir local en dirección liberada tras borrado): " + (t4 ? "ÉXITO" : "FALLO"));

        boolean añadidoMenor = false;
        try {
            Cliente cMenor = new Cliente("menor", "123", LocalDate.now().minusYears(10));
            añadidoMenor = bs.nuevoUsuario(cMenor);
        } catch (IllegalArgumentException e) {
            // Si lanza la excepción al crear el cliente menor, se considera correcto según la validación estricta
            añadidoMenor = false;
        }
        boolean t5 = (añadidoMenor == false);
        System.out.println("Test5 (Rechazar usuario menor de edad): " + (t5 ? "ÉXITO" : "FALLO"));
        // T6) Reserva para local inexistente (usando barLibre que nunca se dio de alta en el sistema)
        boolean reservaLocalInexistente = bs.nuevaReserva(c1, barLibre, LocalDate.now().plusDays(5), LocalTime.of(21, 0));
        boolean t6 = (reservaLocalInexistente == false);
        System.out.println("Test6 (Rechazar reserva para local no registrado): " + (t6 ? "ÉXITO" : "FALLO"));

        // T7) Reserva para local fantasma que comparte dirección con uno real (barCopia no está dado de alta)
        boolean reservaLocalFantasmaDirReal = bs.nuevaReserva(c1, barCopia, LocalDate.now().plusDays(2), LocalTime.of(14, 0));
        boolean t7 = (reservaLocalFantasmaDirReal == false);
        System.out.println("Test7 (Rechazar reserva para local no añadido aunque comparta dirección): " + (t7 ? "ÉXITO" : "FALLO"));

        Review reviewFantasma = new Review(c1, bar1, 5, "Genial", LocalDate.now().minusDays(3), LocalDate.now());
        Contestacion const1 = new Contestacion(d4, reviewFantasma, "Estaba todo bien", LocalDate.MIN);
        boolean comentarioAñadido = bs.nuevaContestacion(const1, reviewFantasma);
        boolean t8 = (comentarioAñadido == false);
        System.out.println("Test8 (Rechazar contestación a review inexistente): " + (t8 ? "ÉXITO" : "FALLO"));

        bs.nuevoUsuario(d2);
        bs.nuevoUsuario(d3);
        bs.nuevoUsuario(d4);
        bs.asociarLocal(bar1, d2);
        bs.asociarLocal(bar1, d3);
        boolean cuartoDueño = bs.asociarLocal(bar1, d4);
        boolean t9 = (cuartoDueño == false);
        System.out.println("Test9 (Respetar límite máximo de 3 dueños por local): " + (t9 ? "ÉXITO" : "FALLO"));

        Review rev1 = new Review(c1, bar1, 4, "Muy bueno", LocalDate.now().minusDays(2), LocalDate.now());
        Review rev2 = new Review(c1, bar1, 3, "No está mal", LocalDate.now().minusDays(2), LocalDate.now());
        bs.nuevaReview(rev1);
        boolean dobleReview = bs.nuevaReview(rev2);
        boolean t10 = (dobleReview == false);
        System.out.println("Test10 (Impedir dos reviews del mismo usuario el mismo día para el mismo local): " + (t10 ? "ÉXITO" : "FALLO"));
        
        System.out.println("\n=== FIN DE LA BATERÍA DE PRUEBAS ===");
    }
}