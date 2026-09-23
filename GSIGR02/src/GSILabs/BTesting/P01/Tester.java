/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BTesting.P01;

import GSILabs.BModel.*;
import GSILabs.BSystem.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Tester {

    public static void main(String[] args) {
        System.out.println("Iniciando batería de pruebas (S1 - S10)...\n");

        BusinessSystem bs = new BusinessSystem();
        
        Direccion dir1 = new Direccion("Pamplona", "Navarra", "Calle Mayor", 1);
        Direccion dir2 = new Direccion("Pamplona", "Navarra", "Calle Estafeta", 2);
        Direccion dir3 = new Direccion("Pamplona", "Navarra", "Pasea Sarasate", 3);

        Bar bar1 = new Bar("El Txoko", dir1, "Bar de pinchos");
        Bar bar2 = new Bar("La Antigua", dir2, "Bar tradicional");

        Cliente c1 = new Cliente("juan88", "pass123", LocalDate.of(1988, 5, 10));
        Cliente c2 = new Cliente("pablo210", "pass456", LocalDate.of(1995, 8, 20));
        Dueño d1 = new Dueño("boss_pepe", "admin1", LocalDate.of(1970, 1, 1));
        Dueño d2 = new Dueño("boss_maria", "admin2", LocalDate.of(1975, 2, 2));
        Dueño d3 = new Dueño("boss_luis", "admin3", LocalDate.of(1980, 3, 3));
        Dueño d4 = new Dueño("boss_carlos", "admin4", LocalDate.of(1985, 4, 4));

        bs.nuevoUsuario(c1);
        bs.nuevoUsuario(d1);
        bs.nuevoLocal(bar1);

        
// T1) Si introduce a un usuario, este puede ser luego localizado a partir de su ID
        bs.nuevoUsuario(c2);
        Usuario usuarioEncontrado = bs.obtenerUsuario("pablo210");
        boolean t1 = (usuarioEncontrado != null && usuarioEncontrado.equals(c2));
        System.out.println("Test1: " + (t1 ? "ÉXITO" : "FALLO"));

// T2) Si busca a un usuario que no existe con obtenerUsuario, el resultado es null
        Usuario usuarioFantasma = bs.obtenerUsuario("juan99");
        boolean t2 = (usuarioFantasma == null);
        System.out.println("Test2: " + (t2 ? "ÉXITO" : "FALLO"));

// T3) No se puede introducir los Locales en la misma dirección
        Bar bar3 = new Bar("Copia Txoko", dir1, "Intento de copia");
        boolean añadidoBar3 = bs.nuevoLocal(bar3);
        boolean t3 = (añadidoBar3 == false);
        System.out.println("Test3: " + (t3 ? "ÉXITO" : "FALLO"));

// T4) Si se añade un local, y se elimina posteriormente, se puede introducir un bar en la misma dirección
        bs.nuevoLocal(bar2);
        bs.eliminarLocal(bar2);
        Bar barNuevoDir2 = new Bar("Nuevo Bar Estafeta", dir2, "Renovado");
        boolean añadidoNuevoBar = bs.nuevoLocal(barNuevoDir2);
        boolean t4 = (añadidoNuevoBar == true);
        System.out.println("Test4: " + (t4 ? "ÉXITO" : "FALLO"));

// T5) No se puede introducir un usuario menor de edad
        Cliente cMenor = new Cliente("menor", "123", LocalDate.now().minusYears(10));
        boolean añadidoMenor = bs.nuevoUsuario(cMenor);
        boolean t5 = (añadidoMenor == false);
        System.out.println("Test5: " + (t5 ? "ÉXITO" : "FALLO"));

// T6) No se pueden hacer reservas para un local inexistente
        Bar bar4 = new Bar("Copia Txoko", dir3, "Intento de copia");
        boolean reservaLocalInexistente = bs.nuevaReserva(c1, bar4, LocalDate.now().plusDays(5), LocalTime.of(21, 0));
        boolean t6 = (reservaLocalInexistente == false);
        System.out.println("Test6: " + (t6 ? "ÉXITO" : "FALLO"));

// T7) No se pueden hacer reservas para un local inexistente, aunque esté en la misma dir que otro existente
        boolean reservaLocalFantasmaDirReal = bs.nuevaReserva(c1, bar3, LocalDate.now().plusDays(2), LocalTime.of(14, 0));
        boolean t7 = (reservaLocalFantasmaDirReal == false);
        System.out.println("Test7: " + (t7 ? "ÉXITO" : "FALLO"));

// T8) No se pueden añadir comentarios para Reviews que no existen
        Review reviewFantasma = new Review(c1, bar1, 5, "Genial", LocalDate.now().minusDays(3), LocalDate.now());
        Contestacion const1 = new Contestacion(d4, reviewFantasma, "Estaba todo bien", LocalDate.MIN);
        boolean comentarioAñadido = bs.nuevaContestacion(const1, reviewFantasma);
        boolean t8 = (comentarioAñadido == false);
        System.out.println("Test8: " + (t8 ? "ÉXITO" : "FALLO"));

// T9) No se pueden añadir cuatro dueños a un bar
        bs.nuevoUsuario(d2);
        bs.nuevoUsuario(d3);
        bs.nuevoUsuario(d4);
        bs.asociarLocal(bar1, d1);
        bs.asociarLocal(bar1, d2);
        bs.asociarLocal(bar1, d3);
        boolean cuartoDueño = bs.asociarLocal(bar1, d4);
        boolean t9 = (cuartoDueño == false);
        System.out.println("Test9: " + (t9 ? "ÉXITO" : "FALLO"));

// T10) No se pueden añadir dos Reviews del mismo usuario, el mismo día para el mismo local
        Review rev1 = new Review(c1, bar1, 4, "Muy bueno", LocalDate.now().minusDays(2), LocalDate.now());
        Review rev2 = new Review(c1, bar1, 3, "No está mal", LocalDate.now().minusDays(2), LocalDate.now());
        bs.nuevaReview(rev1);
        boolean dobleReview = bs.nuevaReview(rev2);
        boolean t10 = (dobleReview == false);
        System.out.println("Test10: " + (t10 ? "ÉXITO" : "FALLO"));
    }
}
