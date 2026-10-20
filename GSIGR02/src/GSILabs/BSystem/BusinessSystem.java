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
 * y los servicios de consulta avanzada (LookupService).
 * 
 * Gestiona el almacenamiento de usuarios, locales, reservas, reviews y contestaciones,
 * garantizando la coherencia de los datos mediante políticas de validación y borrado en cascada.
 * 
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Cazalla, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class BusinessSystem implements LeisureOffice, LookupService {

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Local> locales = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();
    private final Map<Review, Contestacion> contestaciones = new HashMap<>();
    private final Map<Local, Set<Dueño>> propietariosDeLocal = new HashMap<>();
    private final List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor por defecto de BusinessSystem.
     * Inicializa las colecciones en memoria del sistema de información.
     */
    public BusinessSystem() {
    }

    /**
     * Da de alta un nuevo usuario (Cliente o Dueño) en el sistema.
     * Comprueba que el usuario no sea nulo, que tenga al menos 14 años de edad
     * y que su nick no esté registrado previamente.
     * @param u El nuevo usuario a registrar.
     * @return true si el usuario se registró con éxito; false en caso contrario.
     */
    @Override
    public boolean nuevoUsuario(Usuario u) {
        if (u == null) return false;
        
        int edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 14) return false;
        
        if (existeNick(u.getNick())) return false;
        usuarios.add(u);
        return true;
    }

    /**
     * Elimina a un usuario del sistema aplicando una política de borrado en cascada
     * para asegurar la integridad referencial y no dejar datos huérfanos:
     * Si es un Cliente, se eliminan sus reservas y sus reviews (junto con las contestaciones asociadas).
     * Si es un Dueño, se eliminan sus contestaciones y se le desasocia de todos sus locales.
     * @param u El usuario a eliminar.
     * @return true si el usuario existía y fue eliminado; false en caso contrario.
     */
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

    /**
     * Reemplaza en el sistema al usuario viejo por el nuevo.
     * Comprueba que el usuario original exista, que el nuevo cumpla con la edad mínima (>=14)
     * y que el nuevo nick no pertenezca a otro usuario registrado.
     * Propaga la actualización a las reservas, reviews, titularidad de locales y contestaciones.
     * @param u El usuario existente a modificar.
     * @param nuevoU El nuevo usuario con la información actualizada.
     * @return true si el usuario pudo ser modificado; false en caso contrario.
     */
    @Override
    public boolean modificaUsuario(Usuario u, Usuario nuevoU) {
        if (u == null || nuevoU == null) {
            return false;
        }
        
        // No se permite cambiar el tipo de usuario (Cliente a Dueño o viceversa)
        if (!u.getClass().equals(nuevoU.getClass())) {
            return false;
        }
        
        int index = usuarios.indexOf(u);
        if (index == -1) return false;
        
        // Comprobar que el nuevo nick no pertenezca ya a otro usuario diferente
        if (!u.getNick().equals(nuevoU.getNick()) && existeNick(nuevoU.getNick())) {
            return false;
        }
        
        int edad = Period.between(nuevoU.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 14) return false;
        
        // Mantener coherencia en las relaciones existentes
        if (u instanceof Cliente && nuevoU instanceof Cliente) {
            Cliente cViejo = (Cliente) u;
            Cliente cNuevo = (Cliente) nuevoU;
            for (Reserva res : reservas) {
                if (res.getCliente().equals(cViejo)) {
                    res.setCliente(cNuevo);
                }
            }
            // Reubicar contestaciones para no corromper el HashMap al cambiar el nick del cliente
            Map<Review, Contestacion> contestacionesActualizadas = new HashMap<>();
            for (Review rev : reviews) {
                if (rev.getCliente().equals(cViejo)) {
                    Contestacion cont = contestaciones.remove(rev);
                    rev.setCliente(cNuevo);
                    if (cont != null) {
                        contestacionesActualizadas.put(rev, cont);
                    }
                }
            }
            contestaciones.putAll(contestacionesActualizadas);
        } else if (u instanceof Dueño && nuevoU instanceof Dueño) {
            Dueño dViejo = (Dueño) u;
            Dueño dNuevo = (Dueño) nuevoU;
            for (Set<Dueño> dueños : propietariosDeLocal.values()) {
                if (dueños != null && dueños.remove(dViejo)) {
                    dueños.add(dNuevo);
                }
            }
            for (Contestacion c : contestaciones.values()) {
                if (c.getDueño().equals(dViejo)) {
                    c.setDueño(dNuevo);
                }
            }
        }
        
        usuarios.set(index, nuevoU);
        return true;
    }

    /**
     * Comprueba si existe algún usuario registrado con el nick especificado.
     * @param nick Nick del usuario a comprobar.
     * @return true si existe un usuario con ese nick; false en caso contrario o si nick es null.
     */
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

    /**
     * Recupera el usuario registrado con un determinado nick.
     * @param nick Nick del usuario a recuperar.
     * @return El usuario correspondiente, o null si no existe ningún usuario con dicho nick.
     */
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

    /**
     * Incorpora una nueva review al sistema.
     * Valida que la review no sea nula, que el cliente y el local estén registrados en el sistema
     * y que el cliente no haya valorado previamente la misma visita (mismo local y misma fecha de visita).
     * @param r La review a incorporar.
     * @return true si la review fue incorporada con éxito; false si los datos son inválidos o duplicados.
     */
    @Override
    public boolean nuevaReview(Review r) {
        if (r == null || r.getCliente() == null || r.getLocal() == null) {
            return false;
        }
        // Comprobar que el cliente y el local estén dados de alta
        if (!usuarios.contains(r.getCliente()) || !locales.contains(r.getLocal())) {
            return false;
        }
        if (existeRewiew(r.getCliente(), r.getLocal(), r.getFechaVisita())) {
            return false;
        }
        reviews.add(r);
        return true;
    }

    /**
     * Elimina una review del sistema, siempre y cuando exista y no posea una contestación asociada.
     * @param r La review a eliminar.
     * @return true si la review existía y pudo ser eliminada; false en caso contrario.
     */
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

    /**
     * Comprueba si la visita de un usuario a un local en una fecha dada ha sido comentada en el sistema.
     * @param u El usuario cliente.
     * @param l El local visitado.
     * @param ld La fecha de visita.
     * @return true si existe una review registrada para esa combinación; false en caso contrario.
     */
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

    /**
     * Añade una contestación a una review existente.
     * Valida que la review exista, que no tenga ya otra contestación y que el dueño
     * que contesta sea uno de los propietarios legítimos del local valorado.
     * @param c La contestación a añadir.
     * @param r La review a la que se responde.
     * @return true si la contestación fue aceptada; false en caso contrario.
     */
    @Override
    public boolean nuevaContestacion(Contestacion c, Review r) {
        if (c == null || r == null || !reviews.contains(r)) {
            return false;
        }
        if (c.getReview() != null && !c.getReview().equals(r)) {
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

    /**
     * Consulta si una review tiene una contestación asociada.
     * @param r La review a consultar.
     * @return true si la review está registrada y tiene contestación; false en caso contrario.
     */
    @Override
    public boolean tieneContestacion(Review r) {
        return r != null && contestaciones.containsKey(r);
    }

    /**
     * Recupera la contestación asociada a una review dada.
     * @param r La review a consultar.
     * @return La contestación asociada, o null si no existe o la review no está registrada.
     */
    @Override
    public Contestacion obtenerContestacion(Review r) {
        return r != null ? contestaciones.get(r) : null;
    }

    /**
     * Elimina la contestación pasada como argumento.
     * @param c La contestación a eliminar.
     * @return true si la contestación fue eliminada; false en caso contrario.
     */
    @Override
    public boolean eliminaContestacion(Contestacion c) {
        return contestaciones.values().remove(c);
    }

    /**
     * Elimina la contestación asociada a una review dada.
     * @param r La review cuya contestación se desea eliminar.
     * @return true si existía contestación y fue eliminada; false en caso contrario.
     */
    @Override
    public boolean eliminaContestacion(Review r) {
        if (r != null && contestaciones.containsKey(r)) {
            contestaciones.remove(r);
            return true;
        }
        return false;
    }

    /**
     * Añade un nuevo local al sistema, garantizando que no exista otro local en la misma dirección física.
     * @param l El local a añadir.
     * @return true si el local se añadió correctamente; false si es nulo o su dirección ya está ocupada.
     */
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

    /**
     * Elimina un local del sistema aplicando una política de borrado en cascada para evitar datos huérfanos:
     * elimina todas sus reservas, todas sus reviews (y las contestaciones asociadas) y su registro de propietarios.
     * @param local El local a eliminar.
     * @return true si el local existía y fue eliminado; false en caso contrario.
     */
    @Override
    public boolean eliminarLocal(Local local) {
        if (local == null || !locales.contains(local)) {
            return false;
        }
        
        // Borrado en cascada para mantener la coherencia de datos
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

    /**
     * Obtiene el local instalado en una determinada dirección física.
     * @param d La dirección del local a buscar.
     * @return El local correspondiente, o null si no existe ningún local en dicha dirección.
     */
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

    /**
     * Asocia un local a un dueño, siempre que ambos existan y no se supere el límite de 3 dueños por local.
     * La asociación es idempotente si el dueño ya estaba vinculado.
     * @param l Local existente en el sistema.
     * @param d Dueño existente en el sistema.
     * @return true si el dueño quedó asociado con éxito; false si alguno no existe o se supera el límite de 3.
     */
    @Override
    public boolean asociarLocal(Local l, Dueño d) {
        if (l == null || d == null || !usuarios.contains(d) || !locales.contains(l)) {
            return false;
        }
        if (!propietariosDeLocal.containsKey(l) || propietariosDeLocal.get(l) == null) {
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

    /**
     * Desliga un local de un propietario, siempre que ambos existan y estén previamente relacionados.
     * @param l Local existente en el sistema.
     * @param d Dueño existente en el sistema.
     * @return true si la desasociación se completó con éxito; false en caso contrario.
     */
    @Override
    public boolean desasociarLocal(Local l, Dueño d) {
        if (l == null || d == null || !usuarios.contains(d) || !locales.contains(l)) {
            return false;
        }
        if (propietariosDeLocal.containsKey(l)) {
            Set<Dueño> set = propietariosDeLocal.get(l);
            if (set != null && set.contains(d)) {
                return set.remove(d);
            }
        }
        return false;
    }

    /**
     * Reemplaza en el sistema un local viejo por uno nuevo con datos actualizados.
     * Comprueba que el local original exista y que la nueva dirección no esté ocupada por otro local.
     * Mantiene la asociación de propietarios, reviews y reservas asociadas para evitar inconsistencias.
     * @param viejoL Local existente que se desea actualizar.
     * @param nuevoL Nuevo local con los datos actualizados.
     * @return true si el local se actualizó correctamente; false en caso contrario.
     */
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

        // Actualizar titularidad en el mapa de propietarios
        Set<Dueño> dueños = propietariosDeLocal.remove(viejoL);
        if (dueños != null) {
            propietariosDeLocal.put(nuevoL, dueños);
        }

        // Reubicar contestaciones para no corromper el HashMap al mutar la review
        Map<Review, Contestacion> contestacionesActualizadas = new HashMap<>();
        for (Review rev : reviews) {
            if (rev.getLocal().equals(viejoL)) {
                Contestacion c = contestaciones.remove(rev);
                rev.setLocal(nuevoL);
                if (c != null) {
                    contestacionesActualizadas.put(rev, c);
                }
            }
        }
        contestaciones.putAll(contestacionesActualizadas);

        // Propagar a reservas; si el nuevo local no es reservable, eliminar reservas asociadas
        if (nuevoL instanceof Reservable) {
            for (Reserva res : reservas) {
                if (res.getLocal().equals(viejoL)) {
                    res.setLocal((Reservable) nuevoL);
                }
            }
        } else {
            reservas.removeIf(res -> res.getLocal().equals(viejoL));
        }

        return true;
    }

    /**
     * Devuelve las reviews asociadas a un local determinado.
     * @param l Local existente en el sistema.
     * @return Array de reviews del local; array vacío de longitud 0 si no tiene ninguna; o null si el local no existe.
     */
    @Override
    public Review[] verReviews(Local l) {
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

    /**
     * Registra una nueva reserva para un cliente en un local reservable para una fecha y hora futura.
     * Comprueba que cliente y local existan, que la fecha y hora sean futuras y que no exista
     * una reserva duplicada para el mismo cliente, local y fecha.
     * @param c Cliente que realiza la reserva.
     * @param r Local reservable (Bar o Restaurante).
     * @param ld Fecha de la reserva (debe ser futura o de hoy con hora posterior a la actual).
     * @param lt Hora de la reserva.
     * @return true si la reserva fue completada; false en caso contrario.
     */
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

    /**
     * Obtiene todas las reservas asociadas a un cliente específico.
     * @param c El cliente a consultar.
     * @return Array con las reservas del cliente, o null si el cliente no está registrado.
     */
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

    /**
     * Obtiene todas las reservas asociadas a un local reservable.
     * @param r El local reservable a consultar.
     * @return Array con las reservas del local, o null si el local no está registrado.
     */
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

    /**
     * Obtiene todas las reservas del sistema programadas para una fecha dada.
     * @param ld La fecha a consultar.
     * @return Array con las reservas correspondientes a ese día, o null si ld es nulo.
     */
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

    /**
     * Elimina una reserva del sistema si ésta existe.
     * @param r La reserva a eliminar.
     * @return true si la reserva existía y fue eliminada; false en caso contrario.
     */
    @Override
    public boolean eliminarReserva(Reserva r) {
        return r != null && reservas.remove(r);
    }

    /**
     * Lista todos los locales situados en una ciudad y provincia concretas.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array con los locales encontrados; potencialmente de longitud 0, o null si algún parámetro es nulo.
     */
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

    /**
     * Lista los bares situados en una ciudad y provincia concretas.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array con los bares encontrados; array vacío de longitud 0 si no se encuentra ninguno o parámetros nulos.
     */
    @Override
    public Bar[] listarBares(String ciudad, String provincia) {
        Local[] localesEnZona = listarLocales(ciudad, provincia);
        if (localesEnZona == null) {
            return null;
        }

        List<Bar> baresFiltrados = new ArrayList<>();
        for (Local local : localesEnZona) {
            if (local instanceof Bar) {
                baresFiltrados.add((Bar) local);
            }
        }
        return baresFiltrados.toArray(new Bar[baresFiltrados.size()]);
    }

    /**
     * Lista los restaurantes situados en una ciudad y provincia concretas.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array con los restaurantes encontrados; potencialmente de longitud 0, o null si algún parámetro es nulo.
     */
    @Override
    public Restaurante[] listarRestaurantes(String ciudad, String provincia) {
        Local[] localesEnZona = listarLocales(ciudad, provincia);
        if (localesEnZona == null) {
            return null;
        }

        List<Restaurante> restauranteFiltrados = new ArrayList<>();
        for (Local local : localesEnZona) {
            if (local instanceof Restaurante) {
                restauranteFiltrados.add((Restaurante) local);
            }
        }
        return restauranteFiltrados.toArray(new Restaurante[restauranteFiltrados.size()]);
    }

    /**
     * Lista los pubs situados en una ciudad y provincia concretas.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array con los pubs encontrados; potencialmente de longitud 0, o null si algún parámetro es nulo.
     */
    @Override
    public Pub[] listarPubs(String ciudad, String provincia) {
        Local[] localesEnZona = listarLocales(ciudad, provincia);
        if (localesEnZona == null) {
            return null;
        }

        List<Pub> pubsFiltrados = new ArrayList<>();
        for (Local local : localesEnZona) {
            if (local instanceof Pub) {
                pubsFiltrados.add((Pub) local);
            }
        }
        return pubsFiltrados.toArray(new Pub[pubsFiltrados.size()]);
    }

    /**
     * Obtiene la valoración media de las reviews asociadas a un local.
     * @param l Local de interés.
     * @return La valoración media calculada; 0f si no tiene reviews; -1f si el local no existe o es nulo.
     */
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

    /**
     * Obtiene la valoración media de todas las reviews asociadas a los locales pertenecientes a un dueño.
     * @param d El dueño a investigar.
     * @return La valoración media global; 0f si sus locales no tienen reviews; -1f si el dueño no existe o es nulo.
     */
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

    /**
     * Obtiene la valoración media de un local realizada por usuarios que, en el momento de la valoración,
     * se encontraban dentro de un rango de edad determinado (ambos extremos incluidos).
     * @param l Local de interés.
     * @param edadEntre Edad mínima del rango (incluida).
     * @param edadHasta Edad máxima del rango (incluida).
     * @return La valoración media en ese rango de edad; 0f si no hay reviews en ese rango; -1f si el local no existe.
     */
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
            LocalDate fechaDeLaReview = r.getFechaGeneracion() != null ? r.getFechaGeneracion() : r.getFechaVisita();
            LocalDate fechaNacimiento = cliente != null ? cliente.getFechaNacimiento() : null;
            if (fechaDeLaReview != null && fechaNacimiento != null) {
                int edadEnEseMomento = Period.between(fechaNacimiento, fechaDeLaReview).getYears();
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

    /**
     * Obtiene los locales de una ciudad y provincia ordenados por su valoración media en orden descendente.
     * Los locales sin reviews son ponderados con 0.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array de locales ordenados por nota media descendente, o null si algún parámetro es nulo.
     */
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

    /**
     * Obtiene todos los locales de una provincia ordenados por su valoración media en orden descendente.
     * Los locales sin reviews son ponderados con 0.
     * @param provincia Provincia de interés.
     * @return Array de locales ordenados por nota media descendente, o null si provincia es nula.
     */
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

    /**
     * Obtiene los bares de una ciudad y provincia ordenados por su valoración media en orden descendente.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array de bares ordenados por nota media descendente.
     */
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

    /**
     * Obtiene los restaurantes de una ciudad y provincia ordenados por su valoración media en orden descendente.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array de restaurantes ordenados por nota media descendente.
     */
    @Override
    public Restaurante[] obtenerRestaurantesOrdenados(String ciudad, String provincia) {
        Restaurante[] restauranteEnZona = listarRestaurantes(ciudad, provincia);
        if (restauranteEnZona == null || restauranteEnZona.length == 0) {
            return restauranteEnZona;
        }

        java.util.Arrays.sort(restauranteEnZona, (r1, r2) -> {
            float nota1 = obtenerValoracionMedia(r1);
            float nota2 = obtenerValoracionMedia(r2);
            return Float.compare(nota2, nota1);
        });
        return restauranteEnZona;
    }

    /**
     * Obtiene los pubs de una ciudad y provincia ordenados por su valoración media en orden descendente.
     * @param ciudad Ciudad de interés.
     * @param provincia Provincia de interés.
     * @return Array de pubs ordenados por nota media descendente.
     */
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
     * El fichero debe tener una única hoja con el nombre del bar en la primera columna;
     * la lectura se detiene en la primera fila cuya primera columna esté vacía.
     * Parsea la dirección física y carga todas las especialidades disponibles desde las columnas 5 en adelante.
     * Las filas con datos inválidos o duplicados se descartan sin interrumpir la importación.
     * @param f Fichero ODS a importar; debe existir y ser no nulo.
     * @return Número de bares incorporados con éxito al sistema.
     */
    public int importaBares(File f) {
        int numeroBares = 0;

        if (f == null || !f.exists()) {
            return 0;
        }

        try {
            SpreadSheet hoja = SpreadSheet.createFromFile(f);
            Sheet tabla = hoja.getSheet(0);

            for (int fila = 0; fila < tabla.getRowCount(); fila++) {

                Object nombreBar = tabla.getCellAt(0, fila).getValue();

                // La primera celda vacía indica el fin de los datos
                if (nombreBar == null || nombreBar.toString().trim().isEmpty()) {
                    break;
                }

                try {
                    String nombre = nombreBar.toString().trim();

                    // Leer celdas de dirección con comprobación de nulidad
                    Object celda1 = tabla.getCellAt(1, fila).getValue();
                    Object celda2 = tabla.getCellAt(2, fila).getValue();
                    Object celda3 = tabla.getCellAt(3, fila).getValue();

                    if (celda1 == null || celda2 == null || celda3 == null) {
                        continue;
                    }

                    String direccionCompleta = celda1.toString().trim();
                    String localidad         = celda2.toString().trim();
                    String provincia         = celda3.toString().trim();

                    // La dirección tiene el formato "Nombre de calle número"
                    int posicion = direccionCompleta.lastIndexOf(" ");
                    if (posicion < 0) {
                        continue;
                    }
                    String calle        = direccionCompleta.substring(0, posicion).trim();
                    String numeroString = direccionCompleta.substring(posicion + 1).trim();
                    int numero = Integer.parseInt(numeroString);

                    Bar bar = new Bar(nombre, new Direccion(localidad, provincia, calle, numero), "");

                    // Importar especialidades (tags) a partir de la columna 5
                    for (int col = 5; col < tabla.getColumnCount(); col++) {
                        try {
                            Object tagObj = tabla.getCellAt(col, fila).getValue();
                            if (tagObj != null && !tagObj.toString().trim().isEmpty()) {
                                bar.agregarEspecialidad(tagObj.toString().trim());
                            }
                        } catch (IndexOutOfBoundsException e) {
                            break;
                        }
                    }

                    if (this.nuevoLocal(bar)) {
                        numeroBares++;
                        
                        // Si la celda 4 contiene un dueño previamente registrado en el sistema, se asocia
                        try {
                            Object celdaDueno = tabla.getCellAt(4, fila).getValue();
                            if (celdaDueno != null && !celdaDueno.toString().trim().isEmpty()) {
                                Usuario u = this.obtenerUsuario(celdaDueno.toString().trim());
                                if (u instanceof Dueño) {
                                    this.asociarLocal(bar, (Dueño) u);
                                }
                            }
                        } catch (Exception ignored) {
                            // Ignorar si la celda no existe o no corresponde a un dueño registrado
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Fila con datos inválidos: se descarta y se continúa con la siguiente
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el fichero ODS: " + e.getMessage());
        }

        return numeroBares;
    }

    /**
     * Obtiene un array con todos los locales registrados en el sistema.
     * @return Array de locales registrados.
     */
    public Local[] obtenerTodosLosLocales() {
        return locales.toArray(new Local[locales.size()]);
    }

    /**
     * Obtiene los dueños asociados a un local.
     * @param l El local a consultar.
     * @return Array con los dueños del local, o array vacío si no tiene dueños asignados.
     */
    public Dueño[] obtenerDueñosDeLocal(Local l) {
        if (l != null && propietariosDeLocal.containsKey(l)) {
            Set<Dueño> dueños = propietariosDeLocal.get(l);
            return dueños.toArray(new Dueño[dueños.size()]);
        }
        return new Dueño[0];
    }
}
