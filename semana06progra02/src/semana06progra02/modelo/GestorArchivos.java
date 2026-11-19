package semana06progra02.modelo;

import java.io.*;

public class GestorArchivos {

    public static final long MAX_EDITOR = 1024 * 1024;

    private static final String[] EXT_TEXTO = {".txt", ".java", ".csv", ".log", ".xml",".html", ".css", ".js", ".md", ".properties", ".json", ".py", ".c", ".cpp", ".h"};
    private static final String CARACTERES_PROHIBIDOS = "\\/:*?\"<>|";
    private Bitacora bitacora;
    public GestorArchivos(Bitacora bitacora) {
        this.bitacora = bitacora;
    }
    public void validarNombre(String nombre) throws DatosInvalidosException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatosInvalidosException("El nombre no puede estar vacio");
        }
        if (nombre.equals(".") || nombre.equals("..")) {
            throw new DatosInvalidosException("Nombre invalido: " + nombre);
        }
        for (int i = 0; i < nombre.length(); i++) {
            if (CARACTERES_PROHIBIDOS.indexOf(nombre.charAt(i)) != -1) {
                throw new DatosInvalidosException("El nombre no puede tener: \\ / : * ? \" < > |");
            }
        }
    }
    public File crearCarpeta(File padre, String ruta) throws DatosInvalidosException, IOException {
        String[] partes = ruta.trim().split("[/\\\\]");
        File actual = padre;
        for (String parte : partes) {
            validarNombre(parte);
            actual = new File(actual, parte);
        }
        if (actual.exists()) {
            throw new DatosInvalidosException("Ya existe: " + actual.getAbsolutePath());
        }
        if (!actual.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + actual.getAbsolutePath());
        }
        bitacora.registrar("CREAR CARPETA", actual.getAbsolutePath());
        return actual;
    }
    public void escribirTexto(File archivo, String contenido) throws IOException {
        FileWriter fw = new FileWriter(archivo);
        try {
            fw.write(contenido);
        } finally {
            fw.close();
        }
    }
    public File crearArchivo(File carpeta, String nombre, String contenido)
            throws DatosInvalidosException, IOException {
        validarNombre(nombre);
        File nuevo = new File(carpeta, nombre);
        escribirTexto(nuevo, contenido);
        bitacora.registrar("CREAR ARCHIVO", nuevo.getAbsolutePath());
        return nuevo;
    }

    public File renombrar(File archivo, String nuevoNombre) throws DatosInvalidosException, IOException {
        validarNombre(nuevoNombre);
        File nuevo = new File(archivo.getParentFile(), nuevoNombre);
        if (nuevo.exists()) {
            throw new DatosInvalidosException("Ya existe un elemento con ese nombre");
        }
        if (!archivo.renameTo(nuevo)) {
            throw new IOException("No se pudo renombrar " + archivo.getName());
        }
        bitacora.registrar("RENOMBRAR", archivo.getAbsolutePath() + " -> " + nuevo.getName());
        return nuevo;
    }

    public File copiar(File origen, File carpetaDestino) throws IOException {
        File destino = new File(carpetaDestino, origen.getName());
        copiarBytes(origen, destino);
        bitacora.registrar("COPIAR", origen.getAbsolutePath() + " -> " + destino.getAbsolutePath());
        return destino;
    }

    private void copiarBytes(File origen, File destino) throws IOException {
        if(origen.isDirectory()) {
            throw new IOException("Solo se pueden copiar archivos, no carpetas");
        }
        if (origen.getCanonicalPath().equals(destino.getCanonicalPath())){
            throw new IOException("El origen y el destino son el mismo archivo!");
        }
        FileInputStream entrada = new FileInputStream(origen);
        FileOutputStream salida = null;
        try {
            salida = new FileOutputStream(destino);
            byte[] buffer = new byte[4096];
            int leidos;
            while ((leidos = entrada.read(buffer)) != -1) {
                salida.write(buffer, 0, leidos);
            }
        } finally {
            entrada.close();
            if (salida != null) {
                salida.close();
            }
        }
    }

    public File mover(File origen, File carpetaDestino) throws IOException {
        File destino = new File(carpetaDestino, origen.getName());
        if (origen.getCanonicalPath().equals(destino.getCanonicalPath())) {
            throw new IOException("El archivo ya esta en esa carpeta");
        }
        if (destino.exists() && !destino.delete()) {
            throw new IOException("No se pudo reemplazar " + destino.getAbsolutePath());
        }
        if (!origen.renameTo(destino)) {
            copiarBytes(origen, destino);
            if (!origen.delete()) {
                throw new IOException("Se copio pero no se pudo borrar el original");
            }
        }
        bitacora.registrar("MOVER", origen.getAbsolutePath() + " -> " + destino.getAbsolutePath());
        return destino;
    }

    public void eliminar(File archivo) throws IOException {
        borrarRecursivo(archivo);
        bitacora.registrar("ELIMINAR", archivo.getAbsolutePath());
    }

    private void borrarRecursivo(File archivo) throws IOException {
        boolean esEnlace = java.nio.file.Files.isSymbolicLink(archivo.toPath());
        if (archivo.isDirectory() && !esEnlace) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File h : hijos) {
                    borrarRecursivo(h);
                }
            }
        }
        if (!archivo.delete()) {
            throw new IOException("No se pudo eliminar " + archivo.getAbsolutePath());
        }
    }

    public boolean esTexto(File archivo) {
        String ext = Utilidades.obtenerExtension(archivo);
        for (String e : EXT_TEXTO) {
            if (e.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    public String leerTexto(File archivo) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        try {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } finally {
            br.close();
        }
        bitacora.registrar("ABRIR", archivo.getAbsolutePath());
        return sb.toString();
    }
    public void guardarTexto(File archivo, String contenido) throws IOException {
        escribirTexto(archivo, contenido);
        bitacora.registrar("GUARDAR", archivo.getAbsolutePath());
    }
}
