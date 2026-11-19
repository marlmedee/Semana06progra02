package semana06progra02.modelo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilidades {
    public static String formatearTamano(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes/1024.0;
        if (kb < 1024) {
            return String.format("%.2f KB", kb);
        }
        double mb = kb/1024.0;
        if (mb < 1024) {
            return String.format("%.2f MB", mb);
        }
        return String.format("%.2f GB", mb / 1024.0);
    }
    public static String obtenerExtension(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');
        if (punto <= 0 || punto == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(punto).toLowerCase();
    }
    public static String formatearFecha(long milisegundos) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(milisegundos));
    }

    public static String fechaHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
