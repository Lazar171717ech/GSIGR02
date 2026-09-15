/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 *
 * @author alumno
 */
public class Reserva {
    private LocalDate fecha;
    private LocalTime hora;
    private float descuento;
    private Cliente cliente;
    private Reservable local;

    public Reserva(LocalDate fecha, LocalTime hora, float descuento, Cliente cliente, Reservable local) {
        this.fecha = fecha;
        this.hora = hora;
        this.descuento = descuento;
        this.cliente = cliente;
        this.local = local;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public float getDescuento() {
        return descuento;
    }

    public void setDescuento(float descuento) {
        this.descuento = descuento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Reservable getLocal() {
        return local;
    }

    public void setLocal(Reservable local) {
        this.local = local;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fecha, hora, descuento, cliente, local);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Reserva other = (Reserva) obj;
        if (Float.floatToIntBits(this.descuento) != Float.floatToIntBits(other.descuento)) {
            return false;
        }
        if (!Objects.equals(this.fecha, other.fecha)) {
            return false;
        }
        if (!Objects.equals(this.hora, other.hora)) {
            return false;
        }
        if (!Objects.equals(this.cliente, other.cliente)) {
            return false;
        }
        return Objects.equals(this.local, other.local);
    }

    @Override
    public String toString() {
        return "Reserva{"  + "fecha=" + fecha + ", hora=" + hora + ", descuento=" + descuento + ", cliente=" + cliente + ", local=" + local + '}';
    }
    
    
}
