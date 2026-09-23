/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BSystem;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Contestacion;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Dueño;
import GSILabs.BModel.Local;
import GSILabs.BModel.Pub;
import GSILabs.BModel.Reserva;
import GSILabs.BModel.Reservable;
import GSILabs.BModel.Restaurante;
import GSILabs.BModel.Review;
import GSILabs.BModel.Usuario;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author juang
 */
public class BusinessSystem implements LeisureOffice, LookupService{
    
    private List<Usuario> usuarios = new ArrayList<>();
    private Map<Direccion, Local> locales = new HashMap<>();
    private List<Review> reviews = new ArrayList<>();
    private Map<Review, Contestacion> contestaciones = new HashMap<>();
    private Map<Local, Set<Dueño>> propietariosDeLocal = new HashMap<>();
    private List<Reserva> reservas = new ArrayList<>();

    @Override
    public boolean nuevoUsuario(Usuario u) {
        if (u == null) return false;
        if(existeNick(u.getNick())) return false;
        usuarios.add(u);
        return true;
    }

    @Override
    public boolean eliminaUsuario(Usuario u) {
        return usuarios.remove(u);
    }

    @Override
    public boolean modificaUsuario(Usuario u, Usuario nuevoU) {
        if (u == null || nuevoU == null) return false;
        int index = usuarios.indexOf(u);
        if (index == -1) return false;
        usuarios.set(index, nuevoU);
        return true;
    }

    @Override
    public boolean existeNick(String nick) {
        if (nick == null) return false;
        for (Usuario user : usuarios) {
            if (user.getNick().equals(nick)) return true;
        }
        return false;
    }

    @Override
    public Usuario obtenerUsuario(String nick) {
        if (nick == null) return null;
        for (Usuario user : usuarios) {
            if (user.getNick().equals(nick)) return user;
        }
        return null;
    }

    @Override
    public boolean nuevaReview(Review r) {
        if (r == null || r.getCliente() == null || r.getLocal() == null) return false;
        if (!locales.containsKey(r.getLocal().getDireccion())) return false;
        if (existeRewiew(r.getCliente(), r.getLocal(), r.getFechaVisita())) return false;
            
        reviews.add(r);
        return true;
    }

    @Override
    public boolean eliminaReview(Review r) {
        if (r == null || !reviews.contains(r)) return false;
        if (tieneContestacion(r)) return false; 
        
        reviews.remove(r);
        return true;    }

    @Override
    public boolean existeRewiew(Usuario u, Local l, LocalDate ld) {
        if (u == null || l == null || ld == null) return false;
        for (Review r : reviews) {
            if(r.getCliente().equals(u) && 
            r.getLocal().equals(l) && 
            r.getFechaVisita().equals(ld)) return true;
        }
        return false;
    }

    @Override
    public boolean nuevaContestacion(Contestacion c, Review r) {
        if (c == null || r == null || !reviews.contains(r)) return false;
        if (tieneContestacion(r)) return false;
        
        Local localValorado = r.getLocal();
        Dueño dueño = c.getDueño();
        
        Set<Dueño> dueñosLocal = propietariosDeLocal.get(localValorado);
        if(dueñosLocal == null || !dueñosLocal.contains(dueño)) return false;
        
        contestaciones.put(r, c);
        return true;
    }

    @Override
    public boolean tieneContestacion(Review r) {
        return contestaciones.containsKey(r);
    }
    

    @Override
    public Contestacion obtenerContestacion(Review r) {
        return contestaciones.get(r);
    }

    @Override
    public boolean eliminaContestacion(Contestacion c) {
        return contestaciones.values().remove(c);
    }

    @Override
    public boolean eliminaContestacion(Review r) {
        if (r != null && contestaciones.containsKey(r)) {
            contestaciones.remove(r);
            return true;
        }
        return false;
    }

    @Override
    public boolean nuevoLocal(Local l) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean eliminarLocal(Local l) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Local obtenerLocal(Direccion d) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean asociarLocal(Local l, Dueño d) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean desasociarLocal(Local l, Dueño d) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean actualizarLocal(Local viejoL, Local nuevoL) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Review[] verReviews(Local l) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean nuevaReserva(Cliente c, Reservable r, LocalDate ld, LocalTime lt) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Reserva[] obtenerReservas(Cliente c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Reserva[] obtenerReservas(Reservable r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Reserva[] obtenerReservas(LocalDate ld) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean eliminarReserva(Reserva r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Local[] listarLocales(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Bar[] listarBares(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Restaurante[] listarRestaurantes(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Pub[] listarPubs(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public float obtenerValoracionMedia(Local l) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public float obtenerValoracionMedia(Dueño d) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public float obtenerValoracionMedia(Local l, int edadEntre, int edadHasta) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Local[] obtenerLocalesOrdenados(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Local[] obtenerLocalesOrdenados(String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Bar[] obtenerBaresOrdenados(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Restaurante[] obtenerRestaurantesOrdenados(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Pub[] obtenerPubOrdenados(String ciudad, String provincia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
