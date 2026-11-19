package semana06progra02.modelo;

import java.io.File;
import java.util.Arrays;


public class ExploradorArbol extends Recorredor {

    public ExploradorArbol(Bitacora bitacora, int profundidadMax) {
        super(bitacora, profundidadMax);
    }
    public Nodo construir(File raiz) {
        Nodo nodoRaiz = new Nodo(raiz);
        yaVisitada(raiz);
        llenar(nodoRaiz, 0);
        return nodoRaiz;
    }

    private void llenar(Nodo nodo, int nivel) {
        File[] hijos = listar(nodo.getArchivo());
        if (hijos == null) {
            return;
        }
        Arrays.sort(hijos);
        for (int i = 0; i < hijos.length; i++) {
            if (cancelado) {
                return;
            }
            Nodo hijo = new Nodo(hijos[i]);
            nodo.agregarHijo(hijo);
            if(hijos[i].isDirectory()&&puedeEntrar(nivel+1)&&!yaVisitada(hijos[i])) {
                llenar(hijo, nivel + 1);
            }
            if (nivel == 0) {
                avisar(i + 1, hijos.length, hijos[i]);
            }
        }
    }
}
