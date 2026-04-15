import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TresEnRaya extends JFrame {

    private JTabbedPane pestanas;
    private PanelJuego panelMinimax;
    private PanelJuego panelPoda;

    public TresEnRaya() {
        setTitle("Tres en Raya IA - Visualizador de Árbol de Decisiones");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel();
        JButton btnReiniciar = new JButton("Volver a Jugar");
        btnReiniciar.setFont(new Font("Arial", Font.BOLD, 16));
        btnReiniciar.setBackground(new Color(255, 204, 153));
        btnReiniciar.addActionListener(e -> reiniciarJuegoActual());
        panelSuperior.add(btnReiniciar);
        add(panelSuperior, BorderLayout.NORTH);

        pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Arial", Font.BOLD, 14));
        panelMinimax = new PanelJuego(false);
        panelPoda = new PanelJuego(true);

        pestanas.addTab("MiniMax Clásico", panelMinimax);
        pestanas.addTab("Poda Alfa-Beta", panelPoda);
        pestanas.addChangeListener(e -> reiniciarJuegoActual());
        add(pestanas, BorderLayout.CENTER);

        setVisible(true);
    }

    private void reiniciarJuegoActual() {
        if (pestanas.getSelectedIndex() == 0) {
            panelMinimax.reiniciar();
        } else {
            panelPoda.reiniciar();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TresEnRaya());
    }

    class PasoArbol {
        char[][] tableroPrevio;
        List<char[][]> alternativasIA;
        int filaElegida, colElegida;
        char[][] movimientoJugador;

        PasoArbol(char[][] previo) {
            this.tableroPrevio = new char[3][3];
            for (int i = 0; i < 3; i++) {
                System.arraycopy(previo[i], 0, this.tableroPrevio[i], 0, 3);
            }
            this.alternativasIA = new ArrayList<>();
        }
    }

    class PanelJuego extends JPanel {
        private char[][] tablero = {{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}};
        private JButton[][] botones = new JButton[3][3];
        private JTextArea areaConsola;
        private boolean usaPoda;
        private boolean juegoTerminado = false;
        private JButton btnVerArbol;       

        private List<Integer> subValoresMinimax = new ArrayList<>();
        private boolean podaOcurrida = false;
        private int alfaCorteRaiz = Integer.MIN_VALUE;
        
        // Historial para el visualizador
        private List<PasoArbol> historialArbol = new ArrayList<>();

        public PanelJuego(boolean usaPoda) {
            this.usaPoda = usaPoda;
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel panelTablero = new JPanel(new GridLayout(3, 3, 5, 5));
            panelTablero.setPreferredSize(new Dimension(350, 350));
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    botones[i][j] = new JButton("");
                    botones[i][j].setFont(new Font("Arial", Font.BOLD, 60));
                    botones[i][j].setFocusPainted(false);
                    botones[i][j].setBackground(Color.WHITE);
                    final int f = i, c = j;
                    botones[i][j].addActionListener(e -> turnoJugador(f, c));
                    panelTablero.add(botones[i][j]);
                }
            }

            JPanel contenedorTablero = new JPanel(new GridBagLayout());
            contenedorTablero.add(panelTablero);

            areaConsola = new JTextArea();
            areaConsola.setEditable(false);
            areaConsola.setBackground(new Color(30, 30, 30));
            areaConsola.setForeground(new Color(0, 255, 0)); 
            areaConsola.setFont(new Font("Monospaced", Font.PLAIN, 13));
            btnVerArbol = new JButton("Ver Árbol de Decisiones");
            btnVerArbol.setEnabled(false);
            btnVerArbol.addActionListener(e -> mostrarVentanaArbol());
            
            JScrollPane scrollConsola = new JScrollPane(areaConsola);
            TitledBorder bordeConsola = BorderFactory.createTitledBorder(usaPoda ? "Consola: Análisis Poda Alfa-Beta" : "Consola: Análisis Minimax");
            bordeConsola.setTitleColor(Color.BLUE);
            scrollConsola.setBorder(bordeConsola);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contenedorTablero, scrollConsola);
            splitPane.setDividerLocation(420); 
            splitPane.setResizeWeight(0.4);

            add(splitPane, BorderLayout.CENTER);
            JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Lo alineamos a la derecha
            panelInferior.add(btnVerArbol);
            add(panelInferior, BorderLayout.SOUTH);

            log("¡Bienvenido! Tú juegas con 'X' (MIN), IA juega con 'O' (MAX).");
            log("Haz clic en cualquier casilla para comenzar.\n"); 
            
        }

        public void reiniciar() {
            juegoTerminado = false;
            historialArbol.clear();
            btnVerArbol.setEnabled(false);
            areaConsola.setText("");
            log("¡Bienvenido! Tú juegas con 'X' (MIN), IA juega con 'O' (MAX).");
            log("Haz clic en cualquier casilla para comenzar.\n"); 
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    tablero[i][j] = ' ';
                    botones[i][j].setText("");
                    botones[i][j].setEnabled(true);
                }
            }
        }

        private void log(String mensaje) {
            areaConsola.append(mensaje + "\n");
            areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
        }

        private String formatoLista(List<Integer> lista) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lista.size(); i++) {
                sb.append(lista.get(i));
                if (i < lista.size() - 1) sb.append(", ");
            }
            return sb.toString();
        }

        private String desgloseFormula(int v) {
            if (v > 0 && v <= 100) {
                int prof = 100 - v;
                return "100 - " + prof + " = " + v;
            } else if (v < 0 && v >= -100) {
                int prof = v + 100;
                return "-100 + " + prof + " = " + v;
            } else {
                return "0 = 0"; // Empate
            }
        }

        private void turnoJugador(int f, int c) {
            if (juegoTerminado || tablero[f][c] != ' ') return;
            actualizarCasilla(f, c, 'X');
            
            PasoArbol nuevoPaso = new PasoArbol(tablero);
            historialArbol.add(nuevoPaso);

            if (!revisarFinal()) {
                SwingUtilities.invokeLater(() -> turnoIA(nuevoPaso));
            }
        }

        private void turnoIA(PasoArbol paso) {
            log("=================================================");
            if (usaPoda) {
                log("Búsqueda Alfa-Beta Iniciada");
                log("\n[Fórmula ALFA-BETA Aplicada]:");
                log("   Alfa (α): Mejor valor asegurado para IA (MAX)");
                log("   Beta (β): Mejor valor asegurado para Jugador (MIN)");
                log(" * Corte Alfa: Si MIN halla un valor <= α, interrumpe.");
                log(" * Corte Beta: Si MAX halla un valor >= β, interrumpe.");
            } else {
                
                log("Búsqueda Minimax Clásica Iniciada");
                log("\n[Fórmula MINIMAX Aplicada]:");
                log(" MINIMAX(s) = ");
                log("   Utilidad(s)  -> Si 's' es un nodo terminal");
                log("   MAX(ramas)   -> Si es el turno de la IA (O)");
                log("   MIN(ramas)   -> Si es el turno del Jugador (X)");
            }
            log("\n[Función De Utilidad en Hojas]:");
            log("   Victoria IA: 100 - profundidad");
            log("   Victoria X: -100 + profundidad");
            log("   Empate:      0");
            log("=================================================");

            int mejorVal = Integer.MIN_VALUE; 
            int filaOptima = -1, colOptima = -1;
            int alfa = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;

            List<Integer> valoresRaiz = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (tablero[i][j] == ' ') {
                        tablero[i][j] = 'O'; 
                        paso.alternativasIA.add(clonarTablero(tablero));
                        subValoresMinimax.clear();
                        podaOcurrida = false;
                        alfaCorteRaiz = Integer.MIN_VALUE;
                        
                        int valorRama;
                        
                        if (usaPoda) {
                            String alfaStr = (alfa == Integer.MIN_VALUE) ? "-INF" : String.valueOf(alfa);
                            String betaStr = (beta == Integer.MAX_VALUE) ? "INF" : String.valueOf(beta);
                            log("\n>>> Explorando Rama: IA juega 'O' en (" + i + "," + j + ") | [α: " + alfaStr + ", β: " + betaStr + "]");
                            valorRama = algoritmoMinimaxPoda(tablero, 1, false, alfa, beta);
                        } else {
                            log("\n>>> Explorando Rama: IA juega 'O' en (" + i + "," + j + ")");
                            valorRama = algoritmoMinimax(tablero, 1, false);
                        }

                        if (subValoresMinimax.isEmpty()) {
                            log("    [!] Nodo Terminal alcanzado de inmediato.");
                            log("    Utilidad de la rama = " + valorRama + "  [Hoja: " + desgloseFormula(valorRama) + "]");
                        } else {
                            int minPosible = Collections.min(subValoresMinimax);
                            int maxPosible = Collections.max(subValoresMinimax);
                            
                            if (usaPoda && podaOcurrida) {
                                log("    Valores evaluados antes del corte: [" + formatoLista(subValoresMinimax) + "]");
                                log("    [¡Corte ALFA Activado!]: El valor " + valorRama + " es <= Alfa (" + alfaCorteRaiz + ").");
                                log("    -> No hace falta explorar más, MIN nunca elegirá una rama que favorezca tanto a MAX.");
                                log("    -> Mínimo garantizado encontrado : " + minPosible + "  [Hoja: " + desgloseFormula(minPosible) + "]");
                                log("    => ALFA-BETA Parcial: MIN(" + formatoLista(subValoresMinimax) + ", ...) = " + valorRama);
                            } else {
                                log("    Valores evaluados del rival: [" + formatoLista(subValoresMinimax) + "]");
                                log("    -> Mínimo posible (Óptima rival) : " + minPosible + "  [Hoja: " + desgloseFormula(minPosible) + "]");
                                log("    -> Máximo posible (Error rival)  : " + maxPosible + "  [Hoja: " + desgloseFormula(maxPosible) + "]");
                                log("    => MINIMAX Resultante de la rama: MIN(" + formatoLista(subValoresMinimax) + ") = " + valorRama);
                            }
                        }

                        valoresRaiz.add(valorRama);
                        tablero[i][j] = ' ';

                        if (valorRama > mejorVal) {
                            mejorVal = valorRama;
                            filaOptima = i;
                            colOptima = j;
                        }
                        
                        if (usaPoda) {
                            alfa = Math.max(alfa, mejorVal);
                        }
                    }
                }
            }

            if (filaOptima != -1) {
                log("\n=================================================");
                log("Resumen Final en Nodo Raíz (IA - MAX):");
                log("Resultados de las ramas exploradas: [" + formatoLista(valoresRaiz) + "]");
                log("=> Fórmula Final: MAX(" + formatoLista(valoresRaiz) + ") = " + mejorVal);
                log("=> Decisión: Casilla (" + filaOptima + "," + colOptima + ")");
                log("=================================================\n");
                
                paso.filaElegida = filaOptima;
                paso.colElegida = colOptima;
                actualizarCasilla(filaOptima, colOptima, 'O');
                revisarFinal();
            }
        }

        private int calcularUtilidad(char[][] t, int prof) {
            if (esGanador(t, 'O')) return 100 - prof;  
            if (esGanador(t, 'X')) return -100 + prof; 
            return 0; 
        }

        private int algoritmoMinimax(char[][] t, int prof, boolean esMax) {
            if (esGanador(t, 'X') || esGanador(t, 'O') || tableroLleno(t)) {
                return calcularUtilidad(t, prof);
            }

            if (esMax) { 
                int maxEval = Integer.MIN_VALUE;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (t[i][j] == ' ') {
                            t[i][j] = 'O';
                            int eval = algoritmoMinimax(t, prof + 1, false);
                            t[i][j] = ' ';
                            maxEval = Math.max(maxEval, eval);
                        }
                    }
                }
                return maxEval;
            } else { 
                int minEval = Integer.MAX_VALUE;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (t[i][j] == ' ') {
                            t[i][j] = 'X';
                            int eval = algoritmoMinimax(t, prof + 1, true);
                            t[i][j] = ' ';
                            minEval = Math.min(minEval, eval);
                            
                            if (prof == 1) subValoresMinimax.add(eval);
                        }
                    }
                }
                return minEval;
            }
        }

        private int algoritmoMinimaxPoda(char[][] t, int prof, boolean esMax, int alfa, int beta) {
            if (esGanador(t, 'X') || esGanador(t, 'O') || tableroLleno(t)) {
                return calcularUtilidad(t, prof);
            }

            if (esMax) { 
                int maxEval = Integer.MIN_VALUE;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (t[i][j] == ' ') {
                            t[i][j] = 'O';
                            int eval = algoritmoMinimaxPoda(t, prof + 1, false, alfa, beta);
                            t[i][j] = ' ';
                            maxEval = Math.max(maxEval, eval);
                            
                            if (maxEval >= beta) return maxEval;
                            alfa = Math.max(alfa, maxEval);
                        }
                    }
                }
                return maxEval;
            } else { 
                int minEval = Integer.MAX_VALUE;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (t[i][j] == ' ') {
                            t[i][j] = 'X';
                            int eval = algoritmoMinimaxPoda(t, prof + 1, true, alfa, beta);
                            t[i][j] = ' ';
                            minEval = Math.min(minEval, eval);
                            
                            if (prof == 1) subValoresMinimax.add(eval);
                            
                            if (minEval <= alfa) {
                                if (prof == 1) {
                                    podaOcurrida = true;
                                    alfaCorteRaiz = alfa; // Capturamos el valor exacto del Alfa que provocó el corte
                                }
                                return minEval;
                            }
                            beta = Math.min(beta, minEval);
                        }
                    }
                }
                return minEval;
            }
        }

        private void mostrarVentanaArbol() {
            JDialog ventana = new JDialog((Frame)null, "Visualizador de Árbol Completo", true);
            ventana.setSize(900, 600);
            
            JPanel contenedorPrincipal = new JPanel();
            contenedorPrincipal.setLayout(new BoxLayout(contenedorPrincipal, BoxLayout.Y_AXIS));
            
            for (int k = 0; k < historialArbol.size(); k++) {
                PasoArbol p = historialArbol.get(k);
                
                JPanel filaTurno = new JPanel(new FlowLayout(FlowLayout.LEFT));
                filaTurno.setBorder(BorderFactory.createTitledBorder("Turno " + (k+1)));

                // 1. Mostrar Tablero tras movimiento humano
                filaTurno.add(crearMiniTablero(p.tableroPrevio, "Humano (X)", null));
                
                // Flecha
                filaTurno.add(new JLabel("IA Evalúa ->"));

                // 2. Mostrar todas las alternativas de la IA
                for (char[][] alt : p.alternativasIA) {
                    boolean esElegida = false;
                    // Verificar si esta alternativa coincide con la casilla elegida
                    if (alt[p.filaElegida][p.colElegida] == 'O') esElegida = true;
                    
                    filaTurno.add(crearMiniTablero(alt, "Opc. IA", esElegida ? Color.GREEN : null));
                }
                
                contenedorPrincipal.add(filaTurno);
            }

            ventana.add(new JScrollPane(contenedorPrincipal));
            ventana.setLocationRelativeTo(this);
            ventana.setVisible(true);
        }

        private JPanel crearMiniTablero(char[][] t, String titulo, Color bg) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            if (bg != null) p.setBackground(bg);
            
            JPanel grid = new JPanel(new GridLayout(3, 3));
            grid.setPreferredSize(new Dimension(80, 80));
            grid.setOpaque(false);
            
            for(int i=0; i<3; i++) {
                for(int j=0; j<3; j++) {
                    JLabel lbl = new JLabel(String.valueOf(t[i][j]), SwingConstants.CENTER);
                    lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    lbl.setFont(new Font("Arial", Font.BOLD, 14));
                    grid.add(lbl);
                }
            }
            p.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
            p.add(grid, BorderLayout.CENTER);
            return p;
        }


        private void actualizarCasilla(int f, int c, char m) {
            tablero[f][c] = m;
            botones[f][c].setText(String.valueOf(m));
            botones[f][c].setForeground(m == 'X' ? Color.BLUE : Color.RED);
            botones[f][c].setEnabled(false);
        }

        private boolean revisarFinal() {
            if (esGanador(tablero, 'X')) {
                juegoTerminado = true;
                btnVerArbol.setEnabled(true);
                log("\n ¡Resultado: Has Ganado!");
                JOptionPane.showMessageDialog(this, "¡Felicidades, ganaste!", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            if (esGanador(tablero, 'O')) {
                juegoTerminado = true;
                btnVerArbol.setEnabled(true);
                log("\n ¡Resultado: La IA Ganó!");
                String metodo = usaPoda ? "La Poda Alfa-Beta" : "Minimax";
                JOptionPane.showMessageDialog(this, "La IA gana usando " + metodo + ".", "Fin del Juego", JOptionPane.WARNING_MESSAGE);
                return true;
            }
            if (tableroLleno(tablero)) {
                juegoTerminado = true;
                btnVerArbol.setEnabled(true);
                log("\n ¡Resultado: Empate!");
                JOptionPane.showMessageDialog(this, "Es un empate.", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            return false;
        }

        private boolean esGanador(char[][] t, char m) {
            for(int i=0; i<3; i++) {
                if(t[i][0]==m && t[i][1]==m && t[i][2]==m) return true;
                if(t[0][i]==m && t[1][i]==m && t[2][i]==m) return true;
            }
            return (t[0][0]==m && t[1][1]==m && t[2][2]==m) || (t[0][2]==m && t[1][1]==m && t[2][0]==m);
        }

        private boolean tableroLleno(char[][] t) {
            for(char[] f : t) for(char c : f) if(c == ' ') return false;
            return true;
        }

        private char[][] clonarTablero(char[][] original) {
            char[][] copia = new char[3][3];
            for(int i=0; i<3; i++) System.arraycopy(original[i], 0, copia[i], 0, 3);
            return copia;
        }
    }
}