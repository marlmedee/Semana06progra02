package semana06progra02.controlador;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import semana06progra02.modelo.Bitacora;
import semana06progra02.modelo.Buscador;
import semana06progra02.modelo.Configuracion;
import semana06progra02.modelo.DatosInvalidosException;
import semana06progra02.modelo.Duplicados;
import semana06progra02.modelo.Estadisticas;
import semana06progra02.modelo.ExploradorArbol;
import semana06progra02.modelo.Filtros;
import semana06progra02.modelo.GestorArchivos;
import semana06progra02.modelo.Nodo;
import semana06progra02.modelo.ProgresoListener;
import semana06progra02.modelo.Recorredor;
import semana06progra02.modelo.Reporte;
import semana06progra02.modelo.RutaInvalidaException;
import semana06progra02.modelo.Utilidades;
import semana06progra02.vista.VentanaPrincipal;

public class Controlador {

    private VentanaPrincipal vista;
    private Bitacora bitacora = new Bitacora("bitacora.log");
    private Configuracion config = new Configuracion("config.txt");
    private GestorArchivos gestor = new GestorArchivos(bitacora);
    private Recorredor recorredorActual; 
    private Estadisticas ultimasEstadisticas;
    private File raizActual;
    private File archivoEditor;

    public Controlador(VentanaPrincipal vista) {
        this.vista = vista;
        cargarConfiguracion();
        agregarEventos();
        mostrarBitacora();
        vista.setVisible(true);
    }

