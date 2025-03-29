package com.mycompany.tragmone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Aplicación principal de un tragamonedas implementando el patrón MVC con concurrencia.
 * 
 * <p>Esta clase inicia la aplicación y configura los componentes principales:
 * <ul>
 *   <li>Símbolos del juego</li>
 *   <li>Rodillos mecánicos</li>
 *   <li>Modelo-Vista-Controlador</li>
 * </ul>
 */
public class TragMone {
    
    /**
     * Punto de entrada principal para la aplicación.
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Configuración de símbolos con sus valores y colores
            Symbol[] symbols = {
                new Symbol("🍒", 10, Color.RED),
                new Symbol("🍋", 5, Color.YELLOW),
                new Symbol("🍊", 3, Color.ORANGE),
                new Symbol("⭐", 1, Color.CYAN),
                new Symbol("💎", 20, Color.MAGENTA)
            };

            // Creación de 3 rodillos idénticos
            Reel[] reels = new Reel[3];
            Arrays.fill(reels, new Reel(symbols));
            
            // Inicialización del sistema MVC
            SlotMachine model = new SlotMachine(reels);
            SlotMachineViewGUI view = new SlotMachineViewGUI();
            new SlotMachineController(model, view);
            
            view.show();
        });
    }
}

// ---------------------------------------------------------------------------------
// Clase Symbol: Representa un símbolo individual en el juego
// ---------------------------------------------------------------------------------

/**
 * Representa un símbolo en los rodillos con sus propiedades de juego.
 * 
 * @param name  Emoji o representación visual del símbolo
 * @param value Valor base para cálculos de premios
 * @param color Color asociado para la representación gráfica
 */
class Symbol {
    private final String name;
    private final int value;
    private final Color color;

    /**
     * Constructor que inicializa un nuevo símbolo.
     */
    public Symbol(String name, int value, Color color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }

    /**
     * @return Representación textual/visual del símbolo
     */
    public String getName() {
        return name;
    }

    /**
     * @return Valor numérico del símbolo para cálculos de premio
     */
    public int getValue() {
        return value;
    }

    /**
     * @return Color asociado al símbolo para la interfaz gráfica
     */
    public Color getColor() {
        return color;
    }
}

// ---------------------------------------------------------------------------------
// Clase Reel: Representa un rodillo mecánico del tragamonedas
// ---------------------------------------------------------------------------------

/**
 * Simula el comportamiento de un rodillo mecánico con múltiples símbolos.
 */
class Reel {
    private final Symbol[] symbols;
    private static final Random RANDOM = new Random();

    /**
     * Crea un rodillo con los símbolos especificados.
     * 
     * @param symbols Array de símbolos disponibles para este rodillo
     */
    public Reel(Symbol[] symbols) {
        this.symbols = Arrays.copyOf(symbols, symbols.length);
    }

    /**
     * Simula el giro del rodillo y selecciona un símbolo aleatorio.
     * 
     * @return Símbolo seleccionado aleatoriamente
     */
    public Symbol spin() {
        return symbols[RANDOM.nextInt(symbols.length)];
    }
}

// ---------------------------------------------------------------------------------
// Clase SlotMachine: Modelo principal del juego (MVC)
// ---------------------------------------------------------------------------------

/**
 * Contiene la lógica del juego, manejo de créditos y cálculos de premios.
 */
class SlotMachine {
    private static final int MAX_BET = 50;
    private static final int INITIAL_CREDITS = 100;
    private final Reel[] reels;
    private int bet = 1;
    private int credits = INITIAL_CREDITS;

    /**
     * Inicializa el modelo del tragamonedas.
     * 
     * @param reels Rodillos configurados para el juego
     */
    public SlotMachine(Reel[] reels) {
        this.reels = reels;
    }

    /**
     * Establece la apuesta actual con validación automática de límites.
     * 
     * @param bet Valor de la apuesta (1-50)
     */
    public void setBet(int bet) {
        this.bet = Math.min(Math.max(bet, 1), MAX_BET);
    }

    /**
     * @return Créditos disponibles actuales
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Verifica si es posible realizar un giro con la configuración actual.
     * 
     * @return true si hay créditos suficientes y la apuesta es válida
     */
    public boolean canSpin() {
        return credits >= bet && bet > 0 && bet <= MAX_BET;
    }

    /**
     * Ejecuta un giro completo y calcula los resultados.
     * 
     * @return Objeto SpinResult con los resultados del giro
     */
    public SpinResult spin() {
        if (!canSpin()) {
            return new SpinResult(null, 0, credits);
        }
        
        credits -= bet;
        Symbol[] results = spinReels();
        int winnings = calculateWinnings(results);
        credits += winnings;
        
        return new SpinResult(results, winnings, credits);
    }

