package semana06progra02;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import semana06progra02.controlador.Controlador;
import semana06progra02.vista.VentanaPrincipal;

/**
 * @author marlo
 */
public class Semana06progra02 {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((hilo, ex) -> {JOptionPane.showMessageDialog(null, "Ocurrio un error inesperado: " + ex,"Error", JOptionPane.ERROR_MESSAGE);});
        SwingUtilities.invokeLater(() -> new Controlador(new VentanaPrincipal()));
    }
}
