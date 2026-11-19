package semana06progra02.modelo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Reporte {
    public static void exportar(Estadisticas est, File destino) throws IOException {
        FileWriter fw = new FileWriter(destino);
        try {
            if (destino.getName().toLowerCase().endsWith(".csv")) {
                fw.write("Reporte generado," + Utilidades.fechaHoraActual() + "\n\n");
                fw.write(est.generarCsv());
            }
            else{
                fw.write("REPORTE DE ESTADISTICAS\n");
                fw.write("Generado: " + Utilidades.fechaHoraActual() + "\n");
                fw.write("-----------------------------------------\n");
                fw.write(est.generarTexto());
            }
        } finally {
            fw.close();
        }
    }
}
