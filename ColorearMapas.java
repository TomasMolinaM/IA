import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ColorearMapas extends JFrame {

    private int numRegiones;
    private int numColores;
    private int[][] matrizAdyacencia;
    private Color[] paletaColores;
    
    private int[] gridX;
    private int[] gridY;
    private Polygon[] poligonosMapa;
    private boolean mapaGenerado = false;

    private JTextField txtRegiones;
    private JTextField txtColores;
    private JButton btnGenerarMapa;
    private JTabbedPane pestanas;
    
    private PanelAlgoritmo panelDFS;
    private PanelAlgoritmo panelColinas;

    public ColorearMapas() {
        setTitle("Sistemas de Búsqueda - Coloreo de Mapas (Optimizados)");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelSuperior.setBackground(new Color(230, 240, 250));

        panelSuperior.add(new JLabel("Número de Regiones (16-30):"));
        txtRegiones = new JTextField("20", 5);
        panelSuperior.add(txtRegiones);

        panelSuperior.add(new JLabel("Número de Colores (2-10):"));
        txtColores = new JTextField("4", 5);
        panelSuperior.add(txtColores);

        btnGenerarMapa = new JButton("Generar Topología del Mapa");
        btnGenerarMapa.setFont(new Font("Arial", Font.BOLD, 14));
        btnGenerarMapa.setBackground(new Color(150, 200, 150));
        btnGenerarMapa.addActionListener(e -> inicializarMapaGlobal());
        panelSuperior.add(btnGenerarMapa);

        add(panelSuperior, BorderLayout.NORTH);

        pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Arial", Font.BOLD, 14));
        
        panelDFS = new PanelAlgoritmo("Búsqueda Primero en Profundidad (DFS)");
        panelColinas = new PanelAlgoritmo("Ascensión de Colinas (Local Search)");

        pestanas.addTab("Primero en Profundidad", panelDFS);
        pestanas.addTab("Ascensión de Colinas", panelColinas);
        
        add(pestanas, BorderLayout.CENTER);
        setVisible(true);
    }

    private void inicializarMapaGlobal() {
        try {
            numRegiones = Integer.parseInt(txtRegiones.getText().trim());
            numColores = Integer.parseInt(txtColores.getText().trim());

            if (numRegiones < 16 || numRegiones > 30) {
                JOptionPane.showMessageDialog(this, "Las regiones deben estar entre 16 y 30.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (numColores < 2 || numColores > 10) {
                JOptionPane.showMessageDialog(this, "La cantidad de colores debe estar entre 2 y 10.", "Error de Restricción", JOptionPane.ERROR_MESSAGE);
                return;
            }

            generarMapaOrganico();
            definirPaleta();
            mapaGenerado = true;
            
            panelDFS.prepararNuevoJuego();
            panelColinas.prepararNuevoJuego();
            
            JOptionPane.showMessageDialog(this, "Mapa topológico generado con " + numRegiones + " regiones.", "Mapa Listo", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingresa números válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class EstadoAlgoritmo {
        int[] coloresActuales;
        String descripcion;
        List<int[]> alternativasEvaluadas;
        int[] decisionTomada;

        EstadoAlgoritmo(int[] colores, String desc) {
            this.coloresActuales = colores.clone();
            this.descripcion = desc;
            this.alternativasEvaluadas = new ArrayList<>();
        }
    }

    class PanelAlgoritmo extends JPanel {
        private String nombreAlgoritmo;
        private int[] coloresAsignados;
        private JTextArea areaConsola;
        private JButton btnEjecutar;
        private JButton btnVerArbol;
        private LienzoMapa lienzo;
        
        private List<EstadoAlgoritmo> historialArbol;
        private SwingWorker<Boolean, String> workerAlgoritmo;

        public PanelAlgoritmo(String nombre) {
            this.nombreAlgoritmo = nombre;
            this.historialArbol = new ArrayList<>();
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            lienzo = new LienzoMapa(this);
            lienzo.setPreferredSize(new Dimension(650, 600));

            areaConsola = new JTextArea();
            areaConsola.setEditable(false);
            areaConsola.setBackground(new Color(30, 30, 30));
            areaConsola.setForeground(new Color(0, 255, 0));
            areaConsola.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollConsola = new JScrollPane(areaConsola);
            TitledBorder bordeConsola = BorderFactory.createTitledBorder("Consola Extendida: " + nombreAlgoritmo);
            bordeConsola.setTitleColor(Color.BLUE);
            scrollConsola.setBorder(bordeConsola);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lienzo, scrollConsola);
            splitPane.setDividerLocation(650);
            splitPane.setResizeWeight(0.6);

            add(splitPane, BorderLayout.CENTER);

            JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnEjecutar = new JButton("Ejecutar Algoritmo");
            btnEjecutar.setEnabled(false);
            btnEjecutar.addActionListener(e -> ejecutar());
            
            btnVerArbol = new JButton("Ver Árbol de Decisiones / Estados");
            btnVerArbol.setEnabled(false);
            btnVerArbol.addActionListener(e -> mostrarVentanaArbol());
            
            panelInferior.add(btnEjecutar);
            panelInferior.add(btnVerArbol);
            add(panelInferior, BorderLayout.SOUTH);
        }

        public void prepararNuevoJuego() {
            if (!mapaGenerado) return;
            coloresAsignados = new int[numRegiones];
            historialArbol.clear();
            areaConsola.setText("Mapa listo. Presiona 'Ejecutar Algoritmo'.\n");
            btnEjecutar.setEnabled(true);
            btnVerArbol.setEnabled(false);
            lienzo.repaint();
        }

        private void log(String mensaje) {
            areaConsola.append(mensaje + "\n");
            areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
        }

        public int[] getColores() {
            return coloresAsignados;
        }

        private void ejecutar() {
            btnEjecutar.setEnabled(false);
            btnVerArbol.setEnabled(false);
            historialArbol.clear();
            coloresAsignados = new int[numRegiones]; 
            areaConsola.setText("=================================================\n");
            log("Iniciando " + nombreAlgoritmo);
            log("=================================================\n");

            workerAlgoritmo = new SwingWorker<Boolean, String>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (nombreAlgoritmo.contains("DFS")) {
                        return ejecutarDFS(0);
                    } else {
                        return ejecutarColinas();
                    }
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String msg : chunks) log(msg);
                    lienzo.repaint();
                }

                @Override
                protected void done() {
                    btnEjecutar.setEnabled(true);
                    btnVerArbol.setEnabled(true);
                    try {
                        if (get()) {
                            log("\n[!] Algoritmo Finalizado con Éxito.");
                        } else {
                            log("\n[!] Algoritmo Terminó sin Solución o se agoto el Árbol.");
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }

                private boolean ejecutarDFS(int regionActual) throws InterruptedException {
                    int coloresUsadosHastaAhora = contarColoresUnicos(coloresAsignados);
                    int regionesFaltantes = numRegiones - regionActual;
                    int coloresQueFaltanUsar = numColores - coloresUsadosHastaAhora;
                    
                    // Poda Temprana
                    if (coloresQueFaltanUsar > regionesFaltantes) {
                        return false; 
                    }

                    // NODO HOJA: Condición de Parada y Validación Final
                    if (regionActual == numRegiones) {
                        if (coloresUsadosHastaAhora == numColores) {
                            // CORRECCIÓN: Guardar el estado de éxito final en el visualizador
                            EstadoAlgoritmo exito = new EstadoAlgoritmo(coloresAsignados, "Solución Válida Encontrada! (Se usó todos los colores)");
                            exito.decisionTomada = coloresAsignados.clone();
                            historialArbol.add(exito);
                            return true; 
                        } else {
                            publish("  [!] Mapa lleno pero usa " + coloresUsadosHastaAhora + " de " + numColores + " colores. Forzando Backtracking...");
                            
                            // CORRECCIÓN: Guardar el estado de rechazo en el visualizador para ver el salto
                            EstadoAlgoritmo rechazo = new EstadoAlgoritmo(coloresAsignados, "Rechazado: Mapa lleno pero faltan colores (" + coloresUsadosHastaAhora + "/" + numColores + "). Forzando Backtrack.");
                            historialArbol.add(rechazo);
                            
                            Thread.sleep(100);
                            return false; 
                        }
                    }

                    EstadoAlgoritmo estado = new EstadoAlgoritmo(coloresAsignados, "Evaluando Región " + (regionActual + 1));
                    
                    for (int c = 1; c <= numColores; c++) {
                        publish("DFS -> Probando Color " + c + " en Región " + (regionActual + 1));
                        Thread.sleep(2); 
                        
                        int[] alternativa = coloresAsignados.clone();
                        alternativa[regionActual] = c;
                        estado.alternativasEvaluadas.add(alternativa);

                        if (esSeguro(regionActual, c, coloresAsignados)) {
                            coloresAsignados[regionActual] = c;
                            estado.decisionTomada = coloresAsignados.clone();
                            historialArbol.add(estado);
                            
                            if (ejecutarDFS(regionActual + 1)) return true;

                            coloresAsignados[regionActual] = 0;
                            publish("  [X] Retroceso (Backtrack) en Región " + (regionActual + 1));
                            estado = new EstadoAlgoritmo(coloresAsignados, "Backtracking desde Región " + (regionActual + 1));
                        }
                    }
                    if(estado.decisionTomada == null) historialArbol.add(estado);
                    return false;
                }

                private boolean ejecutarColinas() throws InterruptedException {
                    Random r = new Random();
                    for (int i = 0; i < numRegiones; i++) coloresAsignados[i] = r.nextInt(numColores) + 1;
                    
                    int conflictosActuales = contarConflictos(coloresAsignados);
                    publish("Colinas -> Estado Inicial Aleatorio Generado");
                    publish("Conflictos iniciales detectados: " + conflictosActuales);
                    publish("--------------------------------------------------");
                    
                    int iteracion = 1;
                    int maxIteraciones = 300; 

                    while (conflictosActuales > 0 && iteracion <= maxIteraciones) {
                        EstadoAlgoritmo estado = new EstadoAlgoritmo(coloresAsignados, "Iteración " + iteracion + " | Conflictos: " + conflictosActuales);
                        
                        int mejorConflicto = conflictosActuales;
                        int[] mejorVecino = null;
                        int regionMejorada = -1;
                        int colorCambiado = -1;

                        publish("\n>>> Iteración " + iteracion + " <<<");
                        publish("Buscando vecinos cambiando colores conflictivos...");
                        Thread.sleep(100);

                        for (int i = 0; i < numRegiones; i++) {
                            if (tieneConflicto(i, coloresAsignados)) {
                                publish("  -> Analizando Región " + (i+1) + " (En Conflicto)");
                                
                                for (int c = 1; c <= numColores; c++) {
                                    if (c != coloresAsignados[i]) {
                                        int[] vecino = coloresAsignados.clone();
                                        vecino[i] = c;
                                        estado.alternativasEvaluadas.add(vecino);
                                        
                                        int confVecino = contarConflictos(vecino);
                                        publish("     - Si cambiamos a Color " + c + ", los conflictos totales serían: " + confVecino);
                                        
                                        if (confVecino < mejorConflicto) {
                                            mejorConflicto = confVecino;
                                            mejorVecino = vecino;
                                            regionMejorada = i;
                                            colorCambiado = c;
                                            publish("       [!] ¡Encontramos una mejoría potencial!");
                                        }
                                    }
                                }
                            }
                        }

                        if (mejorVecino == null) {
                            publish("\n[Resultado de Iteración]");
                            publish("-> Máximo Local Alcanzado. Ningún cambio de 1 solo color mejora el estado actual.");
                            estado.descripcion += " (Atascado en Máximo Local)";
                            historialArbol.add(estado);
                            return false; 
                        }

                        coloresAsignados = mejorVecino;
                        conflictosActuales = mejorConflicto;
                        estado.decisionTomada = coloresAsignados.clone();
                        historialArbol.add(estado);
                        
                        publish("\n[Resultado de Iteración]");
                        publish("-> Salto Aplicado: Se cambió la Región " + (regionMejorada+1) + " al Color " + colorCambiado);
                        publish("-> Nuevo total de conflictos en el mapa: " + conflictosActuales);
                        iteracion++;
                    }
                    return conflictosActuales == 0;
                }
            };
            workerAlgoritmo.execute();
        }

        private void mostrarVentanaArbol() {
            JDialog ventana = new JDialog((Frame)null, "Visualizador de Estados: " + nombreAlgoritmo, true);
            ventana.setSize(1100, 750);
            
            JPanel contenedor = new JPanel();
            contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
            
            for (int k = 0; k < historialArbol.size(); k++) {
                EstadoAlgoritmo est = historialArbol.get(k);
                
                JPanel filaPaso = new JPanel(new BorderLayout(5, 5));
                
                if (est.descripcion.contains("RECHAZADO")) {
                    filaPaso.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 2), "Paso " + (k+1) + ": " + est.descripcion));
                } else if (est.descripcion.contains("SOLUCIÓN VÁLIDA")) {
                    filaPaso.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 3), "Paso " + (k+1) + ": " + est.descripcion));
                } else {
                    filaPaso.setBorder(BorderFactory.createTitledBorder("Paso " + (k+1) + ": " + est.descripcion));
                }
                
                JPanel panelOpcionesBase = new JPanel(new BorderLayout());
                panelOpcionesBase.add(new JLabel("Alternativas Evaluadas: "), BorderLayout.NORTH);
                
                if(est.alternativasEvaluadas.isEmpty()) {
                    panelOpcionesBase.add(new JLabel("Ninguna viabilidad / Estado Terminal o de Validación"), BorderLayout.CENTER);
                } else {
                    JPanel gridOpciones = new JPanel(new GridLayout(0, 6, 5, 5));
                    for(int a = 0; a < est.alternativasEvaluadas.size(); a++) {
                        gridOpciones.add(crearMiniMapa(est.alternativasEvaluadas.get(a), "Opc " + (a+1)));
                    }
                    panelOpcionesBase.add(gridOpciones, BorderLayout.CENTER);
                }
                
                filaPaso.add(panelOpcionesBase, BorderLayout.CENTER);
                
                if (est.decisionTomada != null) {
                    JPanel panelDecision = new JPanel(new BorderLayout());
                    panelDecision.add(new JLabel("=> ELEGIDO:", SwingConstants.CENTER), BorderLayout.NORTH);
                    panelDecision.add(crearMiniMapa(est.decisionTomada, "Estado Seleccionado"), BorderLayout.CENTER);
                    panelDecision.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                    filaPaso.add(panelDecision, BorderLayout.EAST);
                } else if (est.descripcion.contains("Rechazado")) {
                    JPanel panelDecision = new JPanel(new BorderLayout());
                    panelDecision.add(new JLabel("=> Mapa Fallido:", SwingConstants.CENTER), BorderLayout.NORTH);
                    panelDecision.add(crearMiniMapa(est.coloresActuales, "Faltan Colores"), BorderLayout.CENTER);
                    panelDecision.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    filaPaso.add(panelDecision, BorderLayout.EAST);
                }
                
                contenedor.add(filaPaso);
            }

            JScrollPane scroll = new JScrollPane(contenedor);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            ventana.add(scroll);
            ventana.setLocationRelativeTo(this);
            ventana.setVisible(true);
        }

        private JPanel crearMiniMapa(int[] colores, String titulo) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            
            JPanel miniCanvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.scale(0.15, 0.15); 
                    g2.translate(-150, -150); 
                    
                    for (int i = 0; i < numRegiones; i++) {
                        g2.setColor(colores[i] > 0 ? paletaColores[colores[i]] : Color.WHITE);
                        g2.fillPolygon(poligonosMapa[i]);
                        g2.setColor(Color.DARK_GRAY);
                        g2.drawPolygon(poligonosMapa[i]);
                    }
                }
            };
            miniCanvas.setPreferredSize(new Dimension(80, 80));
            
            p.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
            p.add(miniCanvas, BorderLayout.CENTER);
            return p;
        }
    }

    private boolean esSeguro(int region, int color, int[] colores) {
        for (int i = 0; i < numRegiones; i++) {
            if (matrizAdyacencia[region][i] == 1 && colores[i] == color) return false;
        }
        return true;
    }

    private int contarConflictos(int[] colores) {
        int conflictos = 0;
        for (int i = 0; i < numRegiones; i++) {
            for (int j = i + 1; j < numRegiones; j++) {
                if (matrizAdyacencia[i][j] == 1 && colores[i] != 0 && colores[i] == colores[j]) {
                    conflictos++;
                }
            }
        }
        return conflictos;
    }

    private boolean tieneConflicto(int region, int[] colores) {
        for (int i = 0; i < numRegiones; i++) {
            if (matrizAdyacencia[region][i] == 1 && colores[region] == colores[i]) return true;
        }
        return false;
    }

    private int contarColoresUnicos(int[] colores) {
        boolean[] visto = new boolean[numColores + 1];
        int conteo = 0;
        for (int i = 0; i < numRegiones; i++) {
            if (colores[i] > 0 && !visto[colores[i]]) {
                visto[colores[i]] = true;
                conteo++;
            }
        }
        return conteo;
    }

    private void generarMapaOrganico() {
        matrizAdyacencia = new int[numRegiones][numRegiones];
        gridX = new int[numRegiones];
        gridY = new int[numRegiones];
        int[] grado = new int[numRegiones];

        gridX[0] = 0; gridY[0] = 0;
        Random rand = new Random();
        int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};

        for (int i = 1; i < numRegiones; i++) {
            boolean colocado = false;
            while (!colocado) {
                int padre = rand.nextInt(i);
                if (grado[padre] < 4) {
                    List<Integer> direcciones = new ArrayList<>(List.of(0, 1, 2, 3));
                    Collections.shuffle(direcciones);
                    for (int d : direcciones) {
                        int nx = gridX[padre] + dirs[d][0];
                        int ny = gridY[padre] + dirs[d][1];
                        if (!estaOcupado(nx, ny, i)) {
                            gridX[i] = nx; gridY[i] = ny;
                            matrizAdyacencia[i][padre] = 1; matrizAdyacencia[padre][i] = 1;
                            grado[i]++; grado[padre]++;
                            colocado = true;
                            for (int j = 0; j < i; j++) {
                                if (j != padre && grado[i] < 4 && grado[j] < 4) {
                                    if (Math.abs(gridX[j] - nx) + Math.abs(gridY[j] - ny) == 1) { 
                                        matrizAdyacencia[i][j] = 1; matrizAdyacencia[j][i] = 1;
                                        grado[i]++; grado[j]++;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        poligonosMapa = new Polygon[numRegiones];
        int cellSize = 60;
        int minX = 0, minY = 0;
        for (int i = 0; i < numRegiones; i++) {
            minX = Math.min(minX, gridX[i]); minY = Math.min(minY, gridY[i]);
        }
        for (int i = 0; i < numRegiones; i++) {
            int cx = 250 + (gridX[i] - minX) * cellSize;
            int cy = 250 + (gridY[i] - minY) * cellSize;
            Polygon p = new Polygon();
            for (int a = 0; a < 8; a++) {
                double angulo = a * Math.PI / 4.0;
                double radio = (cellSize / 1.6) * (0.8 + 0.4 * rand.nextDouble());
                p.addPoint(cx + (int) (radio * Math.cos(angulo)), cy + (int) (radio * Math.sin(angulo)));
            }
            poligonosMapa[i] = p;
        }
    }

    private boolean estaOcupado(int x, int y, int count) {
        for (int i = 0; i < count; i++) if (gridX[i] == x && gridY[i] == y) return true;
        return false;
    }

    private void definirPaleta() {
        paletaColores = new Color[numColores + 1];
        paletaColores[0] = Color.WHITE; 
        for (int i = 1; i <= numColores; i++) {
            paletaColores[i] = Color.getHSBColor((float) i / numColores, 0.6f, 0.95f);
        }
    }

    class LienzoMapa extends JPanel {
        private PanelAlgoritmo padre;
        public LienzoMapa(PanelAlgoritmo padre) { this.padre = padre; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!mapaGenerado) return;
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(new Color(245, 245, 240));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int[] colores = padre.getColores();

            g2d.setColor(new Color(150, 150, 150, 100));
            g2d.setStroke(new BasicStroke(3));
            for (int i = 0; i < numRegiones; i++) {
                for (int j = i + 1; j < numRegiones; j++) {
                    if (matrizAdyacencia[i][j] == 1) {
                        Rectangle r1 = poligonosMapa[i].getBounds();
                        Rectangle r2 = poligonosMapa[j].getBounds();
                        g2d.drawLine(r1.x + r1.width/2, r1.y + r1.height/2, r2.x + r2.width/2, r2.y + r2.height/2);
                    }
                }
            }

            for (int i = 0; i < numRegiones; i++) {
                g2d.setColor(colores[i] > 0 ? paletaColores[colores[i]] : Color.WHITE);
                g2d.fillPolygon(poligonosMapa[i]);
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(poligonosMapa[i]);

                Rectangle b = poligonosMapa[i].getBounds();
                String txt = String.valueOf(i + 1);
                
                FontMetrics fm = g2d.getFontMetrics();
                int tx = b.x + b.width/2 - fm.stringWidth(txt)/2;
                int ty = b.y + b.height/2 + fm.getAscent()/2 - 2;
                
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillOval(tx - 4, ty - 13, fm.stringWidth(txt) + 8, 18);
                g2d.setColor(Color.BLACK);
                g2d.drawString(txt, tx, ty);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ColorearMapas());
    }
}