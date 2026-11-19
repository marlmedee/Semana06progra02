package semana06progra02.modelo;

import java.io.*;
public class Bitacora {

    private File archivo;

    public Bitacora(String nombreArchivo) {
        archivo = new File(nombreArchivo);
    }
    public synchronized void registrar(String accion, String ruta) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(archivo, true);
            fw.write(Utilidades.fechaHoraActual() + " | " + accion + " | " + ruta + "\n");
        } catch (IOException e) {
            System.out.println("No se pudo escribir en la bitacora: " + e.getMessage());
        } finally {
            cerrar(fw);
        }
    }

    public String leer() throws IOException {
        if (!archivo.exists()) {
            return "(La bitacora esta vacia)";
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        try {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        }finally{
            br.close();
        }
        return sb.toString();
    }

    public String getRuta() {
        return archivo.getAbsolutePath();
    }

    private void cerrar(FileWriter fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
            }
        }
    }
}