    private void agregarEventos() {
        vista.btnExaminar.addActionListener(e -> elegirCarpeta());
        vista.btnAnalizar.addActionListener(e -> analizar(true));
        vista.btnBuscar.addActionListener(e -> buscar());
        vista.btnCancelar.addActionListener(e -> cancelar());
        vista.btnNuevaCarpeta.addActionListener(e -> nuevaCarpeta());
        vista.btnNuevoArchivo.addActionListener(e -> nuevoArchivo());
        vista.btnRenombrar.addActionListener(e -> renombrar());
        vista.btnCopiar.addActionListener(e -> copiarOMover(false));
        vista.btnMover.addActionListener(e -> copiarOMover(true));
        vista.btnEliminar.addActionListener(e -> eliminar());
        vista.btnAbrirEditor.addActionListener(e -> abrirEnEditor(elementoSeleccionado()));
        vista.btnGuardar.addActionListener(e -> guardarEditor());
        vista.btnExportar.addActionListener(e -> exportarReporte());
        vista.btnActualizarBitacora.addActionListener(e -> mostrarBitacora());
        vista.tablaResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirEnEditor(elementoSeleccionado());
                }
            }
        });

        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarConfiguracion();
                vista.dispose();
                System.exit(0);
            }
        });
    }
    private void cargarConfiguracion() {
        try {
            config.cargar();
            vista.txtRuta.setText(config.get("ruta"));
            vista.txtBuscar.setText(config.get("texto"));
            vista.txtExtension.setText(config.get("extension"));
            vista.txtMinKb.setText(config.get("min"));
            vista.txtMaxKb.setText(config.get("max"));
            vista.txtDesde.setText(config.get("desde"));
            vista.txtHasta.setText(config.get("hasta"));
            if (!config.get("profundidad").isEmpty()) {
                vista.txtProfundidad.setText(config.get("profundidad"));
            }
        } catch (IOException e) {
            error("No se pudo leer config.txt: " + e.getMessage());
        }
    }

    private void guardarConfiguracion() {
        config.put("ruta", vista.txtRuta.getText());
        config.put("texto", vista.txtBuscar.getText());
        config.put("extension", vista.txtExtension.getText());
        config.put("min", vista.txtMinKb.getText());
        config.put("max", vista.txtMaxKb.getText());
        config.put("desde", vista.txtDesde.getText());
        config.put("hasta", vista.txtHasta.getText());
        config.put("profundidad", vista.txtProfundidad.getText());
        try {
            config.guardar();
        } catch (IOException e) {
            error("No se pudo guardar config.txt: " + e.getMessage());
        }
    }

    private void elegirCarpeta() {
        JFileChooser fc = new JFileChooser(vista.txtRuta.getText());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(vista) == JFileChooser.APPROVE_OPTION) {
            vista.txtRuta.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private int leerProfundidad() throws DatosInvalidosException {
        String texto = vista.txtProfundidad.getText().trim();
        if (texto.isEmpty()) {
            return 0;
        }
        try {
            int p = Integer.parseInt(texto);
            if (p < 0) {
                throw new DatosInvalidosException("La profundidad no puede ser negativa");
            }
            return p;
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("La profundidad debe ser un numero entero");
        }
    }

    private void analizar(boolean completo) {
        final File raiz;
        final int profundidad;
        try {
            raiz = Recorredor.validarRuta(vista.txtRuta.getText());
            profundidad = leerProfundidad();
        } catch (RutaInvalidaException | DatosInvalidosException e) {
            error(e.getMessage());
            return;
        }
        raizActual = raiz;
        if (completo) {
            guardarConfiguracion();
            bitacora.registrar("ANALIZAR", raiz.getAbsolutePath());
        }
        final int fases = completo ? 3 : 1;
        trabajando(true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            Nodo nodo;
            Estadisticas est;
            Map<String, List<File>> dups;

            @Override
            protected Void doInBackground() {
                ExploradorArbol exp = new ExploradorArbol(bitacora, profundidad);
                exp.setListener(crearListener(0, fases));
                recorredorActual = exp;
                nodo = exp.construir(raiz);
                if (!completo || exp.isCancelado()) {
                    return null;
                }
                est = new Estadisticas(bitacora, profundidad);
                est.setListener(crearListener(1, fases));
                recorredorActual = est;
                est.calcular(raiz);
                if (est.isCancelado()) {
                    return null;
                }
                Duplicados d = new Duplicados(bitacora, profundidad);
                d.setListener(crearListener(2, fases));
                recorredorActual = d;
                dups = d.buscar(raiz);
                return null;
            }

            @Override
            protected void done() {
                trabajando(false);
                try {
                    get(); 
                } catch (Exception e) {
                    error("Error durante el analisis: " + e.getMessage());
                    return;
                }
                if (recorredorActual.isCancelado()) {
                    vista.lblEstado.setText("Analisis cancelado.");
                    bitacora.registrar("CANCELAR ANALISIS", raiz.getAbsolutePath());
                    return;
                }
                vista.mostrarArbol(nodo);
                if(completo){
                    ultimasEstadisticas = est;
                    vista.txtEstadisticas.setText(est.generarTexto());
                    vista.txtDuplicados.setText(Duplicados.aTexto(dups));
                    vista.pestanas.setSelectedIndex(VentanaPrincipal.TAB_ESTADISTICAS);
                    vista.lblEstado.setText("Analisis terminado: " + est.getTotalArchivos() + " archivos.");
                }
                else{
                    vista.lblEstado.setText("Arbol actualizado.");
                }
                mostrarBitacora();
            }
        };
        worker.execute();
    }

    private ProgresoListener crearListener(final int fase, final int totalFases) {
        return new ProgresoListener() {
            @Override
            public void actualizar(int porcentaje, String rutaActual) {
                final int valor = (fase * 100 + porcentaje) / totalFases;
                SwingUtilities.invokeLater(() -> {
                    vista.barra.setValue(valor);
                    vista.lblEstado.setText("Revisando: " + rutaActual);
                });
            }
        };
    }

    private void cancelar() {
        if (recorredorActual != null) {
            recorredorActual.cancelar();
            vista.lblEstado.setText("Cancelando...");
        }
    }
    private void trabajando(boolean si) {
        vista.btnAnalizar.setEnabled(!si);
        vista.btnBuscar.setEnabled(!si);
        vista.btnCancelar.setEnabled(si);
        vista.barra.setValue(0);
        if (!si) {
            vista.barra.setValue(100);
        }
    }

    private void buscar() {
        final File raiz;
        final int profundidad;
        final Filtros filtros;
        try {
            raiz = Recorredor.validarRuta(vista.txtRuta.getText());
            profundidad = leerProfundidad();
            filtros = Filtros.crear(vista.txtBuscar.getText(), vista.txtExtension.getText(),
                    vista.txtMinKb.getText(), vista.txtMaxKb.getText(),
                    vista.txtDesde.getText(), vista.txtHasta.getText());
        } catch (RutaInvalidaException | DatosInvalidosException e) {
            error(e.getMessage());
            return;
        }
        guardarConfiguracion();
        bitacora.registrar("BUSCAR \"" + vista.txtBuscar.getText().trim() + "\"", raiz.getAbsolutePath());
        trabajando(true);

        final Buscador buscador = new Buscador(bitacora, profundidad);
        buscador.setListener(crearListener(0, 1));
        recorredorActual = buscador;

        SwingWorker<ArrayList<File>, Void> worker = new SwingWorker<ArrayList<File>, Void>() {
            @Override
            protected ArrayList<File> doInBackground() {
                return buscador.buscar(raiz, filtros);
            }

            @Override
            protected void done() {
                trabajando(false);
                ArrayList<File> lista;
                try {
                    lista = get();
                } catch (Exception e) {
                    error("Error en la busqueda: " + e.getMessage());
                    return;
                }
                if (buscador.isCancelado()) {
                    vista.lblEstado.setText("Busqueda cancelada (resultados parciales)");
                }
                llenarTabla(lista);
                vista.pestanas.setSelectedIndex(VentanaPrincipal.TAB_BUSQUEDA);
                if (lista.isEmpty()) {
                    JOptionPane.showMessageDialog(vista,
                            "No se encontraron archivos que coincidan con los criterios ");
                } else if (!buscador.isCancelado()) {
                    vista.lblEstado.setText("Se encontraron "+lista.size()+" archivos ");
                }
                mostrarBitacora();
            }
        };
        worker.execute();
    }

    private void llenarTabla(ArrayList<File> lista) {
        vista.modeloTabla.setRowCount(0);
        for (File f:lista) {
            vista.modeloTabla.addRow(new Object[]{
                f.getName(),
                f.getAbsolutePath(),
                f.length() / 1024,
                Utilidades.formatearFecha(f.lastModified())
            });
        }
    }

    private File elementoSeleccionado() {
        if (vista.pestanas.getSelectedIndex() == VentanaPrincipal.TAB_BUSQUEDA) {
            int fila = vista.tablaResultados.getSelectedRow();
            if (fila != -1) {
                int filaModelo = vista.tablaResultados.convertRowIndexToModel(fila);
                return new File((String) vista.modeloTabla.getValueAt(filaModelo, 1));
            }
        }
        Object seleccion = vista.arbol.getLastSelectedPathComponent();
        if (seleccion instanceof DefaultMutableTreeNode) {
            Object dato = ((DefaultMutableTreeNode) seleccion).getUserObject();
            if (dato instanceof Nodo) {
                return ((Nodo) dato).getArchivo();
            }
        }
        return null;
    }
    private File carpetaDestino() {
        File sel = elementoSeleccionado();
        if (sel != null) {
            if (sel.isDirectory()) {
                return sel;
            }
            return sel.getParentFile();
        }
        try {
            return Recorredor.validarRuta(vista.txtRuta.getText());
        } catch (RutaInvalidaException e) {
            error(e.getMessage());
            return null;
        }
    }

    private void nuevaCarpeta() {
        File padre = carpetaDestino();
        if (padre == null) {
            return;
        }
        String nombre = JOptionPane.showInputDialog(vista,
                "Nombre de la carpeta en " + padre.getAbsolutePath()
                + "\n(para carpetas anidadas use /, ejemplo: a/b/c)");
        if (nombre == null) {
            return; // cancelo
        }
        try {
            gestor.crearCarpeta(padre, nombre);
            mensaje("Carpeta creada!");
            analizar(false);
        } catch (DatosInvalidosException | IOException e) {
            error(e.getMessage());
        }
    }

    private void nuevoArchivo() {
        File carpeta = carpetaDestino();
        if (carpeta == null) {
            return;
        }
        String nombre = JOptionPane.showInputDialog(vista, "Nombre del archivo (ej: notas.txt):");
        if (nombre == null) {
            return;
        }
        File posible = new File(carpeta, nombre.trim());
        if (posible.exists() && !confirmar("El archivo ya existe, quieres sobrescribirlo?")) {
            return;
        }
        JTextArea area = new JTextArea(10, 40);
        int r = JOptionPane.showConfirmDialog(vista, new JScrollPane(area), "Contenido del archivo",
                JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            gestor.crearArchivo(carpeta, nombre.trim(), area.getText());
            mensaje("Archivo creado ");
            analizar(false);
        } catch (DatosInvalidosException | IOException e) {
            error(e.getMessage());
        }
    }

    private void renombrar() {
        File sel = elementoSeleccionado();
        if (sel == null) {
            error("Seleccione un archivo o carpeta.");
            return;
        }
        String nuevo = JOptionPane.showInputDialog(vista, "Nuevo nombre:", sel.getName());
        if (nuevo == null) {
            return;
        }
        try {
            gestor.renombrar(sel, nuevo.trim());
            mensaje("Renombrado correctamente ");
            analizar(false);
        } catch (DatosInvalidosException | IOException e) {
            error(e.getMessage());
        }
    }

    private void copiarOMover(boolean mover) {
        File sel = elementoSeleccionado();
        if (sel == null || sel.isDirectory()) {
            error("Seleccione un archivo (no carpeta) ");
            return;
        }
        JFileChooser fc = new JFileChooser(sel.getParentFile());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle(mover ? "Mover a..." : "Copiar a...");
        if(fc.showOpenDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = fc.getSelectedFile();
        if(new File(destino, sel.getName()).exists()
                && !confirmar("Ya existe un archivo con ese nombre en el destino, quieres sobrescribir?")) {
            return;
        }
        try {
            if (mover) {
                gestor.mover(sel, destino);
                mensaje("archivo movido ");
            }
            else{
                gestor.copiar(sel, destino);
                mensaje("archivo copiado ");
            }
            analizar(false);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void eliminar() {
        File sel = elementoSeleccionado();
        if (sel == null) {
            error("seleccione un archivo o carpeta");
            return;
        }
        String pregunta = "estas seguro que desea eliminar " + sel.getAbsolutePath() + "?";
        if (sel.isDirectory()) {
            pregunta += "\nse borrara la carpeta con todo su contenido ";
        }
        if (!confirmar(pregunta)) {
            return;
        }
        try {
            gestor.eliminar(sel);
            if (sel.equals(archivoEditor)) {
                archivoEditor = null;
                vista.txtEditor.setText("");
                vista.lblArchivoEditor.setText("Ningun archivo abierto");
            }
            mensaje("Eliminado ");
            analizar(false);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void abrirEnEditor(File archivo) {
        if (archivo == null || archivo.isDirectory()) {
            error("Seleccione un archivo de texto ");
            return;
        }
        if (!gestor.esTexto(archivo)) {
            JOptionPane.showMessageDialog(vista, "el archivo no parece ser de texto, no se puede abrir ",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (archivo.length() > GestorArchivos.MAX_EDITOR) {
            JOptionPane.showMessageDialog(vista, "El archivo es demasiado grande ("
                    + Utilidades.formatearTamano(archivo.length()) + "). Maximo 1 MB",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            vista.txtEditor.setText(gestor.leerTexto(archivo));
            vista.txtEditor.setCaretPosition(0);
            archivoEditor = archivo;
            vista.lblArchivoEditor.setText(archivo.getAbsolutePath());
            vista.pestanas.setSelectedIndex(VentanaPrincipal.TAB_EDITOR);
        } catch (IOException e) {
            error("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    private void guardarEditor() {
        if (archivoEditor == null) {
            error("No hay ningun archivo abierto");
            return;
        }
        if (!confirmar("Se va a sobrescribir " + archivoEditor.getName() + ", continuar?")) {
            return;
        }
        try {
            gestor.guardarTexto(archivoEditor, vista.txtEditor.getText());
            mensaje("Guardado");
        } catch (IOException e) {
            error("no se pudo guardar: " + e.getMessage());
        }
    }

    private void exportarReporte() {
        if (ultimasEstadisticas == null) {
            error("Primero analice un directorio ");
            return;
        }
        String nombre = "reporte_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(nombre));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Texto (.txt)", "txt"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("CSV (.csv)", "csv"));
        if (fc.showSaveDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = fc.getSelectedFile();
        String n = destino.getName().toLowerCase();
        if (!n.endsWith(".txt") && !n.endsWith(".csv")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".txt");
        }
        if (destino.exists() && !confirmar("El archivo ya existe, sobrescribir?")) {
            return;
        }
        try {
            Reporte.exportar(ultimasEstadisticas, destino);
            bitacora.registrar("EXPORTAR REPORTE", destino.getAbsolutePath());
            mensaje("Reporte guardado en " + destino.getAbsolutePath());
        } catch (IOException e) {
            error("No se pudo exportar: " + e.getMessage());
        }
    }

    private void mostrarBitacora() {
        try {
            vista.txtBitacora.setText(bitacora.leer());
        } catch (IOException e) {
            vista.txtBitacora.setText("no se pudo leer la bitacora: " + e.getMessage());
        }
    }

    private void error(String texto) {
        JOptionPane.showMessageDialog(vista, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mensaje(String texto) {
        JOptionPane.showMessageDialog(vista, texto);
        mostrarBitacora();
    }

    private boolean confirmar(String pregunta) {
        int r = JOptionPane.showConfirmDialog(vista, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION);
        return r == JOptionPane.YES_OPTION;
    }
}
