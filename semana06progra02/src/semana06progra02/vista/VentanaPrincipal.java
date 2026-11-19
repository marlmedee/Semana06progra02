package semana06progra02.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import semana06progra02.modelo.Nodo;

public class VentanaPrincipal extends JFrame {

    public JTextField txtRuta = new JTextField(35);
    public JButton btnExaminar = new JButton("Examinar...");
    public JTextField txtProfundidad = new JTextField("0", 3);
    public JButton btnAnalizar = new JButton("Analizar");
    public JTextField txtBuscar = new JTextField(12);
    public JTextField txtExtension = new JTextField(5);
    public JTextField txtMinKb = new JTextField(5);
    public JTextField txtMaxKb = new JTextField(5);
    public JTextField txtDesde = new JTextField(8);
    public JTextField txtHasta = new JTextField(8);
    public JButton btnBuscar = new JButton("Buscar");
    public JButton btnNuevaCarpeta = new JButton("Nueva carpeta");
    public JButton btnNuevoArchivo = new JButton("Nuevo archivo");
    public JButton btnRenombrar = new JButton("Renombrar");
    public JButton btnCopiar = new JButton("Copiar");
    public JButton btnMover = new JButton("Mover");
    public JButton btnEliminar = new JButton("Eliminar");
    public JButton btnAbrirEditor = new JButton("Abrir en editor");
    public JButton btnExportar = new JButton("Exportar reporte");
    public JTree arbol = new JTree(new DefaultMutableTreeNode("(sin cargar)"));
    public JTabbedPane pestanas = new JTabbedPane();
    public JTextArea txtEstadisticas = new JTextArea();
    public DefaultTableModel modeloTabla;
    public JTable tablaResultados;
    public JTextArea txtDuplicados = new JTextArea();
    public JTextArea txtEditor = new JTextArea();
    public JLabel lblArchivoEditor = new JLabel("Ningun archivo abierto");
    public JButton btnGuardar = new JButton("Guardar");
    public JTextArea txtBitacora = new JTextArea();
    public JButton btnActualizarBitacora = new JButton("Actualizar");

    public JProgressBar barra = new JProgressBar(0, 100);
    public JLabel lblEstado = new JLabel("Listo");
    public JButton btnCancelar = new JButton("Cancelar");
    public static final int TAB_ESTADISTICAS = 0;
    public static final int TAB_BUSQUEDA = 1;
    public static final int TAB_DUPLICADOS = 2;
    public static final int TAB_EDITOR = 3;
    public static final int TAB_BITACORA = 4;

    public VentanaPrincipal() {
        setTitle("Explorador de archivos");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        add(crearPanelArriba(), BorderLayout.NORTH);
        add(crearPanelCentro(), BorderLayout.CENTER);
        add(crearPanelAbajo(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelArriba() {
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.add(new JLabel("Directorio raiz:"));
        fila1.add(txtRuta);
        fila1.add(btnExaminar);
        fila1.add(new JLabel("Profundidad (0 = ilimitada):"));
        fila1.add(txtProfundidad);
        fila1.add(btnAnalizar);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila2.add(new JLabel("Buscar:"));
        fila2.add(txtBuscar);
        fila2.add(new JLabel("Ext:"));
        fila2.add(txtExtension);
        fila2.add(new JLabel("Min KB:"));
        fila2.add(txtMinKb);
        fila2.add(new JLabel("Max KB:"));
        fila2.add(txtMaxKb);
        fila2.add(new JLabel("Desde (dd/MM/yyyy):"));
        fila2.add(txtDesde);
        fila2.add(new JLabel("Hasta:"));
        fila2.add(txtHasta);
        fila2.add(btnBuscar);

        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila3.add(btnNuevaCarpeta);
        fila3.add(btnNuevoArchivo);
        fila3.add(btnRenombrar);
        fila3.add(btnCopiar);
        fila3.add(btnMover);
        fila3.add(btnEliminar);
        fila3.add(btnAbrirEditor);
        fila3.add(btnExportar);

        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.add(fila1);
        panel.add(fila2);
        panel.add(fila3);
        return panel;
    }

    private JSplitPane crearPanelCentro() {
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        txtEstadisticas.setEditable(false);
        txtEstadisticas.setFont(mono);
        txtDuplicados.setEditable(false);
        txtBitacora.setEditable(false);
        txtEditor.setFont(mono);

        modeloTabla = new DefaultTableModel(
                new Object[]{"Nombre", "Ruta absoluta", "Tamaño (KB)", "Fecha modificacion"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 2) {
                    return Long.class;
                }
                return String.class;
            }
        };
        tablaResultados = new JTable(modeloTabla);
        tablaResultados.setAutoCreateRowSorter(true);

        JPanel panelEditor = new JPanel(new BorderLayout());
        JPanel arribaEditor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arribaEditor.add(btnGuardar);
        arribaEditor.add(lblArchivoEditor);
        panelEditor.add(arribaEditor, BorderLayout.NORTH);
        panelEditor.add(new JScrollPane(txtEditor), BorderLayout.CENTER);
        JPanel panelBitacora = new JPanel(new BorderLayout());
        JPanel arribaBitacora = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arribaBitacora.add(btnActualizarBitacora);
        panelBitacora.add(arribaBitacora, BorderLayout.NORTH);
        panelBitacora.add(new JScrollPane(txtBitacora), BorderLayout.CENTER);
        pestanas.addTab("Estadisticas", new JScrollPane(txtEstadisticas));
        pestanas.addTab("Busqueda", new JScrollPane(tablaResultados));
        pestanas.addTab("Duplicados", new JScrollPane(txtDuplicados));
        pestanas.addTab("Editor", panelEditor);
        pestanas.addTab("Bitacora", panelBitacora);
        arbol.setCellRenderer(new RenderArbol());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(arbol), pestanas);
        split.setDividerLocation(330);
        return split;
    }

    private JPanel crearPanelAbajo() {
        JPanel panel = new JPanel(new BorderLayout());
        barra.setStringPainted(true);
        btnCancelar.setEnabled(false);
        panel.add(lblEstado, BorderLayout.CENTER);
        JPanel derecha = new JPanel();
        derecha.add(barra);
        derecha.add(btnCancelar);
        panel.add(derecha, BorderLayout.EAST);
        return panel;
    }
    public void mostrarArbol(Nodo raiz) {
        DefaultMutableTreeNode nodoRaiz = convertir(raiz);
        arbol.setModel(new DefaultTreeModel(nodoRaiz));
    }

    private DefaultMutableTreeNode convertir(Nodo nodo) {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode(nodo);
        for (Nodo hijo : nodo.getHijos()) {
            n.add(convertir(hijo));
        }
        return n;
    }
    private static class RenderArbol extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object dato = ((DefaultMutableTreeNode) value).getUserObject();
            if (dato instanceof Nodo) {
                if (((Nodo) dato).esCarpeta()) {
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                } else {
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
            }
            return this;
        }
    }
}
