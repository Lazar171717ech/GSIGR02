/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalDate;
import java.time.Period;

/**
 * Clase abstracta Usuario, esto se debe a que no puede existir un Usuario, sino que un Cliente o un Dueño.
 */
public abstract class Usuario {
    
    private String nick;
    private String password;
    private LocalDate fechaNacimiento;

    /**
     * @param nick El nick del usuario (debe tener al menos 3 caracteres y ser úniico).
     * @param password La contraseña del usuario.
     * @param fechaNacimiento La fecha de nacimiento, tiene que ser >=  14 años.
     */
    public Usuario(String nick, String password, LocalDate fechaNacimiento) {
        setNick(nick);
        this.password = password;
        setFechaNacimiento(fechaNacimiento);
    }

    public String getNick() {
        return nick;
    }

    /**
     * @param nick Nombre de usuario.
     */
    public void setNick(String nick) {
        if (nick == null || nick.trim().length() < 3) {
            throw new IllegalArgumentException("El nick debe tener al menos tres caracteres.");
        }
        this.nick = nick.trim(); // Limpiamos espacios del principio y final.
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento, validando la edad mínima.
     * @param fechaNacimiento Fecha de nacimiento del usuario.
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < 14) {
            throw new IllegalArgumentException("No se permiten usuarios menores de 14 años.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nick='" + nick + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                '}';
    }
}