    /**
     * Realiza el giro de todos los rodillos.
     * 
     * @return Array de símbolos resultantes
     */
    private Symbol[] spinReels() {
        Symbol[] results = new Symbol[reels.length];
        for (int i = 0; i < reels.length; i++) {
            results[i] = reels[i].spin();
        }
        return results;
    }

    /**
     * Calcula las ganancias basadas en combinaciones de símbolos.
     * 
     * @param symbols Símbolos obtenidos en el giro
     * @return Monto total ganado
     */
    private int calculateWinnings(Symbol[] symbols) {
        long uniqueCount = Arrays.stream(symbols)
                               .map(Symbol::getName)
                               .distinct()
                               .count();

        if (uniqueCount == 1) return symbols[0].getValue() * bet * 2;  // 3 símbolos iguales
        if (uniqueCount == 2) return symbols[0].getValue() * bet;      // 2 símbolos iguales
        return 0;  // Sin combinación ganadora
    }
}

// ---------------------------------------------------------------------------------
// Clase SpinResult: Contenedor de resultados de un giro
// ---------------------------------------------------------------------------------

/**
 * Registro inmutable que contiene los resultados de un giro del tragamonedas.
 * 
 * @param symbols  Símbolos resultantes del giro
 * @param winnings Créditos ganados en el giro
 * @param credits  Saldo total después del giro
 */
record SpinResult(Symbol[] symbols, int winnings, int credits) {}

// ---------------------------------------------------------------------------------
// Clase SlotMachineViewGUI: Vista gráfica del juego (MVC)
// ---------------------------------------------------------------------------------

/**
 * Implementa la interfaz gráfica de usuario usando Swing.
 */
class SlotMachineViewGUI {
    private final JFrame frame;
    private final JLabel[] reelLabels;
    private final JLabel creditsLabel;
    private final JLabel resultLabel;
    private final JTextField betField;
    private final JButton spinButton;
    private final JButton increaseBetButton;
    private final JButton decreaseBetButton;
    private final Color DEFAULT_BG = new Color(30, 30, 30);

    /**
     * Constructor que inicializa todos los componentes gráficos.
     */
    public SlotMachineViewGUI() {
        frame = new JFrame("🎰 Tragamonedas MVC 🎰");
        reelLabels = new JLabel[3];
        creditsLabel = new JLabel("Créditos: ");
        resultLabel = new JLabel("", SwingConstants.CENTER);
        betField = new JTextField("1", 5);
        spinButton = new JButton("GIRAR");
        increaseBetButton = new JButton("+");
        decreaseBetButton = new JButton("-");
        
        configureUI();
    }

    /**
     * Configuración principal de la interfaz gráfica.
     */
    private void configureUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(DEFAULT_BG);
        
        frame.add(createReelsPanel(), BorderLayout.CENTER);
        frame.add(createControlPanel(), BorderLayout.SOUTH);
        frame.add(createInfoPanel(), BorderLayout.NORTH);
        
        styleComponents();
    }

    /**
     * Crea el panel de rodillos con estilo.
     * 
     * @return JPanel configurado
     */
    private JPanel createReelsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(DEFAULT_BG);
        
        for (int i = 0; i < 3; i++) {
            reelLabels[i] = new JLabel("", SwingConstants.CENTER);
            reelLabels[i].setOpaque(true);
            reelLabels[i].setBackground(new Color(50, 50, 50));
            reelLabels[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
            reelLabels[i].setFont(new Font("Segoe UI Emoji", Font.BOLD, 80));
            panel.add(reelLabels[i]);
        }
        return panel;
    }

    /**
     * Crea el panel de control con botones y campos de apuesta.
     * 
     * @return JPanel configurado
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.setBackground(DEFAULT_BG);
        
        JPanel betPanel = new JPanel();
        betPanel.setBackground(DEFAULT_BG);
        betPanel.add(new JLabel("Apuesta:"));
        betPanel.add(decreaseBetButton);
        betPanel.add(betField);
        betPanel.add(increaseBetButton);
        
        JPanel spinPanel = new JPanel();
        spinPanel.setBackground(DEFAULT_BG);
        spinPanel.add(spinButton);
        
        panel.add(betPanel);
        panel.add(spinPanel);
        return panel;
    }

    /**
     * Crea el panel superior de información con créditos y resultados.
     * 
     * @return JPanel configurado
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setBackground(DEFAULT_BG);
        
        creditsLabel.setForeground(Color.WHITE);
        resultLabel.setForeground(Color.YELLOW);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        panel.add(creditsLabel);
        panel.add(resultLabel);
        return panel;
    }

    /**
     * Aplica estilos consistentes a los componentes gráficos.
     */
    private void styleComponents() {
        Color buttonColor = new Color(70, 70, 70);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        
        for (JButton button : new JButton[]{spinButton, increaseBetButton, decreaseBetButton}) {
            button.setBackground(buttonColor);
            button.setForeground(Color.WHITE);
            button.setFont(buttonFont);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }
        
        betField.setFont(new Font("Arial", Font.BOLD, 14));
        betField.setHorizontalAlignment(JTextField.CENTER);
    }

