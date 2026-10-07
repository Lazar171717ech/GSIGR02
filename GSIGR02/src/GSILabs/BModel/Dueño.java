/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalDate;

/**
 * Clase que representa a un Dueño.
 */
public class Dueño extends Usuario {

    /**
     * @param nick El nick del usuario (debe tener al menos 3 caracteres y ser úniico).
     * @param password La contraseña del usuario.
     * @param fechaNacimiento La fecha de nacimiento, tiene que ser >=  14 años.
     */
    public Dueño(String nick, String password, LocalDate fechaNacimiento) {
        super(nick, password, fechaNacimiento);
    }

    @Override
    public String toString() {
        return "Dueño{" +
                "nick='" + getNick() + '\'' +
                ", fechaNacimiento=" + getFechaNacimiento() +
                '}';
    }
}