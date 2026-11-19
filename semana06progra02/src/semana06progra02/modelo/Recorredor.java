package semana06progra02.modelo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public abstract class Recorredor {

    protected Bitacora bitacora;
    protected int profundidadMax; 
    protected ProgresoListener listener;
    protected volatile boolean cancelado = false;
    private HashSet<String> visitadas = new HashSet<>();

    public Recorredor(Bitacora bitacora, int profundidadMax) {
        this.bitacora = bitacora;
        this.profundidadMax = profundidadMax;
    }

    public void setListener(ProgresoListener listener) {
        this.listener = listener;
    }

    public void cancelar() {
        cancelado = true;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public static File validarRuta(String ruta) throws RutaInvalidaException {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new RutaInvalidaException("La ruta no puede estar vacia");
        }
        File carpeta = new File(ruta.trim());
        if (!carpeta.exists()) {
            throw new RutaInvalidaException("La ruta no existe: " + ruta);
        }
        if (!carpeta.isDirectory()) {
            throw new RutaInvalidaException("La ruta no es un directorio: " + ruta);
        }
        return carpeta;
    }

    protected File[] listar(File carpeta) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) {
            bitacora.registrar("SIN PERMISO (omitida)", carpeta.getAbsolutePath());
        }
        return hijos;
    }

    protected boolean yaVisitada(File carpeta) {
        try {
            String canonica = carpeta.getCanonicalPath();
            if (visitadas.contains(canonica)) {
                return true;
            }
            visitadas.add(canonica);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    protected boolean puedeEntrar(int nivel) {
        return profundidadMax == 0 || nivel < profundidadMax;
    }
    protected void avisar(int hechos, int total, File actual) {
        if (listener != null && total > 0) {
            listener.actualizar(hechos * 100 / total, actual.getAbsolutePath());
        }
    }
}
