package GSILabs.BSystem;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Local;
import GSILabs.BModel.Restaurante;
import GSILabs.BModel.Review;
import GSILabs.BModel.Usuario;
import GSILabs.connect.AdminGateway;
import GSILabs.connect.ClientGateway;
import GSILabs.connect.LocalFinder;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fachada pública remota del sistema de información que implementa las interfaces
 * RMI {@link LocalFinder}, {@link ClientGateway} y {@link AdminGateway} mediante
 * composición y delegación sobre una instancia de {@link BusinessSystem}.
 *
 * <p>Se emplea composición en lugar de herencia directa porque
 * {@link LeisureOffice#eliminaReview(Review)} devuelve {@code boolean} primitivo,
 * mientras que {@link AdminGateway#eliminaReview(Review)} devuelve {@link Boolean},
 * lo cual impide compilar ambas interfaces en una misma clase sin alterar las
 * interfaces oficiales.
 *
 * @author Joel Vílchez, Urko Inés, Aimar Ijurko, Iker Fernández, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class PublicBusinessSystem implements LocalFinder, ClientGateway, AdminGateway {

    private final BusinessSystem businessSystem;
    private long contadorUsuariosFalsos = 0;

    /**
     * Constructor por defecto. Crea una nueva instancia vacía de
     * {@link BusinessSystem} como núcleo de negocio.
     */
    public PublicBusinessSystem() {
        this(new BusinessSystem());
    }

    /**
     * Constructor que envuelve un {@link BusinessSystem} existente.
     *
     * @param businessSystem Sistema de negocio interno sobre el que se delegan
     *                       las operaciones. No puede ser {@code null}.
     * @throws IllegalArgumentException Si {@code businessSystem} es {@code null}.
     */
    public PublicBusinessSystem(BusinessSystem businessSystem) {
        if (businessSystem == null) {
            throw new IllegalArgumentException("El BusinessSystem no puede ser nulo.");
        }
        this.businessSystem = businessSystem;
    }

    /**
     * Devuelve la instancia local de {@link BusinessSystem} encapsulada por esta fachada.
     * Este método es de conveniencia local y no está expuesto en las interfaces remotas.
     *
     * @return El {@link BusinessSystem} subyacente.
     */
    public BusinessSystem getBusinessSystem() {
        return businessSystem;
    }

    /**
     * Normaliza un texto eliminando espacios al inicio/final y colapsando
     * secuencias de espacios internos a un único espacio.
     *
     * @param texto Cadena de entrada.
     * @return Cadena normalizada, o {@code null} si es nula o queda vacía.
     */
    private static String normalizarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().replaceAll("\\s+", " ");
        return normalizado.isEmpty() ? null : normalizado;
    }

    /**
     * Recupera el local cuyo nombre coincide exactamente (ignorando mayúsculas,
     * minúsculas y espacios redundantes) con el especificado.
     *
     * @param name Nombre del local buscado.
     * @return El {@link Local} encontrado, o {@code null} si el parámetro es
     *         nulo/vacío o no existe coincidencia exacta.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Local getLocal(String name) throws RemoteException {
        String buscado = normalizarTexto(name);
        if (buscado == null) {
            return null;
        }
        for (Local local : businessSystem.obtenerTodosLosLocales()) {
            String nombreLocal = normalizarTexto(local.getNombre());
            if (nombreLocal != null && nombreLocal.equalsIgnoreCase(buscado)) {
                return local;
            }
        }
        return null;
    }

    /**
     * Recupera todos los locales cuyo nombre coincide parcial o totalmente
     * (ignorando mayúsculas, minúsculas y espacios redundantes) con el texto indicado.
     *
     * @param name Nombre completo o subcadena a buscar.
     * @return Array de locales coincidentes; nunca {@code null} (array vacío si
     *         no hay coincidencias o el parámetro es nulo/vacío).
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Local[] getLocals(String name) throws RemoteException {
        String buscado = normalizarTexto(name);
        if (buscado == null) {
            return new Local[0];
        }
        String buscadoLower = buscado.toLowerCase(Locale.ROOT);
        List<Local> coincidencias = new ArrayList<>();
        for (Local local : businessSystem.obtenerTodosLosLocales()) {
            String nombreLocal = normalizarTexto(local.getNombre());
            if (nombreLocal != null && nombreLocal.toLowerCase(Locale.ROOT).contains(buscadoLower)) {
                coincidencias.add(local);
            }
        }
        return coincidencias.toArray(new Local[0]);
    }

    /**
     * Incorpora una nueva review al sistema validando sus campos y re-enlazándola
     * con las instancias canónicas de {@link Cliente} y {@link Local} registradas
     * en el servidor antes de delegar en {@link BusinessSystem#nuevaReview(Review)}.
     *
     * @param r Review recibida (habitualmente deserializada vía RMI).
     * @return {@code true} si la review se insertó con éxito; {@code false} si es
     *         inválida, el cliente/local no existen o ya existe para esa fecha.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized boolean insertaReview(Review r) throws RemoteException {
        if (r == null || r.getCliente() == null || r.getLocal() == null
                || r.getFechaVisita() == null || r.getFechaGeneracion() == null) {
            return false;
        }
        Usuario uReg = businessSystem.obtenerUsuario(r.getCliente().getNick());
        if (!(uReg instanceof Cliente)) {
            return false;
        }
        Local lReg = businessSystem.obtenerLocal(r.getLocal().getDireccion());
        if (lReg == null || !lReg.equals(r.getLocal())) {
            return false;
        }
        try {
            Review reviewCanonica = new Review(
                    (Cliente) uReg,
                    lReg,
                    r.getValoracion(),
                    r.getComentario(),
                    r.getFechaVisita(),
                    r.getFechaGeneracion());
            return businessSystem.nuevaReview(reviewCanonica);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Retira una review del sistema si existe y no tiene una contestación asociada.
     *
     * @param r Review a eliminar.
     * @return {@code true} si existía, no tenía contestación y se eliminó;
     *         {@code false} en caso contrario.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized boolean quitaReview(Review r) throws RemoteException {
        return businessSystem.eliminaReview(r);
    }

    /**
     * Devuelve el {@link Bar} con mejor valoración media de la ciudad indicada.
     *
     * @param ciudad Nombre de la ciudad (ignorando mayúsculas y espacios externos/sobrantes).
     * @return El mejor bar de la ciudad, o {@code null} si la ciudad es nula/vacía
     *         o no existen bares registrados en ella.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Bar mejorBar(String ciudad) throws RemoteException {
        String ciudadNorm = normalizarTexto(ciudad);
        if (ciudadNorm == null) {
            return null;
        }
        Bar mejor = null;
        float mejorMedia = -1f;
        for (Local local : businessSystem.obtenerTodosLosLocales()) {
            if (local instanceof Bar) {
                Direccion dir = local.getDireccion();
                String localidad = dir != null ? normalizarTexto(dir.getLocalidad()) : null;
                if (localidad != null && localidad.equalsIgnoreCase(ciudadNorm)) {
                    float media = businessSystem.obtenerValoracionMedia(local);
                    if (media > mejorMedia) {
                        mejorMedia = media;
                        mejor = (Bar) local;
                    }
                }
            }
        }
        return mejor;
    }

    /**
     * Devuelve los {@code num} mejores restaurantes de una ciudad ordenados de forma
     * descendente por su valoración media, rellenando con {@code null} las posiciones
     * sobrantes si existen menos candidatos que {@code num}.
     *
     * @param ciudad Nombre de la ciudad.
     * @param num    Número máximo de restaurantes en el array devuelto.
     * @return Array de tamaño {@code num} con los mejores restaurantes (y posibles
     *         {@code null} al final), o {@code null} si {@code num} es nulo o negativo.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Restaurante[] mejoresRestaurantes(String ciudad, Integer num) throws RemoteException {
        if (num == null || num < 0) {
            return null;
        }
        if (num == 0) {
            return new Restaurante[0];
        }
        Restaurante[] resultado = new Restaurante[num];
        String ciudadNorm = normalizarTexto(ciudad);
        if (ciudadNorm == null) {
            return resultado;
        }

        List<Restaurante> candidatos = new ArrayList<>();
        for (Local local : businessSystem.obtenerTodosLosLocales()) {
            if (local instanceof Restaurante) {
                Direccion dir = local.getDireccion();
                String localidad = dir != null ? normalizarTexto(dir.getLocalidad()) : null;
                if (localidad != null && localidad.equalsIgnoreCase(ciudadNorm)) {
                    candidatos.add((Restaurante) local);
                }
            }
        }

        candidatos.sort((r1, r2) -> Float.compare(
                businessSystem.obtenerValoracionMedia(r2),
                businessSystem.obtenerValoracionMedia(r1)));

        int limite = Math.min(num, candidatos.size());
        for (int i = 0; i < limite; i++) {
            resultado[i] = candidatos.get(i);
        }
        return resultado;
    }

    /**
     * Elimina un local del sistema junto con sus reservas, reviews y contestaciones asociadas.
     *
     * @param l Local a eliminar.
     * @return {@code true} si el local existía y se eliminó; {@code false} en caso contrario.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Boolean eliminaLocal(Local l) throws RemoteException {
        return businessSystem.eliminarLocal(l);
    }

    /**
     * Elimina todas las reviews (y sus contestaciones asociadas) de un local existente,
     * manteniendo el propio local en el sistema.
     *
     * @param l Local cuyas reviews deben eliminarse.
     * @return {@code true} si el local existe en el sistema y se limpiaron sus reviews;
     *         {@code false} si el local es nulo o no está registrado.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Boolean eliminaReviewsDeLocal(Local l) throws RemoteException {
        Review[] reviewsLocal = businessSystem.verReviews(l);
        if (reviewsLocal == null) {
            return false;
        }
        for (Review rev : reviewsLocal) {
            businessSystem.eliminaContestacion(rev);
            businessSystem.eliminaReview(rev);
        }
        return true;
    }

    /**
     * Elimina una review del sistema desde el perfil de administración, borrando
     * previamente su contestación si existiera.
     *
     * @param r Review a eliminar.
     * @return {@code true} si la review existía y se eliminó; {@code false} en caso contrario.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Boolean eliminaReview(Review r) throws RemoteException {
        if (r == null) {
            return false;
        }
        if (businessSystem.tieneContestacion(r)) {
            businessSystem.eliminaContestacion(r);
        }
        return businessSystem.eliminaReview(r);
    }

    /**
     * Elimina todas las reviews escritas por un cliente determinado (y sus
     * contestaciones asociadas), conservando al cliente y sus reservas.
     *
     * @param c Cliente cuyas reviews deben eliminarse.
     * @return Número exacto de reviews eliminadas ({@code 0} si existe sin reviews),
     *         o {@code -1} si el parámetro es inválido o no existe un {@link Cliente}
     *         registrado con ese nick.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Integer eliminaReviewsDeUsuario(Cliente c) throws RemoteException {
        if (c == null || c.getNick() == null) {
            return -1;
        }
        Usuario registrado = businessSystem.obtenerUsuario(c.getNick());
        if (!(registrado instanceof Cliente)) {
            return -1;
        }

        int eliminadas = 0;
        for (Local local : businessSystem.obtenerTodosLosLocales()) {
            Review[] reviewsLocal = businessSystem.verReviews(local);
            if (reviewsLocal == null) {
                continue;
            }
            for (Review rev : reviewsLocal) {
                if (rev.getCliente() != null && rev.getCliente().equals(registrado)) {
                    if (businessSystem.tieneContestacion(rev)) {
                        businessSystem.eliminaContestacion(rev);
                    }
                    if (businessSystem.eliminaReview(rev)) {
                        eliminadas++;
                    }
                }
            }
        }
        return eliminadas;
    }

    /**
     * Crea un cliente falso con un nick único garantizado y registra en su nombre
     * una review falsa con la puntuación indicada sobre un local existente.
     *
     * @param l          Local registrado al que añadir la review falsa.
     * @param puntuacion Puntuación comprendida entre 0 y 5.
     * @return {@code true} si la operación se completó; {@code false} si algún argumento
     *         es inválido o el local no existe en el sistema.
     * @throws RemoteException Si se produce un error de comunicación RMI.
     */
    @Override
    public synchronized Boolean insertaReviewFalsa(Local l, Integer puntuacion) throws RemoteException {
        if (l == null || puntuacion == null || puntuacion < 0 || puntuacion > 5) {
            return false;
        }
        Local localReg = businessSystem.obtenerLocal(l.getDireccion());
        if (localReg == null || !localReg.equals(l)) {
            return false;
        }

        String nickFalso;
        do {
            contadorUsuariosFalsos++;
            nickFalso = "fakeUser_" + contadorUsuariosFalsos;
        } while (businessSystem.existeNick(nickFalso));

        Cliente usuarioFalso;
        try {
            usuarioFalso = new Cliente(nickFalso, "fakePass123", LocalDate.now().minusYears(25));
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (!businessSystem.nuevoUsuario(usuarioFalso)) {
            return false;
        }

        LocalDate hoy = LocalDate.now();
        try {
            Review reviewFalsa = new Review(
                    usuarioFalso,
                    localReg,
                    puntuacion,
                    "Review generada por administrador",
                    hoy,
                    hoy);
            if (!businessSystem.nuevaReview(reviewFalsa)) {
                businessSystem.eliminaUsuario(usuarioFalso);
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            businessSystem.eliminaUsuario(usuarioFalso);
            return false;
        }
    }
}
