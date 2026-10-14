
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
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Clase principal que implementa la lógica de negocio del sistema LeisureOffice
 * y los servicios de consulta (LookupService).
 * 
 * @author Aimar Ijurko
 */
public class BusinessSystem implements LeisureOffice, LookupService {

    private List<Usuario> usuarios = new ArrayList<>();
    private List<Local> locales = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private Map<Review, Contestacion> contestaciones = new HashMap<>();
    private Map<Local, Set<Dueño>> propietariosDeLocal = new HashMap<>();
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor por defecto de BusinessSystem.
     */
    public BusinessSystem() {
    }

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
        if (u == null || !usuarios.contains(u)) {
            return false;
        }

        if (u instanceof Cliente) {
            Cliente cliente = (Cliente) u;
            // 1. Eliminar reservas hechas por este cliente
            reservas.removeIf(reserva -> reserva.getCliente().equals(cliente));
            
            // 2. Eliminar reviews escritas por este cliente (y sus contestaciones asociadas)
            reviews.removeIf(review -> {
                if (review.getCliente().equals(cliente)) {
                    contestaciones.remove(review);
                    return true;
                }
                return false;
            });
            
        } else if (u instanceof Dueño) {
            Dueño dueño = (Dueño) u;
            // 1. Eliminar contestaciones hechas por este dueño
            contestaciones.values().removeIf(contestacion -> contestacion.getDueño().equals(dueño));
            
            // 2. Desasociar al dueño de los locales que posea
            for (Map.Entry<Local, Set<Dueño>> entry : propietariosDeLocal.entrySet()) {
                entry.getValue().remove(dueño);
            }
        }

        // 3. Finalmente, eliminar al usuario de la lista principal
        return usuarios.remove(u);
    }

    @Override
    public boolean modificaUsuario(Usuario u, Usuario nuevoU) {
        if (u == null || nuevoU == null) {
            return false;
        }
        
        int index = usuarios.indexOf(u);
        if (index == -1) return false;
        
        // Punto 6: Comprobar que el nuevo nick no pertenezca ya a otro usuario
        if (!u.getNick().equals(nuevoU.getNick()) && existeNick(nuevoU.getNick())) {
            return false;
        }
        
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
        // Punto 4: Comprobar que el cliente y el local estén dados de alta
        if (!usuarios.contains(r.getCliente()) || !locales.contains(r.getLocal())) {
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
    public boolean eliminarLocal(Local local) {
        if (local == null || !locales.contains(local)) {
            return false;
        }
        
        // Punto 9: Borrado en cascada para mantener la coherencia de datos
        reservas.removeIf(reserva -> reserva.getLocal().equals(local));

        reviews.removeIf(review -> {
            if (review.getLocal().equals(local)) {
                contestaciones.remove(review);
                return true;
            }
            return false;
        });

        propietariosDeLocal.remove(local);

        return locales.remove(local);
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
            return true;
        }
        Set<Dueño> dueñosActuales = propietariosDeLocal.get(l);
        if (dueñosActuales.contains(d)) {
            return true;
        }
        if (dueñosActuales.size() < 3) {
            dueñosActuales.add(d);
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

        // Punto 8: Evitar guardar un valor null en el mapa si el local no tenía dueños
        Set<Dueño> dueños = propietariosDeLocal.remove(viejoL);
        if (dueños != null) {
            propietariosDeLocal.put(nuevoL, dueños);
        }
        return true;
    }

    @Override
    public Review[] verReviews(Local l) {
        // Puntos 1 y 2: Solucionar el NullPointerException y devolver array vacío si no hay reviews
        if (l == null || !locales.contains(l)) {
            return null;
        }
        List<Review> revsList = new ArrayList<>();
        for (Review review : reviews) {
            if (review.getLocal().equals(l)) {
                revsList.add(review);
            }
        }
        return revsList.toArray(new Review[revsList.size()]);
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
        if (localesEnZona == null) {
            return new Bar[0];
        }

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
        if (localesEnZona == null) {
            return new Restaurante[0];
        }

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
        if (localesEnZona == null) {
            return new Pub[0];
        }

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
        if (localesEnZona == null || localesEnZona.length == 0) {
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
        if (baresEnZona == null || baresEnZona.length == 0) {
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
        if (restauranteEnZona == null || restauranteEnZona.length == 0) {
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
        if (pubsEnZona == null || pubsEnZona.length == 0) {
            return pubsEnZona;
        }

        java.util.Arrays.sort(pubsEnZona, (pub1, pub2) -> {
            float nota1 = obtenerValoracionMedia(pub1);
            float nota2 = obtenerValoracionMedia(pub2);
            return Float.compare(nota2, nota1);
        });
        return pubsEnZona;
    }
    
    /**
     * Carga un listado de bares desde un fichero ODS y los incorpora al sistema de negocio.
     * @param f el archivo ODS
     * @return el número de bares incorporados
     * @throws IOException si ocurre algún error de E/S
     */
    public int importaBares(File f) throws IOException{
        int numeroBares = 0;

        if (f == null || !f.exists()) {
            return 0;
        }

        // Abrir el fichero ODS
        SpreadSheet hoja = SpreadSheet.createFromFile(f);

        // Obtener la primera hoja
        Sheet tabla = hoja.getSheet(0);

        for (int fila = 0; fila < tabla.getRowCount(); fila++) {

            Object nombreBar = tabla.getCellAt(0,fila).getValue();

            if (nombreBar == null || nombreBar.toString().trim().isEmpty()) {
                break;
            }

            String nombre = nombreBar.toString().trim();

            String direccionCompleta =
                    tabla.getCellAt(1, fila).getValue().toString().trim();

            String localidad =
                    tabla.getCellAt(2, fila).getValue().toString().trim();

            String provincia =
                    tabla.getCellAt(3, fila).getValue().toString().trim();

            int posicion = direccionCompleta.lastIndexOf(" ");

            String calle = direccionCompleta.substring(0, posicion);
            String numeroString = direccionCompleta.substring(posicion + 1);
            int numero = Integer.parseInt(numeroString);

            String descripcion = "";

            // Construcción del bar
            Bar bar = new Bar(nombre, new Direccion(localidad, provincia, calle, numero), descripcion);

            // Intentar incorporar el bar al sistema
            if (this.nuevoLocal(bar)) {
                numeroBares++;
            }
        }

        return numeroBares;
    }

    /**
     * Obtiene un array con todos los locales registrados.
     * @return un array de locales
     */
    public Local[] obtenerTodosLosLocales() {
        return locales.toArray(new Local[locales.size()]); // Devolvemos una copia para que no se modifique la original 
    }

    /**
     * Obtiene los dueños asociados a un local.
     * @param l el local
     * @return un array de dueños
     */
    public Dueño[] obtenerDueñosDeLocal(Local l) {
        if (propietariosDeLocal.containsKey(l)) {
            Set<Dueño> dueños = propietariosDeLocal.get(l);
            return dueños.toArray(new Dueño[dueños.size()]);
        }
        return new Dueño[0];
    }
}