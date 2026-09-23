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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author juang
 */
public class BusinessSystem implements LeisureOffice, LookupService {

    private List<Usuario> usuarios = new ArrayList<>();
    private List<Local> locales = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private Map<Review, Contestacion> contestaciones = new HashMap<>();
    private Map<Local, Set<Dueño>> propietariosDeLocal = new HashMap<>();
    private List<Reserva> reservas = new ArrayList<>();

    @Override
    public boolean nuevoUsuario(Usuario u) {
        if (u == null) return false;
        
        int edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 14) return false;
        
        if (existeNick(u.getNick())) return false;
        usuarios.add(u);
        return true;
    }

    @Override
    public boolean eliminaUsuario(Usuario u) {
        return usuarios.remove(u);
    }

    @Override
    public boolean modificaUsuario(Usuario u, Usuario nuevoU) {
        if (u == null || nuevoU == null) {
            return false;
        }
        
        int index = usuarios.indexOf(u);
        if (index == -1) return false;
        
        int edad = Period.between(nuevoU.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 14) return false;
        
        usuarios.set(index, nuevoU);
        return true;
    }

    @Override
    public boolean existeNick(String nick) {
        if (nick == null) {
            return false;
        }
        for (Usuario user : usuarios) {
            if (user.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Usuario obtenerUsuario(String nick) {
        if (nick == null) {
            return null;
        }
        for (Usuario user : usuarios) {
            if (user.getNick().equals(nick)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean nuevaReview(Review r) {
        if (r == null || r.getCliente() == null || r.getLocal() == null) {
            return false;
        }
        if (existeRewiew(r.getCliente(), r.getLocal(), r.getFechaVisita())) {
            return false;
        }
        reviews.add(r);
        return true;
    }

    @Override
    public boolean eliminaReview(Review r) {
        if (r == null || !reviews.contains(r)) {
            return false;
        }
        if (tieneContestacion(r)) {
            return false;
        }
        reviews.remove(r);
        return true;
    }

    @Override
    public boolean existeRewiew(Usuario u, Local l, LocalDate ld) {
        if (u == null || l == null || ld == null) {
            return false;
        }
        for (Review r : reviews) {
            if (r.getCliente().equals(u)
                    && r.getLocal().equals(l)
                    && r.getFechaVisita().equals(ld)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean nuevaContestacion(Contestacion c, Review r) {
        if (c == null || r == null || !reviews.contains(r)) {
            return false;
        }
        if (tieneContestacion(r)) {
            return false;
        }

        Local localValorado = r.getLocal();
        Dueño dueño = c.getDueño();
        Set<Dueño> dueñosLocal = propietariosDeLocal.get(localValorado);
        if (dueñosLocal == null || !dueñosLocal.contains(dueño)) {
            return false;
        }

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
        if (l == null) {
            return false;
        }
        if (obtenerLocal(l.getDireccion()) != null) {
            return false;
        }
        return locales.add(l);
    }

    @Override
    public boolean eliminarLocal(Local l) {
        return locales.remove(l);
    }

    @Override
    public Local obtenerLocal(Direccion d) {
        if (d == null) {
            return null;
        }
        for (Local local : locales) {
            if (local.getDireccion().equals(d)) {
                return local;
            }
        }
        return null;
    }

    @Override
    public boolean asociarLocal(Local l, Dueño d) {
        if (!usuarios.contains(d) || !locales.contains(l)) {
            return false;
        }
        if (!propietariosDeLocal.containsKey(l)) {
            Set<Dueño> dueños = new HashSet<>();
            dueños.add(d);
            propietariosDeLocal.put(l, dueños);
        }
        if (propietariosDeLocal.get(l).size() < 3) {
            propietariosDeLocal.get(l).add(d);
            return true;
        }
        return false;
    }

    @Override
    public boolean desasociarLocal(Local l, Dueño d) {
        if (!usuarios.contains(d) || !locales.contains(l)) {
            return false;
        }
        if (propietariosDeLocal.containsKey(l)) {
            if (propietariosDeLocal.get(l).contains(d)) {
                propietariosDeLocal.get(l).remove(d);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean actualizarLocal(Local viejoL, Local nuevoL) {
        if (viejoL == null || nuevoL == null) {
            return false;
        }

        int index = locales.indexOf(viejoL);
        if (index == -1) {
            return false;
        }

        if (!viejoL.getDireccion().equals(nuevoL.getDireccion()) && obtenerLocal(nuevoL.getDireccion()) != null) {
            return false;
        }

        locales.set(index, nuevoL);

        Set<Dueño> dueños = propietariosDeLocal.get(viejoL);
        propietariosDeLocal.remove(viejoL);
        propietariosDeLocal.put(nuevoL, dueños);
        return true;
    }

    @Override
    public Review[] verReviews(Local l) {
        Review[] revs = null;
        if (locales.contains(l)) {
            int i = 0;
            for (Review review : reviews) {
                if (review.getLocal().equals(l)) {
                    revs[i] = review;
                    i++;
                }
            }
        }
        return revs;
    }

    @Override
    public boolean nuevaReserva(Cliente c, Reservable r, LocalDate ld, LocalTime lt) {
        if (c == null || r == null || ld == null || lt == null) {
            return false;
        }

        if (!usuarios.contains(c) || !locales.contains(r)) {
            return false;
        }

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (ld.isBefore(hoy)) {
            return false;
        }
        if (ld.isEqual(hoy) && !lt.isAfter(ahora)) {
            return false;
        }

        boolean reservaDuplicada = reservas.stream()
                .anyMatch(reserva -> reserva.getCliente().equals(c)
                && reserva.getLocal().equals(r)
                && reserva.getFecha().equals(ld));

        if (reservaDuplicada) {
            return false;
        }

        return reservas.add(new Reserva(ld, lt, 0, c, r));
    }

    @Override
    public Reserva[] obtenerReservas(Cliente c) {
        if (c == null || !usuarios.contains(c)) {
            return null;
        }

        List<Reserva> reservasDelCliente = new ArrayList<>();

        for (Reserva reserva : reservas) {
            if (reserva.getCliente().equals(c)) {
                reservasDelCliente.add(reserva);
            }
        }
        return reservasDelCliente.toArray(new Reserva[reservasDelCliente.size()]);
    }

    @Override
    public Reserva[] obtenerReservas(Reservable r) {
        if (r == null || !locales.contains(r)) {
            return null;
        }

        List<Reserva> reservasDelLocal = new ArrayList<>();

        for (Reserva reserva : reservas) {
            if (reserva.getLocal().equals(r)) {
                reservasDelLocal.add(reserva);
            }
        }
        return reservasDelLocal.toArray(new Reserva[reservasDelLocal.size()]);
    }

    @Override
    public Reserva[] obtenerReservas(LocalDate ld) {
        if (ld == null) {
            return null;
        }

        List<Reserva> reservasDelDia = new ArrayList<>();

        for (Reserva reserva : reservas) {
            if (reserva.getFecha().equals(ld)) {
                reservasDelDia.add(reserva);
            }
        }
        return reservasDelDia.toArray(new Reserva[reservasDelDia.size()]);
    }

    @Override
    public boolean eliminarReserva(Reserva r) {
        return reservas.remove(r);
    }

    @Override
    public Local[] listarLocales(String ciudad, String provincia) {
        if (ciudad == null || provincia == null) {
            return null;
        }

        List<Local> localesFiltrados = new ArrayList<>();

        for (Local local : locales) {
            Direccion dir = local.getDireccion();
            if (dir != null
                    && ciudad.equalsIgnoreCase(dir.getLocalidad())
                    && provincia.equalsIgnoreCase(dir.getProvincia())) {
                localesFiltrados.add(local);
            }
        }
        return localesFiltrados.toArray(new Local[localesFiltrados.size()]);
    }

    @Override
    public Bar[] listarBares(String ciudad, String provincia) {
        Local[] localesEnZona = listarLocales(ciudad, provincia);

        List<Bar> baresFiltrados = new ArrayList<>();
        for (Local local : localesEnZona) {
            if (local instanceof Bar) {
                baresFiltrados.add((Bar) local);
            }
        }
        return baresFiltrados.toArray(new Bar[baresFiltrados.size()]);
    }

    @Override
    public Restaurante[] listarRestaurantes(String ciudad, String provincia) {
        Local[] localesEnZona = listarLocales(ciudad, provincia);

        List<Restaurante> restauranteFiltrados = new ArrayList<>();
        for (Local local : localesEnZona) {
            if (local instanceof Restaurante) {
                restauranteFiltrados.add((Restaurante) local);
            }
        }
        return restauranteFiltrados.toArray(new Restaurante[restauranteFiltrados.size()]);
    }

    @Override
    public Pub[] listarPubs(String ciudad, String provincia) {
        Local[] localesEnZona = listarLocales(ciudad, provincia);

        List<Pub> pubsFiltrados = new ArrayList<>();
        for (Local local : localesEnZona) {
            if (local instanceof Pub) {
                pubsFiltrados.add((Pub) local);
            }
        }
        return pubsFiltrados.toArray(new Pub[pubsFiltrados.size()]);
    }

    @Override
    public float obtenerValoracionMedia(Local l) {
        Review[] reviewsDelLocal = verReviews(l);

        if (reviewsDelLocal == null) {
            return -1f;
        }
        if (reviewsDelLocal.length == 0) {
            return 0f;
        }
        float suma = 0;
        for (Review r : reviewsDelLocal) {
            suma += r.getValoracion();
        }

        return suma / reviewsDelLocal.length;
    }

    @Override
    public float obtenerValoracionMedia(Dueño d) {
        if (d == null || !usuarios.contains(d)) {
            return -1f;
        }

        float sumaValoraciones = 0;
        int contadorReviews = 0;

        for (Review r : reviews) {
            Local local = r.getLocal();
            Set<Dueño> dueñosDelLocal = propietariosDeLocal.get(local);
            if (dueñosDelLocal != null && dueñosDelLocal.contains(d)) {
                sumaValoraciones += r.getValoracion();
                contadorReviews++;
            }
        }
        if (contadorReviews == 0) {
            return 0f;
        }
        return sumaValoraciones / contadorReviews;
    }

    @Override
    public float obtenerValoracionMedia(Local l, int edadEntre, int edadHasta) {
        Review[] reviewsDelLocal = verReviews(l);
        if (reviewsDelLocal == null) {
            return -1f;
        }

        float sumaValoraciones = 0;
        int contador = 0;

        for (Review r : reviewsDelLocal) {
            Usuario cliente = r.getCliente();
            LocalDate fechaDeLaReview = r.getFechaVisita();
            LocalDate fechaNacimiento = cliente.getFechaNacimiento();
            if (fechaDeLaReview != null && fechaNacimiento != null) {
                int edadEnEseMomento = java.time.Period.between(fechaNacimiento, fechaDeLaReview).getYears();
                if (edadEnEseMomento >= edadEntre && edadEnEseMomento <= edadHasta) {
                    sumaValoraciones += r.getValoracion();
                    contador++;
                }
            }
        }

        if (contador == 0) {
            return 0f;
        }

        return sumaValoraciones / contador;
    }

    @Override
    public Local[] obtenerLocalesOrdenados(String ciudad, String provincia) {
        Local[] localesEnZona = listarLocales(ciudad, provincia);
        if (localesEnZona.length == 0) {
            return localesEnZona;
        }

        java.util.Arrays.sort(localesEnZona, (local1, local2) -> {
            float nota1 = obtenerValoracionMedia(local1);
            float nota2 = obtenerValoracionMedia(local2);
            return Float.compare(nota2, nota1);
        });

        return localesEnZona;
    }

    @Override
    public Local[] obtenerLocalesOrdenados(String provincia) {
        if (provincia == null) {
            return null;
        }

        List<Local> localesEnProvincia = new ArrayList<>();
        for (Local local : locales) {
            Direccion dir = local.getDireccion();
            if (dir != null && provincia.equalsIgnoreCase(dir.getProvincia())) {
                localesEnProvincia.add(local);
            }
        }

        Local[] arrayLocales = localesEnProvincia.toArray(new Local[localesEnProvincia.size()]);
        java.util.Arrays.sort(arrayLocales, (local1, local2) -> {
            float nota1 = obtenerValoracionMedia(local1);
            float nota2 = obtenerValoracionMedia(local2);
            return Float.compare(nota2, nota1);
        });

        return arrayLocales;
    }

    @Override
    public Bar[] obtenerBaresOrdenados(String ciudad, String provincia) {
        Bar[] baresEnZona = listarBares(ciudad, provincia);
        if (baresEnZona.length == 0) {
            return baresEnZona;
        }

        java.util.Arrays.sort(baresEnZona, (bar1, bar2) -> {
            float nota1 = obtenerValoracionMedia(bar1);
            float nota2 = obtenerValoracionMedia(bar2);
            return Float.compare(nota2, nota1);
        });
        return baresEnZona;
    }

    @Override
    public Restaurante[] obtenerRestaurantesOrdenados(String ciudad, String provincia) {
        Restaurante[] restauranteEnZona = listarRestaurantes(ciudad, provincia);
        if (restauranteEnZona.length == 0) {
            return restauranteEnZona;
        }

        java.util.Arrays.sort(restauranteEnZona, (bar1, bar2) -> {
            float nota1 = obtenerValoracionMedia(bar1);
            float nota2 = obtenerValoracionMedia(bar2);
            return Float.compare(nota2, nota1);
        });
        return restauranteEnZona;
    }

    @Override
    public Pub[] obtenerPubOrdenados(String ciudad, String provincia) {
        Pub[] pubsEnZona = listarPubs(ciudad, provincia);
        if (pubsEnZona.length == 0) {
            return pubsEnZona;
        }

        java.util.Arrays.sort(pubsEnZona, (bar1, bar2) -> {
            float nota1 = obtenerValoracionMedia(bar1);
            float nota2 = obtenerValoracionMedia(bar2);
            return Float.compare(nota2, nota1);
        });
        return pubsEnZona;
    }

}
