package semana06progra02.modelo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Filtros {

    public String texto = "";
    public String extension = "";
    public long minBytes = -1;
    public long maxBytes = -1;
    public long fechaDesde = -1;
    public long fechaHasta = -1;

    public static Filtros crear(String texto, String ext, String min, String max,
            String desde, String hasta) throws DatosInvalidosException {
        Filtros f = new Filtros();
        f.texto = texto.trim().toLowerCase();
        f.extension = limpiarExtension(ext);
        long minKb = leerNumero(min, "Tamaño minimo");
        long maxKb = leerNumero(max, "Tamaño maximo");
        if (minKb != -1 && maxKb != -1 && minKb > maxKb) {
            throw new DatosInvalidosException("El tamaño minimo no puede ser mayor que el maximo.");
        }
        if (minKb != -1) {
            f.minBytes = minKb * 1024;
        }
        if (maxKb != -1) {
            f.maxBytes = maxKb * 1024;
        }

        f.fechaDesde = leerFecha(desde, "Fecha desde");
        f.fechaHasta = leerFecha(hasta, "Fecha hasta");
        if (f.fechaHasta != -1) {
            f.fechaHasta = f.fechaHasta + (24L * 60 * 60 * 1000) - 1;
        }
        if (f.fechaDesde != -1 && f.fechaHasta != -1 && f.fechaDesde > f.fechaHasta) {
            throw new DatosInvalidosException("La fecha desde no puede ser despues de la fecha hasta");
        }

        if (f.texto.isEmpty() && !f.tieneFiltros()) {
            throw new DatosInvalidosException("Escriba un texto de busqueda o use al menos un filitro");
        }
        return f;
    }

    public boolean tieneFiltros() {
        return !extension.isEmpty() || minBytes != -1 || maxBytes != -1|| fechaDesde != -1 || fechaHasta != -1;
    }

    private static String limpiarExtension(String ext) {
        String e = ext.trim().toLowerCase();
        if(e.isEmpty()){
            return "";
        }
        if(!e.startsWith(".")) {
            e = "." + e;
        }
        return e;
    }

    private static long leerNumero(String valor, String campo) throws DatosInvalidosException {
        String v = valor.trim();
        if (v.isEmpty()) {
            return -1;
        }
        long n;
        try {
            n = Long.parseLong(v);
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException(campo + " debe ser un numero entero.");
        }
        if (n < 0) {
            throw new DatosInvalidosException(campo + " no puede ser negativo.");
        }
        return n;
    }

    private static long leerFecha(String valor, String campo) throws DatosInvalidosException {
        String v = valor.trim();
        if (v.isEmpty()) {
            return -1;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date d = sdf.parse(v);
            return d.getTime();
        } catch (ParseException e) {
            throw new DatosInvalidosException(campo + " debe tener el formato dd/MM/yyyy.");
        }
    }
}
