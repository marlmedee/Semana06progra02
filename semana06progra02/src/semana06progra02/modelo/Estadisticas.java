package semana06progra02.modelo;

import java.io.File;

public class Estadisticas extends Recorredor {

    public static final String[] CATEGORIAS = {".txt", ".java", ".pdf", "otros"};

    private int[] cantidad = new int[4];
    private long[] tamano = new long[4];
    private File masGrande;
    private File masReciente;
    private int profundidadAlcanzada = 0;
    private int carpetas = 0;
    private int carpetasVacias = 0;
    private File raiz;

    public Estadisticas(Bitacora bitacora, int profundidadMax) {
        super(bitacora, profundidadMax);
    }

    public void calcular(File raiz) {
        this.raiz = raiz;
        yaVisitada(raiz);
        recorrer(raiz, 0);
    }

    private void recorrer(File carpeta, int nivel) {
        File[] hijos = listar(carpeta);
        if (hijos == null) {
            return;
        }
        if (hijos.length == 0 && nivel > 0) {
            carpetasVacias++;
        }
        for (int i = 0; i < hijos.length; i++) {
            if (cancelado) {
                return;
            }
            File f = hijos[i];
            if(nivel+ 1>profundidadAlcanzada) {
                profundidadAlcanzada = nivel + 1;
            }
            if(f.isDirectory()) {
                carpetas++;
                if (puedeEntrar(nivel + 1) && !yaVisitada(f)) {
                    recorrer(f, nivel + 1);
                }
            }
            else {
                contarArchivo(f);
            }
            if (nivel == 0) {
                avisar(i + 1, hijos.length, f);
            }
        }
    }

    private void contarArchivo(File f) {
        int pos = buscarCategoria(Utilidades.obtenerExtension(f));
        cantidad[pos]++;
        tamano[pos] += f.length();

        if (masGrande == null || f.length() > masGrande.length()) {
            masGrande = f;
        }
        if (masReciente == null || f.lastModified() > masReciente.lastModified()) {
            masReciente = f;
        }
    }
    private int buscarCategoria(String extension) {
        for (int i = 0; i < 3; i++) {
            if (CATEGORIAS[i].equals(extension)) {
                return i;
            }
        }
        return 3;
    }

    public int getTotalArchivos() {
        return cantidad[0] + cantidad[1] + cantidad[2] + cantidad[3];
    }

    public long getTotalBytes() {
        return tamano[0] + tamano[1] + tamano[2] + tamano[3];
    }

    public double getPorcentaje(int pos) {
        if (getTotalArchivos() == 0) {
            return 0;
        }
        return cantidad[pos] * 100.0 / getTotalArchivos();
    }

    public int getCantidad(int pos) {
        return cantidad[pos];
    }

    public long getTamano(int pos) {
        return tamano[pos];
    }

    public String generarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("Directorio: ").append(raiz.getAbsolutePath()).append("\n\n");
        sb.append(String.format("%-8s %10s %15s %10s%n", "Tipo", "Cantidad", "Tamaño", "%"));
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%-8s %10d %15s %9.2f%%%n", CATEGORIAS[i], cantidad[i],
                    Utilidades.formatearTamano(tamano[i]), getPorcentaje(i)));
        }
        sb.append(String.format("%-8s %10d %15s%n", "TOTAL", getTotalArchivos(),
                Utilidades.formatearTamano(getTotalBytes())));
        sb.append("\n");
        sb.append("Archivo mas grande: ").append(textoArchivo(masGrande, true)).append("\n");
        sb.append("Archivo mas reciente: ").append(textoArchivo(masReciente, false)).append("\n");
        sb.append("Profundidad maxima alcanzada: ").append(profundidadAlcanzada).append("\n");
        sb.append("Numero de carpetas: ").append(carpetas).append("\n");
        sb.append("Carpetas vacias: ").append(carpetasVacias).append("\n");
        return sb.toString();
    }

    public String generarCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tipo,Cantidad,Bytes,Tamano,Porcentaje\n");
        for (int i = 0; i < 4; i++) {
            sb.append(CATEGORIAS[i]).append(",").append(cantidad[i]).append(",")
                    .append(tamano[i]).append(",").append(Utilidades.formatearTamano(tamano[i]))
                    .append(",").append(String.format("%.2f", getPorcentaje(i)).replace(',', '.'))
                    .append("\n");
        }
        sb.append("TOTAL,").append(getTotalArchivos()).append(",").append(getTotalBytes())
                .append(",").append(Utilidades.formatearTamano(getTotalBytes())).append(",100\n");
        sb.append("\n");
        sb.append("Directorio,\"").append(raiz.getAbsolutePath()).append("\"\n");
        sb.append("Archivo mas grande,\"").append(textoArchivo(masGrande, true)).append("\"\n");
        sb.append("Archivo mas reciente,\"").append(textoArchivo(masReciente, false)).append("\"\n");
        sb.append("Profundidad maxima,").append(profundidadAlcanzada).append("\n");
        sb.append("Carpetas,").append(carpetas).append("\n");
        sb.append("Carpetas vacias,").append(carpetasVacias).append("\n");
        return sb.toString();
    }

    private String textoArchivo(File f, boolean conTamano) {
        if (f == null) {
            return "(ninguno)";
        }
        if (conTamano) {
            return f.getAbsolutePath() + " (" + Utilidades.formatearTamano(f.length()) + ")";
        }
        return f.getAbsolutePath() + " (" + Utilidades.formatearFecha(f.lastModified()) + ")";
    }
}
