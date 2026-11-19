package semana06progra02.modelo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Duplicados extends Recorredor {

    private Map<String, List<File>> mapa = new HashMap<>();

    public Duplicados(Bitacora bitacora, int profundidadMax) {
        super(bitacora, profundidadMax);
    }
    public Map<String, List<File>> buscar(File raiz) {
        mapa.clear();
        yaVisitada(raiz);
        recorrer(raiz, 0);

        Map<String, List<File>> repetidos = new HashMap<>();
        for (String clave : mapa.keySet()) {
            if (mapa.get(clave).size() > 1) {
                repetidos.put(clave, mapa.get(clave));
            }
        }
        return repetidos;
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
                if (puedeEntrar(nivel+1)&& !yaVisitada(f)){
                    recorrer(f, nivel + 1);
                }
            }
            else{
                agregar(f);
            }
            if(nivel == 0) {
                avisar(i + 1, hijos.length, f);
            }
        }
    }
    private void agregar(File f) {
        String clave = f.getName().toLowerCase() + "|" + f.length();
        if (!mapa.containsKey(clave)) {
            mapa.put(clave, new ArrayList<File>());
        }
        mapa.get(clave).add(f);
    }
    public static String aTexto(Map<String, List<File>> grupos) {
        if (grupos.isEmpty()) {
            return "No se encontraron archivos duplicados ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Grupos de duplicados encontrados: ").append(grupos.size()).append("\n\n");
        for (String clave : grupos.keySet()) {
            List<File> lista = grupos.get(clave);
            File primero = lista.get(0);
            sb.append(primero.getName()).append(" - ")
                    .append(Utilidades.formatearTamano(primero.length()))
                    .append(" (").append(lista.size()).append(" copias)\n");
            for (File f : lista) {
                sb.append("    ").append(f.getAbsolutePath()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
