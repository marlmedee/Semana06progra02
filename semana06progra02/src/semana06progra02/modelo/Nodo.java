package semana06progra02.modelo;

import java.io.File;
import java.util.ArrayList;

public class Nodo {

    private File archivo;
    private ArrayList<Nodo> hijos = new ArrayList<>();

    public Nodo(File archivo) {
        this.archivo = archivo;
    }

    public File getArchivo() {
        return archivo;
    }

    public boolean esCarpeta() {
        return archivo.isDirectory();
    }

    public ArrayList<Nodo> getHijos() {
        return hijos;
    }

    public void agregarHijo(Nodo n) {
        hijos.add(n);
    }

    @Override
    public String toString() {
        if (archivo.getName().isEmpty()) {
            return archivo.getAbsolutePath(); 
        }
        return archivo.getName();
    }
}
