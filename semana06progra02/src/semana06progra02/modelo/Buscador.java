package semana06progra02.modelo;

import java.io.File;
import java.util.ArrayList;

public class Buscador extends Recorredor {

    private ArrayList<File> resultados = new ArrayList<>();
    private Filtros filtros;

    public Buscador(Bitacora bitacora, int profundidadMax) {
        super(bitacora, profundidadMax);
    }
    public ArrayList<File> buscar(File raiz, Filtros filtros) {
        this.filtros = filtros;
        resultados.clear();
        yaVisitada(raiz);
        recorrer(raiz, 0);
        return resultados;
    }

    private void recorrer(File carpeta, int nivel) {
        File[] hijos = listar(carpeta);
        if (hijos == null) {
            return;
        }
        for (int i = 0; i < hijos.length; i++) {
            if (cancelado) {
                return;
            }
            File f = hijos[i];
            if (f.isDirectory()) {
                if (puedeEntrar(nivel + 1) && !yaVisitada(f)) {
                    recorrer(f, nivel + 1);
                }
            }
            else if (cumple(f)){
                resultados.add(f);
            }
            if (nivel == 0) {
                avisar(i + 1, hijos.length, f);
            }
        }
    }
    private boolean cumple(File f) {
        if(!f.getName().toLowerCase().contains(filtros.texto)){
            return false;
        }
        if(!filtros.extension.isEmpty() && !filtros.extension.equals(Utilidades.obtenerExtension(f))){
            return false;
        }
        if(filtros.minBytes != -1 && f.length() < filtros.minBytes) {
            return false;
        }
        if(filtros.maxBytes != -1 && f.length() > filtros.maxBytes) {
            return false;
        }
        if(filtros.fechaDesde != -1 && f.lastModified() < filtros.fechaDesde) {
            return false;
        }
        if(filtros.fechaHasta != -1 && f.lastModified() > filtros.fechaHasta){
            return false;
        }
        return true;
    }
}