    /**
     * Muestra la ventana principal.
     */
    public void show() {
        frame.setVisible(true);
    }

    // Métodos de configuración de listeners
    public void setSpinButtonListener(ActionListener listener) {
        spinButton.addActionListener(listener);
    }

    public void setIncreaseBetListener(ActionListener listener) {
        increaseBetButton.addActionListener(listener);
    }

    public void setDecreaseBetListener(ActionListener listener) {
        decreaseBetButton.addActionListener(listener);
    }

    /**
     * @return Valor actual de la apuesta
     */
    public int getBet() {
        try {
            return Integer.parseInt(betField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Actualiza el campo de apuesta.
     * 
     * @param bet Nuevo valor a mostrar
     */
    public void setBet(int bet) {
        betField.setText(String.valueOf(bet));
    }

    /**
     * Muestra los resultados de un giro en la interfaz.
     * 
     * @param result Resultados a mostrar
     */
    public void displaySpinResult(SpinResult result) {
        if (result.getSymbols() != null) {
            for (int i = 0; i < result.getSymbols().length; i++) {
                reelLabels[i].setText(result.getSymbols()[i].getName());
                reelLabels[i].setForeground(result.getSymbols()[i].getColor());
            }
        }
        creditsLabel.setText("Créditos: " + result.getCredits());
        resultLabel.setText(result.getWinnings() > 0 ? 
            "¡GANASTE " + result.getWinnings() + " CRÉDITOS! 🎉" : 
            "¡SUERTE EN LA PRÓXIMA! 💸");
    }

    /**
     * Habilita o deshabilita el botón de giro.
     * 
     * @param enabled true para habilitar, false para deshabilitar
     */
    public void setSpinEnabled(boolean enabled) {
        spinButton.setEnabled(enabled);
        spinButton.setText(enabled ? "GIRAR" : "GIRANDO...");
    }

    /**
     * @return Array de etiquetas de los rodillos
     */
    public JLabel[] getReelLabels() {
        return reelLabels;
    }
}

// ---------------------------------------------------------------------------------
// Clase SlotMachineController: Controlador del juego (MVC)
// ---------------------------------------------------------------------------------

/**
 * Coordina la interacción entre el modelo y la vista.
 */
class SlotMachineController {
    private final SlotMachine model;
    private final SlotMachineViewGUI view;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean isSpinning = false;

    /**
     * Crea un nuevo controlador y configura las relaciones MVC.
     * 
     * @param model Modelo del juego
     * @param view Vista gráfica
     */
    public SlotMachineController(SlotMachine model, SlotMachineViewGUI view) {
        this.model = model;
        this.view = view;
        initialize();
    }

    /**
     * Inicializa los listeners y actualiza la vista inicial.
     */
    private void initialize() {
        setupListeners();
        updateView();
    }

    /**
     * Configura los listeners para los controles de la interfaz.
     */
    private void setupListeners() {
        view.setSpinButtonListener(e -> handleSpin());
        view.setIncreaseBetListener(e -> adjustBet(1));
        view.setDecreaseBetListener(e -> adjustBet(-1));
    }

    /**
     * Maneja la lógica de un giro del tragamonedas.
     */
    private void handleSpin() {
        if (isSpinning) return;
        isSpinning = true;
        view.setSpinEnabled(false);
        
        executor.execute(() -> {
            animateSpin();
            SpinResult result = model.spin();
            
            SwingUtilities.invokeLater(() -> {
                view.displaySpinResult(result);
                view.setSpinEnabled(true);
                isSpinning = false;
            });
        });
    }

    /**
     * Ejecuta la animación del giro de los rodillos.
     */
    private void animateSpin() {
        Random random = new Random();
        String[] symbols = {"🍒", "🍋", "🍊", "⭐", "💎"};
        
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(i * 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            SwingUtilities.invokeLater(() -> {
                for (JLabel reel : view.getReelLabels()) {
                    reel.setText(symbols[random.nextInt(symbols.length)]);
                    reel.setForeground(new Color(
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256)
                    ));
                }
            });
        }
    }

    /**
     * Ajusta la apuesta actual con validación de límites.
     * 
     * @param delta Cantidad a incrementar/decrementar (+1/-1)
     */
    private void adjustBet(int delta) {
        int newBet = view.getBet() + delta;
        newBet = Math.max(1, Math.min(newBet, Math.min(model.getCredits(), SlotMachine.MAX_BET)));
        model.setBet(newBet);
        view.setBet(newBet);
        updateView();
    }

    /**
     * Actualiza la vista con el estado actual del modelo.
     */
    private void updateView() {
        view.displaySpinResult(new SpinResult(null, 0, model.getCredits()));
    }
}