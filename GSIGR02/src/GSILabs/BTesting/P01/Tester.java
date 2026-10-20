package GSILabs.BTesting.P01;

import GSILabs.BModel.*;
import GSILabs.BSystem.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Clase ejecutable para la batería de pruebas (S1 - S10) y controles positivos de la Práctica 01.
 * Valida la lógica de negocio del sistema, incluyendo altas, bajas, límites de propietarios,
 * integridad referencial, reservas, reviews y contestaciones sobre bares, restaurantes y pubs.
 * 
 * @author Equipo2
 */
public class Tester {

    /**
     * Constructor por defecto de Tester.
     */
    public Tester() {
    }

    /**
     * Método principal que ejecuta la batería completa de pruebas de integración y validaciones.
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("        INICIANDO BATERÍA DE CONTROLES POSITIVOS Y SUCESOS S1 - S10             ");
        System.out.println("================================================================================\n");

        BusinessSystem bs = new BusinessSystem();

        // 1. Definición de direcciones de prueba
        Direccion dir1 = new Direccion("Pamplona", "Navarra", "Calle Mayor", 1);
        Direccion dir2 = new Direccion("Pamplona", "Navarra", "Calle Estafeta", 2);
        Direccion dir3 = new Direccion("Pamplona", "Navarra", "Paseo Sarasate", 3);
        Direccion dirRest = new Direccion("Tudela", "Navarra", "Plaza de los Fueros", 10);
        Direccion dirPub  = new Direccion("Pamplona", "Navarra", "Calle San Nicolas", 15);

        // 2. Definición de locales de prueba (Bares, Restaurante y Pub para variedad completa de datos)
        Bar bar1 = new Bar("El Txoko", dir1, "Bar de pinchos y raciones");
        bar1.agregarEspecialidad("Tortilla");
        bar1.agregarEspecialidad("Croquetas");

        Bar bar2 = new Bar("La Antigua", dir2, "Bar tradicional");
        Bar barLibre = new Bar("Bar Libre", dir3, "Bar en dirección libre no registrado");
        Bar barCopia = new Bar("Copia Txoko", dir1, "Bar no registrado en dirección ocupada");

        Restaurante rest1 = new Restaurante("Asador Navarra", dirRest, "Carnes a la brasa", 32.5f, 80, 4);
        Pub pub1 = new Pub("Pub La Noche", dirPub, "Copas y música", LocalTime.of(22, 0), LocalTime.of(4, 30));

        // 3. Definición de usuarios de prueba
        Cliente c1 = new Cliente("juan88", "pass123", LocalDate.of(1988, 5, 10));
        Cliente c2 = new Cliente("pablo210", "pass456", LocalDate.of(1995, 8, 20));
        Cliente c3 = new Cliente("laura_g", "pass789", LocalDate.of(1992, 11, 15));

        Dueño d1 = new Dueño("boss_pepe", "admin1", LocalDate.of(1970, 1, 1));
        Dueño d2 = new Dueño("boss_maria", "admin2", LocalDate.of(1975, 2, 2));
        Dueño d3 = new Dueño("boss_luis", "admin3", LocalDate.of(1980, 3, 3));
        Dueño d4 = new Dueño("boss_carlos", "admin4", LocalDate.of(1985, 4, 4));

        // ================================================================================
        // CONTROLES POSITIVOS (Comprobación de que las operaciones válidas SÍ se aceptan)
        // ================================================================================
        System.out.println("--- FASE 1: CONTROLES POSITIVOS ---");

        // CP1: Alta de clientes válidos
        boolean cp1_1 = bs.nuevoUsuario(c1);
        boolean cp1_2 = bs.nuevoUsuario(c2);
        boolean cp1_3 = bs.nuevoUsuario(c3);
        System.out.println("[CP1] Altas de clientes válidos (c1, c2, c3): " + (cp1_1 && cp1_2 && cp1_3 ? "ÉXITO" : "FALLO"));

        // CP2: Alta de dueños válidos
        boolean cp2_1 = bs.nuevoUsuario(d1);
        boolean cp2_2 = bs.nuevoUsuario(d2);
        boolean cp2_3 = bs.nuevoUsuario(d3);
        boolean cp2_4 = bs.nuevoUsuario(d4);
        System.out.println("[CP2] Altas de dueños válidos (d1, d2, d3, d4): " + (cp2_1 && cp2_2 && cp2_3 && cp2_4 ? "ÉXITO" : "FALLO"));

        // CP3: Altas de locales válidos (Bar, Restaurante y Pub)
        boolean cp3_bar  = bs.nuevoLocal(bar1);
        boolean cp3_rest = bs.nuevoLocal(rest1);
        boolean cp3_pub  = bs.nuevoLocal(pub1);
        System.out.println("[CP3] Altas de locales válidos (Bar, Restaurante, Pub): " + (cp3_bar && cp3_rest && cp3_pub ? "ÉXITO" : "FALLO"));

        // CP4: Asociación válida de los tres dueños permitidos a bar1
        boolean cp4_d1 = bs.asociarLocal(bar1, d1);
        boolean cp4_d2 = bs.asociarLocal(bar1, d2);
        boolean cp4_d3 = bs.asociarLocal(bar1, d3);
        System.out.println("[CP4] Asociación progresiva de 3 dueños permitidos a bar1: " + (cp4_d1 && cp4_d2 && cp4_d3 ? "ÉXITO" : "FALLO"));

        // CP5: Asociación válida de dueño a Restaurante
        boolean cp5_rest = bs.asociarLocal(rest1, d1);
        System.out.println("[CP5] Asociación de dueño d1 a restaurante rest1: " + (cp5_rest ? "ÉXITO" : "FALLO"));

        // CP6: Creación y registro de una primera Review válida
        Review revValida = new Review(c1, bar1, 5, "Excelente comida y trato", LocalDate.now().minusDays(5), LocalDate.now().minusDays(4));
        boolean cp6_rev = bs.nuevaReview(revValida);
        System.out.println("[CP6] Alta de primera review válida de c1 sobre bar1: " + (cp6_rev ? "ÉXITO" : "FALLO"));

        // CP7: Creación y registro de una Reserva válida con éxito para Bar
        boolean cp7_resBar = bs.nuevaReserva(c1, bar1, LocalDate.now().plusDays(3), LocalTime.of(21, 30));
        System.out.println("[CP7] Alta de reserva válida con éxito en bar1: " + (cp7_resBar ? "ÉXITO" : "FALLO"));

        // CP8: Creación y registro de una Reserva válida con éxito para Restaurante
        boolean cp8_resRest = bs.nuevaReserva(c2, rest1, LocalDate.now().plusDays(4), LocalTime.of(14, 0));
        System.out.println("[CP8] Alta de reserva válida con éxito en restaurante rest1: " + (cp8_resRest ? "ÉXITO" : "FALLO"));

        // CP9: Creación y registro de una Contestación válida con éxito por el dueño del local
        Contestacion contValida = new Contestacion(d1, revValida, "Muchas gracias por tu visita", LocalDate.now().minusDays(3));
        boolean cp9_cont = bs.nuevaContestacion(contValida, revValida);
        System.out.println("[CP9] Alta de contestación válida de dueño legítimo d1 a revValida: " + (cp9_cont ? "ÉXITO" : "FALLO"));

        // ================================================================================
        // SUCESOS S1 - S10 (Comprobación de restricciones de negocio del guión)
        // ================================================================================
        System.out.println("\n--- FASE 2: COMPROBACIÓN DE SUCESOS S1 - S10 ---");

        // S1) Localizar usuario a partir de su ID (nick)
        // Comprobación: Se busca "pablo210" registrado en CP1 y se verifica que coincida con c2.
        Usuario uEncontrado = bs.obtenerUsuario("pablo210");
        boolean s1 = (uEncontrado != null && uEncontrado.equals(c2));
        System.out.println("S1 (Localizar usuario por nick existente): " + (s1 ? "ÉXITO" : "FALLO")
                + " [Recuperado: " + (uEncontrado != null ? uEncontrado.getNick() : "null") + "]");

        // S2) Buscar un usuario que no existe devuelve null
        // Comprobación: Se consulta un nick no registrado ("usuario_inexistente") y debe retornar null.
        Usuario uFantasma = bs.obtenerUsuario("usuario_inexistente");
        boolean s2 = (uFantasma == null);
        System.out.println("S2 (Buscar usuario inexistente devuelve null): " + (s2 ? "ÉXITO" : "FALLO"));

        // S3) No se pueden introducir dos Locales en la misma dirección
        // Comprobación: barCopia tiene dir1 (misma que bar1 ya registrado); nuevoLocal debe rechazarlo.
        boolean anadidoBarCopia = bs.nuevoLocal(barCopia);
        boolean s3 = (!anadidoBarCopia);
        System.out.println("S3 (Rechazar local en dirección ya ocupada): " + (s3 ? "ÉXITO" : "FALLO"));

        // S4) Si se añade un local y se elimina posteriormente, se puede introducir otro en la misma dirección
        // Comprobación: Se añade bar2 en dir2, se elimina bar2, y luego se añade barNuevoDir2 en dir2. Debe aceptarse.
        bs.nuevoLocal(bar2);
        boolean eliminadoBar2 = bs.eliminarLocal(bar2);
        Bar barNuevoDir2 = new Bar("Nuevo Bar Estafeta", dir2, "Renovado y reabierto");
        boolean anadidoTrasBorrado = bs.nuevoLocal(barNuevoDir2);
        boolean s4 = (eliminadoBar2 && anadidoTrasBorrado);
        System.out.println("S4 (Permitir nuevo local en dirección liberada tras borrado): " + (s4 ? "ÉXITO" : "FALLO"));

        // S5) No se puede introducir un usuario menor de 14 años
        // Comprobación: Se intenta instanciar un cliente de 10 años; debe lanzar IllegalArgumentException
        // o ser rechazado por nuevoUsuario si se burlara el constructor.
        boolean menorRechazado = false;
        try {
            Cliente cMenor = new Cliente("menor_diez", "pass123", LocalDate.now().minusYears(10));
            menorRechazado = !bs.nuevoUsuario(cMenor);
        } catch (IllegalArgumentException e) {
            // La excepción en el modelo valida la regla estrictamente antes de llegar a la capa de negocio
            menorRechazado = true;
        }
        boolean s5 = menorRechazado;
        System.out.println("S5 (Rechazar usuario menor de 14 años): " + (s5 ? "ÉXITO" : "FALLO"));

        // S6) No se pueden hacer reservas para un local inexistente (en dirección libre)
        // Comprobación: barLibre está en dir3 (libre), pero nunca se dio de alta en el sistema; nuevaReserva debe retornar false.
        boolean resLocalInexistente = bs.nuevaReserva(c1, barLibre, LocalDate.now().plusDays(5), LocalTime.of(21, 0));
        boolean s6 = (!resLocalInexistente);
        System.out.println("S6 (Rechazar reserva para local inexistente en dirección libre): " + (s6 ? "ÉXITO" : "FALLO"));

        // S7) No se pueden hacer reservas para un local inexistente, aunque comparta dirección con uno existente
        // Comprobación: barCopia está en dir1 (dirección del bar1 registrado), pero barCopia no está dado de alta. Debe retornar false.
        boolean resLocalFantasmaMismaDir = bs.nuevaReserva(c1, barCopia, LocalDate.now().plusDays(5), LocalTime.of(14, 0));
        boolean s7 = (!resLocalFantasmaMismaDir);
        System.out.println("S7 (Rechazar reserva para local no registrado en dirección ocupada): " + (s7 ? "ÉXITO" : "FALLO"));

        // S8) No se pueden añadir comentarios (contestaciones) para Reviews que no existen
        // Comprobación: Se crea una review fantasma no registrada en bs; nuevaContestacion debe retornar false.
        Review reviewFantasma = new Review(c1, bar1, 4, "Buena tapa", LocalDate.now().minusDays(10), LocalDate.now().minusDays(9));
        Contestacion contFantasma = new Contestacion(d1, reviewFantasma, "Respuesta a review no registrada", LocalDate.now());
        boolean contAceptada = bs.nuevaContestacion(contFantasma, reviewFantasma);
        boolean s8 = (!contAceptada);
        System.out.println("S8 (Rechazar contestación a review inexistente en el sistema): " + (s8 ? "ÉXITO" : "FALLO"));

        // S9) No se pueden añadir cuatro dueños a un bar
        // Comprobación: bar1 ya tiene asociados d1, d2 y d3 (en CP4). Se intenta asociar d4 como cuarto dueño; debe retornar false.
        boolean cuartoDuenoAceptado = bs.asociarLocal(bar1, d4);
        boolean s9 = (!cuartoDuenoAceptado);
        System.out.println("S9 (Impedir superar el límite de 3 dueños por local): " + (s9 ? "ÉXITO" : "FALLO"));

        // S10) No se pueden añadir dos Reviews del mismo usuario, el mismo día para el mismo local
        // Comprobación: c1 ya visitó bar1 hace 2 días (rev1). Se intenta registrar rev2 para la misma visita,
        // incluso usando una nueva instancia de Cliente con el mismo nick para verificar que equals/hashCode lo intercepta.
        LocalDate fechaVisitaComun = LocalDate.now().minusDays(2);
        Review revA = new Review(c1, bar1, 4, "Primera opinion de la visita", fechaVisitaComun, LocalDate.now());
        boolean revA_ok = bs.nuevaReview(revA);
        Cliente c1_clon = new Cliente("juan88", "otra_pass", LocalDate.of(1988, 5, 10));
        Review revB_duplicada = new Review(c1_clon, bar1, 3, "Segunda opinion de la misma visita", fechaVisitaComun, LocalDate.now());
        boolean revB_ok = bs.nuevaReview(revB_duplicada);
        boolean s10 = (revA_ok && !revB_ok);
        System.out.println("S10 (Impedir doble review del mismo cliente y visita para el mismo local): " + (s10 ? "ÉXITO" : "FALLO"));

        System.out.println("\n================================================================================");
        System.out.println("           BATERÍA DE PRUEBAS COMPLETADA SATISFACTORIAMENTE                     ");
        System.out.println("================================================================================");
    }
}
