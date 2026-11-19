package semana06progra02.modelo;

import java.io.*;
import java.util.HashMap;

public class Configuracion {

    private File archivo;
    private HashMap<String, String> datos = new HashMap<>();

    public Configuracion(String nombreArchivo) {
        archivo = new File(nombreArchivo);
    }

    public void cargar() throws IOException {
        datos.clear();
        if (!archivo.exists()) {
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        try {
            String linea;
            while ((linea = br.readLine()) != null) {
                int igual = linea.indexOf('=');
                if (igual > 0) {
                    datos.put(linea.substring(0, igual), linea.substring(igual + 1));
                }
            }
        } finally {
            br.close();
        }
    }

    public void guardar() throws IOException {
        FileWriter fw = new FileWriter(archivo);
        try {
            for (String clave : datos.keySet()) {
                fw.write(clave + "=" + datos.get(clave) + "\n");
            }
        } finally {
            fw.close();
        }
    }
    public String get(String clave) {
        String valor = datos.get(clave);
        if (valor == null) {
            return "";
        }
        return valor;
    }
    public void put(String clave, String valor) {
        datos.put(clave, valor.replace("\n", " "));
    }
}
