import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * MIPS Hazard Analyzer with Performance Evaluation
 * Complete standalone application for Computer Architecture Project
 * 
 * Features:
 * - Detects RAW, WAR, WAW hazards
 * - Detects Control hazards (branches)
 * - Detects Structural hazards (memory conflicts)
 * - Pipeline stage simulation
 * - Performance metrics (CPI, Throughput, Stall Cycles)
 * - Report generation with export to file
 * 
 * @author Rameesha, Urva, Asad
 * @version 2.1 - Fixed Performance Metrics display
 */
public class HazardAnalyzerComplete {
    private static final Color DARK_BG = new Color(30, 35, 45);
    private static final Color primaryColor = new Color(0, 120, 215);
    private static final Color secondaryColor = new Color(0, 153, 188);
    private static final Color textColor = Color.WHITE;
    
    // Pipeline stages
    private static final String[] STAGES = {"IF", "ID", "EX", "MEM", "WB"};
    
    private JFrame frame;
    private JTextArea codeArea;
    private JTable pipelineTable;
    private DefaultTableModel pipelineModel;
    private java.util.Map<Integer, String> cycleHazards = new java.util.HashMap<>();  // Track hazards per cycle
    private java.util.Map<Integer, Integer> instructionHazardCount = new java.util.HashMap<>();  // Hazard count per instruction
    private JTextArea reportArea;
    private JTextArea performanceArea;  // FIXED: Made this a class variable
    private JLabel statsLabel;
    private JTabbedPane tabbedPane;
    private JLabel tooltipLabel;  // For visualization tooltips
    private JTable registerTable;  // Register viewer table reference
    private DefaultTableModel registerModel;  // Register viewer model reference
    
    // Register state tracking
    private java.util.Map<Integer, Integer> registerValues = new java.util.HashMap<>();
    private java.util.Map<Integer, Integer> registerReadCounts = new java.util.HashMap<>();
    private java.util.Map<Integer, Integer> registerWriteCounts = new java.util.HashMap<>();
    private java.util.Map<Integer, String> registerLastWritten = new java.util.HashMap<>();
    
    // Analysis data storage
    private List<Instruction> analysisInstructions = new ArrayList<>();
    private List<Hazard> analysisHazards = new ArrayList<>();
    
    // Gantt chart zoom level
    private float ganttZoom = 1.0f;
    
    // Pipeline animation controls
    private int currentCycle = 0;
    private boolean isAnimating = false;
    private javax.swing.Timer animationTimer = null;
    private int animationSpeed = 500;  // milliseconds per step
    private JLabel cycleIndicatorLabel;
    private JButton playPauseButton;
    
    // Performance metrics
    private int totalInstructions = 0;
    private int totalCycles = 0;
    private int stallCycles = 0;
    private int rawCount = 0, warCount = 0, wawCount = 0, controlCount = 0, structuralCount = 0;
    
    // Responsive font sizing
    private float baseWidth = 1400f;
    private float baseHeight = 900f;
    private float currentScale = 1.0f;
    
    // Cached font references for updating
    private JLabel titleLabel;
    private java.util.List<JLabel> allLabels = new ArrayList<>();
    private java.util.List<JTextArea> allTextAreas = new ArrayList<>();
    private java.util.List<JTable> allTables = new ArrayList<>();
    
    public static void main(String[] args) {
        ProfessionalSplashScreen splash = new ProfessionalSplashScreen();
        splash.setVisible(true);
        
        // Simulate loading
        for (int i = 0; i <= 100; i += 25) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            String[] messages = {
                "Loading MIPS parser...",
                "Initializing hazard detector...",
                "Setting up pipeline simulator...",
                "Loading performance calculator...",
                "Starting GUI..."
            };
            int msgIndex = Math.min(i / 25, messages.length - 1);
            splash.updateProgress(i, messages[msgIndex]);
        }
        
        SwingUtilities.invokeLater(() -> {
            HazardAnalyzerComplete analyzer = new HazardAnalyzerComplete();
            analyzer.createAndShowGUI();
            splash.close();
        });
    }
    
    // Responsive font sizing methods
    private Font getScaledFont(String name, int style, int baseSize) {
        return new Font(name, style, (int)(baseSize * currentScale));
    }
    
    private void updateScaleFactor(int width, int height) {
        currentScale = Math.min((float)width / baseWidth, (float)height / baseHeight);
        currentScale = Math.max(0.8f, Math.min(currentScale, 1.5f)); // Clamp between 0.8x and 1.5x
    }
    
    private int getScaledSize(int baseSize) {
        return (int)(baseSize * currentScale);
    }
    
    private void refreshAllFonts() {
        if (frame == null) return;
        
        // Update title label
        if (titleLabel != null) {
            titleLabel.setFont(getScaledFont("Poppins", Font.BOLD, 16));
        }
        
        // Update status label
        if (statsLabel != null) {
            statsLabel.setFont(getScaledFont("Arial", Font.PLAIN, 11));
        }
        
        // Update code area
        if (codeArea != null) {
            codeArea.setFont(getScaledFont("JetBrains Mono", Font.PLAIN, 13));
        }
        
        // Update pipeline table
        if (pipelineTable != null) {
            pipelineTable.setFont(getScaledFont("Monospaced", Font.PLAIN, 12));
            pipelineTable.getTableHeader().setFont(getScaledFont("Arial", Font.BOLD, 12));
            pipelineTable.setRowHeight(getScaledSize(25));
        }
        
        // Update report and performance areas
        if (reportArea != null) {
            reportArea.setFont(getScaledFont("Monospaced", Font.PLAIN, 12));
        }
        if (performanceArea != null) {
            performanceArea.setFont(getScaledFont("Monospaced", Font.PLAIN, 12));
        }
        
        // Update register table
        if (registerTable != null) {
            registerTable.setFont(getScaledFont("Monospaced", Font.PLAIN, 11));
            registerTable.getTableHeader().setFont(getScaledFont("Arial", Font.BOLD, 12));
            registerTable.setRowHeight(getScaledSize(25));
        }
        
        // Update tabbedPane font
        if (tabbedPane != null) {
            tabbedPane.setFont(getScaledFont("Arial", Font.PLAIN, 12));
        }
        
        frame.repaint();
    }
    
    private void createAndShowGUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);
        frame.setMinimumSize(new Dimension(1400, 900));
        frame.setLayout(new BorderLayout());
        
        // Add component listener for window resizing
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateScaleFactor(frame.getWidth(), frame.getHeight());
                refreshAllFonts();
            }
        });
        
        // Set initial scale
        updateScaleFactor(1400, 900);
        
        // Custom title bar
        
        
        // Main content panel with gradient background
        JPanel mainContent = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(26, 42, 108),      // #1a2a6c
                    getWidth(), getHeight(), new Color(177, 31, 31) // #b21f1f
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        mainContent.setOpaque(false);
        
        // Main split pane with professional styling
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.4);
        mainSplitPane.setOpaque(false);
        mainSplitPane.setBackground(new Color(30, 35, 45));
        
        // LEFT PANEL: Code Input with card style
        RoundedPanel leftCardPanel = new RoundedPanel(15, new Color(30, 35, 45));
        leftCardPanel.setLayout(new BorderLayout(10, 10));
        leftCardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel leftPanel = createCodeInputPanel();
        leftCardPanel.add(leftPanel, BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(leftCardPanel);
        
        // RIGHT PANEL: Results and Pipeline with card style
        RoundedPanel rightCardPanel = new RoundedPanel(15, new Color(30, 35, 45));
        rightCardPanel.setLayout(new BorderLayout(10, 10));
        rightCardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel rightPanel = createResultsPanel();
        rightCardPanel.add(rightPanel, BorderLayout.CENTER);
        mainSplitPane.setRightComponent(rightCardPanel);
        
        mainContent.add(mainSplitPane, BorderLayout.CENTER);
        frame.add(mainContent, BorderLayout.CENTER);
        
        // Professional status bar
        statsLabel = new JLabel("Ready. Load or write MIPS code and click Analyze.", JLabel.CENTER);
        statsLabel.setBorder(BorderFactory.createEtchedBorder());
        statsLabel.setFont(getScaledFont("Arial", Font.PLAIN, 11));
        statsLabel.setForeground(Color.DARK_GRAY);
        frame.add(statsLabel, BorderLayout.SOUTH);
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private JPanel createStatusBar(Color primaryColor, Color secondaryColor, Color textColor) {
        JPanel statusBar = new JPanel(new BorderLayout(15, 0));
        statusBar.setBackground(primaryColor);
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Status indicator
        JLabel statusLabel = new JLabel("● Ready");
        statusLabel.setFont(new Font("Poppins", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(76, 175, 80));
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        // Timestamp label
        statsLabel = new JLabel("Last Analysis: Never | Total Hazards: 0");
        statsLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statsLabel.setForeground(textColor);
        statusBar.add(statsLabel, BorderLayout.CENTER);
        
        // Version label
        JLabel versionLabel = new JLabel("v3.0");
        versionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        versionLabel.setForeground(secondaryColor);
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private JPanel createCodeInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        titleLabel = new JLabel("MIPS Assembly Code Input");
        titleLabel.setFont(getScaledFont("Poppins", Font.BOLD, 16));
        titleLabel.setForeground(secondaryColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Toolbar with modern buttons
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, getScaledSize(8), getScaledSize(5)));
        toolBar.setOpaque(false);
        
        GradientButton loadButton = new GradientButton("Load File");
        GradientButton exampleButton = new GradientButton("Load Example");
        GradientButton analyzeButton = new GradientButton("Analyze Hazards");
        GradientButton clearButton = new GradientButton("Clear");
        GradientButton exportButton = new GradientButton("Export Report");
        GradientButton exportPdfButton = new GradientButton("Export PDF");
        GradientButton benchmarkButton = new GradientButton("Benchmark");
        
        // Set scaled fonts for buttons
        loadButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 12));
        exampleButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 12));
        analyzeButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 12));
        clearButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 12));
        exportButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 12));
        exportPdfButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 12));
        benchmarkButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 12));
        
        loadButton.setToolTipText("Load MIPS assembly code from .asm or .s file");
        exampleButton.setToolTipText("Load example code with all hazard types");
        analyzeButton.setToolTipText("Analyze code for pipeline hazards and calculate performance metrics");
        clearButton.setToolTipText("Clear all input and reset results");
        exportButton.setToolTipText("Export analysis report to text file");
        exportPdfButton.setToolTipText("Export analysis report as formatted PDF document");
        benchmarkButton.setToolTipText("Compare multiple MIPS files for research benchmarking");
        
        loadButton.addActionListener(e -> loadFile());
        exampleButton.addActionListener(e -> loadExample());
        analyzeButton.addActionListener(e -> analyzeCode());
        clearButton.addActionListener(e -> clearAll());
        exportButton.addActionListener(e -> exportReport());
        exportPdfButton.addActionListener(e -> exportToPDF());
        benchmarkButton.addActionListener(e -> runBenchmarkMode());
        
        toolBar.add(loadButton);
        toolBar.add(exampleButton);
        toolBar.add(analyzeButton);
        toolBar.add(clearButton);
        toolBar.add(exportButton);
        toolBar.add(exportPdfButton);
        toolBar.add(benchmarkButton);
        panel.add(toolBar, BorderLayout.CENTER);
        
        // Code area with dark theme and syntax highlighting
        RoundedPanel codePanel = new RoundedPanel(10, new Color(20, 20, 25));
        codePanel.setLayout(new BorderLayout());
        
        // Use JTextPane with syntax highlighting
        JTextPane codePane = new JTextPane();
        codePane.setFont(getScaledFont("JetBrains Mono", Font.PLAIN, 13));
        codePane.setBackground(new Color(20, 20, 25));
        codePane.setForeground(textColor);
        codePane.setCaretColor(secondaryColor);
        codePane.setMargin(new Insets(getScaledSize(10), getScaledSize(10), getScaledSize(10), getScaledSize(10)));
        
        // Create a custom document with syntax highlighting
        MIPSSyntaxDocument syntaxDoc = new MIPSSyntaxDocument();
        codePane.setDocument(syntaxDoc);
        
        // Wrap for compatibility with existing code
        codeArea = new JTextArea() {
            @Override
            public void setText(String t) {
                codePane.setText(t);
            }
            @Override
            public String getText() {
                return codePane.getText();
            }
        };
        
        JScrollPane scrollPane = new JScrollPane(codePane);
        scrollPane.getViewport().setBackground(new Color(20, 20, 25));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        codePanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(codePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Tabbed pane for results
        tabbedPane = new JTabbedPane();
        
        // Pipeline simulation tab with animation controls
        tabbedPane.addTab("Pipeline Simulation", createPipelineSimulationPanel());
        
        // Report tab with styled panel
        // Hazard Report Tab - Using JTextPane for HTML formatting with colors
        reportArea = new JTextArea();
        reportArea.setFont(getScaledFont("Monospaced", Font.PLAIN, 11));
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(20, 25, 35));  // Dark background
        reportArea.setForeground(new Color(200, 220, 255));  // Light blue text
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setMargin(new Insets(15, 15, 15, 15));
        reportArea.setCaretColor(new Color(100, 150, 200));
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.getViewport().setBackground(new Color(20, 25, 35));
        reportScroll.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
        
        RoundedPanel reportPanel = new RoundedPanel(10, new Color(20, 25, 35));
        reportPanel.setLayout(new BorderLayout());
        reportPanel.add(reportScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Hazard Report", reportPanel);
        
        // Performance tab - Using JTextPane for HTML formatting with colors
        performanceArea = new JTextArea();
        performanceArea.setFont(getScaledFont("Monospaced", Font.PLAIN, 11));
        performanceArea.setEditable(false);
        performanceArea.setBackground(new Color(20, 25, 35));  // Dark background
        performanceArea.setForeground(new Color(200, 220, 255));  // Light blue text
        performanceArea.setLineWrap(true);
        performanceArea.setWrapStyleWord(true);
        performanceArea.setMargin(new Insets(15, 15, 15, 15));
        performanceArea.setCaretColor(new Color(100, 150, 200));
        JScrollPane perfScroll = new JScrollPane(performanceArea);
        perfScroll.getViewport().setBackground(new Color(20, 25, 35));
        perfScroll.setBorder(BorderFactory.createLineBorder(new Color(0, 153, 188), 2));
        
        RoundedPanel perfPanel = new RoundedPanel(10, new Color(20, 25, 35));
        perfPanel.setLayout(new BorderLayout());
        perfPanel.add(perfScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Performance Metrics", perfPanel);
        
        // Hazard Visualization tab
        tabbedPane.addTab("Hazard Visualization", createHazardVisualizationPanel());
        
        // Gantt Chart tab
        tabbedPane.addTab("Pipeline Stage Gantt Chart", createGanttChartPanel());
        
        // Hazard Heatmap tab
        tabbedPane.addTab("Hazard Heatmap", createHazardHeatmapPanel());
        
        // Register File Viewer tab
        tabbedPane.addTab("Register File", createRegisterViewerPanel());
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPipelineSimulationPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Control panel with animation buttons and slider
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, getScaledSize(10), getScaledSize(8)));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Pipeline Animation Controls"));
        controlPanel.setBackground(new Color(240, 240, 240));
        
        // Play/Pause button
        playPauseButton = new GradientButton("Play");
        playPauseButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 11));
        playPauseButton.setPreferredSize(new Dimension(getScaledSize(90), getScaledSize(90)));
        playPauseButton.addActionListener(e -> toggleAnimation());
        controlPanel.add(playPauseButton);
        
        // Step button
        JButton stepButton = new GradientButton("Step");
        stepButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 11));
        stepButton.setPreferredSize(new Dimension(getScaledSize(90), getScaledSize(90)));
        stepButton.addActionListener(e -> advanceOneCycle());
        controlPanel.add(stepButton);
        
        // Reset button
        JButton resetButton = new GradientButton("Reset");
        resetButton.setFont(getScaledFont("Segoe UI", Font.BOLD, 11));
        resetButton.setPreferredSize(new Dimension(getScaledSize(90), getScaledSize(90)));
        resetButton.addActionListener(e -> resetAnimation());
        controlPanel.add(resetButton);
        
        // Separator
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        
        // Speed label and slider
        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setFont(getScaledFont("Arial", Font.BOLD, 11));
        controlPanel.add(speedLabel);
        
        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 1000, 500);
        speedSlider.setMajorTickSpacing(300);
        speedSlider.setMinorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setPreferredSize(new Dimension(getScaledSize(250), getScaledSize(50)));
        speedSlider.addChangeListener(e -> {
            animationSpeed = speedSlider.getValue();
            // If animation is running, restart it with new speed
            if (isAnimating && animationTimer != null) {
                animationTimer.stop();
                animationTimer.setDelay(animationSpeed);
                animationTimer.start();
            }
        });
        controlPanel.add(speedSlider);
        
        // Cycle indicator
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        cycleIndicatorLabel = new JLabel("Cycle: 0 / 0");
        cycleIndicatorLabel.setFont(getScaledFont("Arial", Font.BOLD, 12));
        cycleIndicatorLabel.setForeground(Color.BLUE);
        controlPanel.add(cycleIndicatorLabel);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Pipeline table
        pipelineModel = new DefaultTableModel(new String[]{"Cycle", "IF", "ID", "EX", "MEM", "WB", "Hazards"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pipelineTable = new JTable(pipelineModel) {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component comp = super.prepareRenderer(renderer, row, column);
                
                // Highlight current cycle row in yellow
                if (row == currentCycle && currentCycle < pipelineModel.getRowCount()) {
                    comp.setBackground(new Color(255, 255, 100));  // Bright yellow
                    comp.setForeground(Color.BLACK);
                } else if (row % 2 == 0) {
                    comp.setBackground(new Color(240, 240, 240));
                    comp.setForeground(Color.BLACK);
                } else {
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(Color.BLACK);
                }
                
                // Red border on hazard stages
                if (row == currentCycle && column > 0) {
                    for (Hazard h : analysisHazards) {
                        if ((h.instr1Idx == row || h.instr2Idx == row) && column <= 5) {
                            comp.setBackground(new Color(255, 150, 150));  // Light red
                            break;
                        }
                    }
                }
                
                return comp;
            }
        };
        pipelineTable.setFont(getScaledFont("Monospaced", Font.PLAIN, 12));
        pipelineTable.getTableHeader().setFont(getScaledFont("Arial", Font.BOLD, 12));
        pipelineTable.setRowHeight(getScaledSize(25));
        
        // Apply custom cell renderer for hazard highlighting
        pipelineTable.setDefaultRenderer(Object.class, new HazardTableCellRenderer());
        pipelineTable.setDefaultRenderer(String.class, new HazardTableCellRenderer());
        JScrollPane pipelineScroll = new JScrollPane(pipelineTable);
        
        mainPanel.add(pipelineScroll, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private void toggleAnimation() {
        if (totalCycles == 0) {
            JOptionPane.showMessageDialog(frame, "Please analyze code first!");
            return;
        }
        
        if (!isAnimating) {
            // Start animation
            isAnimating = true;
            playPauseButton.setText("Pause");
            playPauseButton.setBackground(new Color(255, 100, 100));
            playPauseButton.setOpaque(true);
            
            animationTimer = new javax.swing.Timer(animationSpeed, e -> {
                if (currentCycle < totalCycles - 1) {
                    currentCycle++;
                    updateCycleIndicator();
                    pipelineTable.repaint();
                } else {
                    // Stop at last cycle
                    isAnimating = false;
                    playPauseButton.setText("▶ Play");
                    playPauseButton.setBackground(null);
                    if (animationTimer != null) {
                        animationTimer.stop();
                    }
                }
            });
            animationTimer.start();
        } else {
            // Pause animation
            isAnimating = false;
            playPauseButton.setText("Play");
            playPauseButton.setBackground(null);
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
    }
    
    private void advanceOneCycle() {
        if (totalCycles == 0) {
            JOptionPane.showMessageDialog(frame, "Please analyze code first!");
            return;
        }
        
        // Stop animation if running
        if (isAnimating) {
            isAnimating = false;
            playPauseButton.setText("▶ Play");
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
        
        if (currentCycle < totalCycles - 1) {
            currentCycle++;
            updateCycleIndicator();
            pipelineTable.repaint();
        }
    }
    
    private void resetAnimation() {
        // Stop animation if running
        if (isAnimating) {
            isAnimating = false;
            playPauseButton.setText("▶ Play");
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
        
        currentCycle = 0;
        updateCycleIndicator();
        pipelineTable.repaint();
    }
    
    private void updateCycleIndicator() {
        if (cycleIndicatorLabel != null) {
            cycleIndicatorLabel.setText("Cycle: " + currentCycle + " / " + (totalCycles - 1));
        }
    }
    
    private void loadFile() {
        JFileChooser chooser = new JFileChooser(".");
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(chooser.getSelectedFile().toPath()));
                codeArea.setText(content);
                statsLabel.setText("Loaded: " + chooser.getSelectedFile().getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error loading file: " + e.getMessage());
            }
        }
    }
    
    private void loadExample() {
        codeArea.setText(
            "# ============================================================\n" +
            "# COMPLETE TEST CODE WITH ALL HAZARD TYPES\n" +
            "# ============================================================\n\n" +
            ".text\n" +
            "main:\n" +
            "\n" +
            "# ---------- RAW HAZARDS ----------\n" +
            "    add $t0, $t1, $t2\n" +
            "    lw  $t3, 0($t0)\n" +
            "\n" +
            "# ---------- WAW HAZARDS ----------\n" +
            "    add $s0, $s1, $s2\n" +
            "    sub $s0, $s3, $s4\n" +
            "\n" +
            "# ---------- WAR HAZARDS ----------\n" +
            "    lw  $a0, 0($a1)\n" +
            "    add $a0, $a2, $a3\n" +
            "\n" +
            "# ---------- CONTROL HAZARDS ----------\n" +
            "    beq $t0, $t1, target\n" +
            "    add $t2, $t3, $t4\n" +
            "target:\n" +
            "    add $t5, $t6, $t7\n" +
            "\n" +
            "# ---------- STRUCTURAL HAZARDS ----------\n" +
            "    lw  $k0, 0($k1)\n" +
            "    sw  $k2, 4($k3)\n" +
            "\n" +
            "# ---------- CLEAN CODE ----------\n" +
            "    add $v0, $zero, $zero\n" +
            "    jr  $ra\n"
        );
        statsLabel.setText("Complete example loaded with all hazard types!");
    }
    
    private void clearAll() {
        codeArea.setText("");
        pipelineModel.setRowCount(0);
        reportArea.setText("");
        performanceArea.setText("");
        statsLabel.setText("Cleared. Load or write new code.");
        
        // Reset register table to initial state
        if (registerModel != null) {
            String[] regNames = {
                "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
                "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
                "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
                "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
            };
            for (int i = 0; i < 32; i++) {
                registerModel.setValueAt("$" + i, i, 0);
                registerModel.setValueAt(regNames[i], i, 1);
                registerModel.setValueAt("0x00000000", i, 2);
                registerModel.setValueAt("-", i, 3);
                registerModel.setValueAt(0, i, 4);
                registerModel.setValueAt(0, i, 5);
            }
            if (registerTable != null) registerTable.repaint();
        }
        
        // Reset animation
        currentCycle = 0;
        isAnimating = false;
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (playPauseButton != null) {
            playPauseButton.setText("▶ Play");
            playPauseButton.setBackground(null);
        }
        if (cycleIndicatorLabel != null) {
            cycleIndicatorLabel.setText("Cycle: 0 / 0");
        }
    }
    
    private void exportReport() {
        String report = reportArea.getText();
        String performance = performanceArea.getText();
        
        if (report.isEmpty() && performance.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No report to export. Please analyze code first!");
            return;
        }
        
        JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new File("Hazard_Analysis_Report.txt"));
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                String fullReport = "=============================================================\n" +
                                    "           MIPS HAZARD ANALYZER COMPLETE REPORT              \n" +
                                    "=============================================================\n\n" +
                                    "Generated on: " + new Date() + "\n\n" +
                                    "=" + "=".repeat(68) + "\n\n" +
                                    report + "\n\n" +
                                    performance;
                
                java.nio.file.Files.write(chooser.getSelectedFile().toPath(), fullReport.getBytes());
                JOptionPane.showMessageDialog(frame, "Report exported successfully to:\n" + 
                                              chooser.getSelectedFile().getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error exporting report: " + e.getMessage());
            }
        }
    }
    
    private void exportToPDF() {
        String report = reportArea.getText();
        String performance = performanceArea.getText();
        
        if (report.isEmpty() && performance.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No report to export. Please analyze code first!");
            return;
        }
        
        try {
            // Try to load iText7 library dynamically
            Class<?> pdfDocumentClass = Class.forName("com.itextpdf.kernel.pdf.PdfDocument");
            Class<?> pdfWriterClass = Class.forName("com.itextpdf.kernel.pdf.PdfWriter");
            Class<?> documentClass = Class.forName("com.itextpdf.layout.Document");
            Class<?> paragraphClass = Class.forName("com.itextpdf.layout.element.Paragraph");
            
            // iText7 is available, proceed with PDF export
            JFileChooser chooser = new JFileChooser(".");
            chooser.setSelectedFile(new File("Hazard_Analysis_Report.pdf"));
            
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    File outputFile = chooser.getSelectedFile();
                    exportToPDFWithiText7(outputFile, report, performance);
                    JOptionPane.showMessageDialog(frame, "PDF exported successfully to:\n" + outputFile.getAbsolutePath());
                    statsLabel.setText("PDF report exported successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error creating PDF: " + ex.getMessage());
                }
            }
        } catch (ClassNotFoundException ex) {
            // iText7 not available, show instructions
            String message = "iText7 library not found. To enable PDF export:\n\n" +
                           "1. Download: https://mvnrepository.com/artifact/com.itextpdf/itext7-core\n" +
                           "2. Save JAR to: w:\\Sem 6\\CA\\HazardAnalyzerProject\\lib\\itext7-core.jar\n" +
                           "3. Recompile: javac -cp lib/* HazardAnalyzerComplete.java\n" +
                           "4. Run: java -cp .:lib/* HazardAnalyzerComplete\n\n" +
                           "For now, use 'Export Report' for text format.";
            JOptionPane.showMessageDialog(frame, message, "PDF Export Not Available", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportToPDFWithiText7(File outputFile, String report, String performance) throws Exception {
        // Use reflection to call iText7 methods
        try {
            // Create PdfWriter: new PdfWriter(filename)
            Class<?> pdfWriterClass = Class.forName("com.itextpdf.kernel.pdf.PdfWriter");
            Object writer = pdfWriterClass.getConstructor(String.class).newInstance(outputFile.getAbsolutePath());
            
            // Create PdfDocument: new PdfDocument(writer)
            Class<?> pdfDocClass = Class.forName("com.itextpdf.kernel.pdf.PdfDocument");
            Object pdfDoc = pdfDocClass.getConstructor(pdfWriterClass).newInstance(writer);
            
            // Create Document: new Document(pdfDoc)
            Class<?> docClass = Class.forName("com.itextpdf.layout.Document");
            Object doc = docClass.getConstructor(pdfDocClass).newInstance(pdfDoc);
            
            // Create title paragraph
            Class<?> paragraphClass = Class.forName("com.itextpdf.layout.element.Paragraph");
            Object titlePara = paragraphClass.getConstructor(String.class).newInstance("MIPS Hazard Analysis Report");
            
            // Set font size using reflection
            Class<?> fontSizeClass = Float.TYPE;
            java.lang.reflect.Method setSizeMethod = paragraphClass.getMethod("setFontSize", fontSizeClass);
            setSizeMethod.invoke(titlePara, 18f);
            
            // Add title to document
            docClass.getMethod("add", Class.forName("com.itextpdf.layout.element.IBlockData")).invoke(doc, titlePara);
            
            // Add metadata paragraph
            Object metaPara = paragraphClass.getConstructor(String.class).newInstance("Generated on: " + new Date());
            setSizeMethod.invoke(metaPara, 10f);
            docClass.getMethod("add", Class.forName("com.itextpdf.layout.element.IBlockData")).invoke(doc, metaPara);
            
            // Add separator
            Object sepPara = paragraphClass.getConstructor(String.class).newInstance("\n");
            docClass.getMethod("add", Class.forName("com.itextpdf.layout.element.IBlockData")).invoke(doc, sepPara);
            
            // Add report content
            String[] reportLines = report.split("\n");
            for (String line : reportLines) {
                Object contentPara = paragraphClass.getConstructor(String.class).newInstance(line.isEmpty() ? " " : line);
                setSizeMethod.invoke(contentPara, 11f);
                docClass.getMethod("add", Class.forName("com.itextpdf.layout.element.IBlockData")).invoke(doc, contentPara);
            }
            
            // Add performance metrics
            Object perfTitlePara = paragraphClass.getConstructor(String.class).newInstance("\nPerformance Metrics");
            setSizeMethod.invoke(perfTitlePara, 14f);
            docClass.getMethod("add", Class.forName("com.itextpdf.layout.element.IBlockData")).invoke(doc, perfTitlePara);
            
            String[] perfLines = performance.split("\n");
            for (String line : perfLines) {
                Object perfPara = paragraphClass.getConstructor(String.class).newInstance(line.isEmpty() ? " " : line);
                setSizeMethod.invoke(perfPara, 10f);
                docClass.getMethod("add", Class.forName("com.itextpdf.layout.element.IBlockData")).invoke(doc, perfPara);
            }
            
            // Close document
            docClass.getMethod("close").invoke(doc);
            
        } catch (Exception e) {
            throw new Exception("Failed to generate PDF with iText7: " + e.getMessage(), e);
        }
    }
    
    private void analyzeCode() {
        String sourceCode = codeArea.getText();
        if (sourceCode.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter or load some MIPS code first!");
            return;
        }
        
        // Reset animation
        currentCycle = 0;
        isAnimating = false;
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (playPauseButton != null) {
            playPauseButton.setText("▶ Play");
            playPauseButton.setBackground(null);
        }
        
        // Reset counters
        totalInstructions = 0;
        totalCycles = 0;
        stallCycles = 0;
        rawCount = warCount = wawCount = controlCount = structuralCount = 0;
        
        // Initialize register tracking
        initializeRegisterTracking();
        
        // Parse instructions
        List<Instruction> instructions = parseInstructions(sourceCode);
        analysisInstructions = instructions;  // Store for visualization
        totalInstructions = instructions.size();
        
        if (totalInstructions == 0) {
            reportArea.setText("No valid instructions found to analyze!");
            performanceArea.setText("No instructions to analyze.");
            return;
        }
        
        // Track register reads and writes during analysis
        updateRegisterTracking(instructions);
        
        // Detect hazards
        List<Hazard> hazards = detectHazards(instructions);
        analysisHazards = hazards;  // Store for visualization
        for (Hazard h : hazards) {
            switch (h.type) {
                case "RAW": rawCount++; break;
                case "WAR": warCount++; break;
                case "WAW": wawCount++; break;
                case "CONTROL": controlCount++; break;
                case "STRUCTURAL": structuralCount++; break;
            }
        }
        
        // Calculate stall cycles (1 stall per hazard typically)
        stallCycles = hazards.size();
        totalCycles = totalInstructions + stallCycles;
        
        // Calculate performance metrics
        double cpi = (double)totalCycles / totalInstructions;
        double throughput = (double)totalInstructions / totalCycles;
        double stallPercentage = totalCycles > 0 ? (stallCycles * 100.0 / totalCycles) : 0;
        
        // Simulate pipeline
        simulatePipeline(instructions, hazards);
        
        // Generate report
        generateReport(instructions, hazards);
        
        // Update performance tab - FIXED: Now updates correctly
        updatePerformanceTab(cpi, throughput, stallPercentage);
        
        // Update register file viewer
        updateRegisterViewerTable();
        
        // Update cycle indicator for animation
        updateCycleIndicator();
        
        // Update status
        statsLabel.setText(String.format("Analysis complete: %d instructions, %d hazards found, CPI: %.2f", 
                           totalInstructions, hazards.size(), cpi));
    }
    
    private List<Instruction> parseInstructions(String sourceCode) {
        List<Instruction> instructions = new ArrayList<>();
        String[] lines = sourceCode.split("\\r?\\n");
        int lineNum = 0;
        
        for (String line : lines) {
            lineNum++;
            // Remove comments
            int commentIndex = line.indexOf('#');
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            line = line.trim();
            
            // Skip empty lines and labels
            if (line.isEmpty() || line.endsWith(":")) {
                continue;
            }
            
            Instruction instr = parseSingleInstruction(line, lineNum);
            if (instr != null && instr.isValid) {
                instructions.add(instr);
            }
        }
        return instructions;
    }
    
    private Instruction parseSingleInstruction(String line, int lineNum) {
        line = line.toLowerCase().trim();
        String[] parts = line.split("\\s+");
        if (parts.length == 0) return null;
        
        Instruction instr = new Instruction();
        instr.original = line;
        instr.lineNum = lineNum;
        instr.name = parts[0];
        
        String operands = "";
        if (line.contains(" ")) {
            operands = line.substring(line.indexOf(' ') + 1).trim();
        }
        
        // Parse based on instruction type
        if (isBranch(instr.name)) {
            instr.type = "BRANCH";
            parseBranch(instr, operands);
            instr.writeReg = null;
        } else if (isLoadStore(instr.name)) {
            instr.type = "MEMORY";
            parseLoadStore(instr, operands);
        } else if (isRType(instr.name)) {
            instr.type = "R-TYPE";
            parseRType(instr, operands);
        } else if (isIType(instr.name)) {
            instr.type = "I-TYPE";
            parseIType(instr, operands);
        } else if (isJump(instr.name)) {
            instr.type = "JUMP";
            instr.writeReg = null;
        } else {
            instr.type = "UNKNOWN";
            instr.isValid = false;
            return instr;
        }
        
        instr.isValid = true;
        return instr;
    }
    
    private boolean isRType(String name) {
        return name.matches("add|addu|sub|subu|and|or|xor|nor|slt|sltu|sll|srl|sra|jr");
    }
    
    private boolean isIType(String name) {
        return name.matches("addi|addiu|andi|ori|xori|slti|sltiu|lui");
    }
    
    private boolean isLoadStore(String name) {
        return name.matches("lw|lh|lhu|lb|lbu|sw|sh|sb");
    }
    
    private boolean isBranch(String name) {
        return name.matches("beq|bne|bgtz|blez|bltz|bgez");
    }
    
    private boolean isJump(String name) {
        return name.matches("j|jal");
    }
    
    private void parseRType(Instruction instr, String operands) {
        if (operands.isEmpty()) return;
        
        String[] regs = operands.split("\\s*,\\s*");
        if (regs.length >= 1 && !instr.name.equals("jr")) {
            int reg = parseRegisterNumber(regs[0]);
            if (reg != -1 && reg != 0) instr.writeReg = reg;
        }
        if (regs.length >= 2) {
            int reg = parseRegisterNumber(regs[1]);
            if (reg != -1 && reg != 0) instr.readRegs.add(reg);
        }
        if (regs.length >= 3) {
            int reg = parseRegisterNumber(regs[2]);
            if (reg != -1 && reg != 0) instr.readRegs.add(reg);
        }
    }
    
    private void parseIType(Instruction instr, String operands) {
        if (operands.isEmpty()) return;
        
        String[] parts = operands.split("\\s*,\\s*");
        if (parts.length >= 2) {
            int rt = parseRegisterNumber(parts[0]);
            if (rt != -1 && rt != 0) instr.writeReg = rt;
            int rs = parseRegisterNumber(parts[1]);
            if (rs != -1 && rs != 0) instr.readRegs.add(rs);
        }
    }
    
    private void parseLoadStore(Instruction instr, String operands) {
        if (operands.isEmpty()) return;
        
        String[] parts = operands.split("\\s*,\\s*");
        if (instr.name.startsWith("l")) {
            // Load: rt, offset(rs)
            if (parts.length >= 2) {
                int rt = parseRegisterNumber(parts[0]);
                if (rt != -1 && rt != 0) instr.writeReg = rt;
                String rsPart = parts[1];
                int parenIndex = rsPart.indexOf('(');
                if (parenIndex != -1) {
                    String rsReg = rsPart.substring(parenIndex + 1, rsPart.indexOf(')'));
                    int rs = parseRegisterNumber(rsReg);
                    if (rs != -1 && rs != 0) instr.readRegs.add(rs);
                }
            }
        } else {
            // Store: rt, offset(rs)
            if (parts.length >= 2) {
                int rt = parseRegisterNumber(parts[0]);
                if (rt != -1 && rt != 0) instr.readRegs.add(rt);
                String rsPart = parts[1];
                int parenIndex = rsPart.indexOf('(');
                if (parenIndex != -1) {
                    String rsReg = rsPart.substring(parenIndex + 1, rsPart.indexOf(')'));
                    int rs = parseRegisterNumber(rsReg);
                    if (rs != -1 && rs != 0) instr.readRegs.add(rs);
                }
            }
        }
    }
    
    private void parseBranch(Instruction instr, String operands) {
        if (operands.isEmpty()) return;
        
        String[] parts = operands.split("\\s*,\\s*");
        if (parts.length >= 2) {
            int rs = parseRegisterNumber(parts[0]);
            if (rs != -1 && rs != 0) instr.readRegs.add(rs);
            int rt = parseRegisterNumber(parts[1]);
            if (rt != -1 && rt != 0) instr.readRegs.add(rt);
        }
    }
    
    private int parseRegisterNumber(String reg) {
        reg = reg.trim().toLowerCase();
        // Handle offset(rs) format
        if (reg.contains("(")) {
            int start = reg.indexOf('(') + 1;
            int end = reg.indexOf(')');
            if (start > 0 && end > start) {
                reg = reg.substring(start, end);
            }
        }
        
        // Named registers
        if (reg.equals("$zero") || reg.equals("$0")) return 0;
        if (reg.equals("$at") || reg.equals("$1")) return 1;
        if (reg.equals("$v0")) return 2;
        if (reg.equals("$v1")) return 3;
        if (reg.equals("$a0")) return 4;
        if (reg.equals("$a1")) return 5;
        if (reg.equals("$a2")) return 6;
        if (reg.equals("$a3")) return 7;
        if (reg.equals("$t0")) return 8;
        if (reg.equals("$t1")) return 9;
        if (reg.equals("$t2")) return 10;
        if (reg.equals("$t3")) return 11;
        if (reg.equals("$t4")) return 12;
        if (reg.equals("$t5")) return 13;
        if (reg.equals("$t6")) return 14;
        if (reg.equals("$t7")) return 15;
        if (reg.equals("$s0")) return 16;
        if (reg.equals("$s1")) return 17;
        if (reg.equals("$s2")) return 18;
        if (reg.equals("$s3")) return 19;
        if (reg.equals("$s4")) return 20;
        if (reg.equals("$s5")) return 21;
        if (reg.equals("$s6")) return 22;
        if (reg.equals("$s7")) return 23;
        if (reg.equals("$t8")) return 24;
        if (reg.equals("$t9")) return 25;
        if (reg.equals("$k0")) return 26;
        if (reg.equals("$k1")) return 27;
        if (reg.equals("$gp")) return 28;
        if (reg.equals("$sp")) return 29;
        if (reg.equals("$fp")) return 30;
        if (reg.equals("$ra")) return 31;
        
        // Number format $n
        if (reg.matches("\\$\\d+")) {
            return Integer.parseInt(reg.substring(1));
        }
        
        return -1;  // Invalid register
    }
    
    private List<Hazard> detectHazards(List<Instruction> instructions) {
        List<Hazard> hazards = new ArrayList<>();
        
        for (int i = 0; i < instructions.size() - 1; i++) {
            Instruction current = instructions.get(i);
            Instruction next = instructions.get(i + 1);
            
            // Skip if registers are invalid (-1)
            
            // 1. RAW Hazard (Read After Write)
            if (current.writeReg != null && current.writeReg != 0 && current.writeReg != -1) {
                if (next.readRegs.contains(current.writeReg)) {
                    hazards.add(new Hazard("RAW", i, i+1, 
                        String.format("Line %d writes $%d, Line %d reads it", 
                        current.lineNum, current.writeReg, next.lineNum)));
                }
            }
            
            // 2. WAW Hazard (Write After Write)
            if (current.writeReg != null && current.writeReg != 0 && current.writeReg != -1 &&
                next.writeReg != null && next.writeReg != 0 && next.writeReg != -1 &&
                current.writeReg.equals(next.writeReg)) {
                hazards.add(new Hazard("WAW", i, i+1,
                    String.format("Lines %d and %d both write to $%d", 
                    current.lineNum, next.lineNum, current.writeReg)));
            }
            
            // 3. WAR Hazard (Write After Read)
            if (next.writeReg != null && next.writeReg != 0 && next.writeReg != -1) {
                if (current.readRegs.contains(next.writeReg)) {
                    hazards.add(new Hazard("WAR", i, i+1,
                        String.format("Line %d reads $%d, Line %d writes it",
                        current.lineNum, next.writeReg, next.lineNum)));
                }
            }
            
            // 4. Control Hazard (Branch instructions)
            if (current.type.equals("BRANCH")) {
                hazards.add(new Hazard("CONTROL", i, i+1,
                    String.format("Branch instruction at line %d causes pipeline flush (1-2 cycle penalty)", 
                    current.lineNum)));
            }
            
            // 5. Structural Hazard (Memory unit conflicts)
            // NOTE: In a standard 5-stage pipelined MIPS:
            // - Instruction i uses MEM stage at cycle N
            // - Instruction i+1 uses MEM stage at cycle N+1
            // They use the memory unit at DIFFERENT cycles, so NO conflict.
            // True structural hazards require SAME resource in SAME cycle.
            // Consecutive memory ops are fine in pipelined MIPS (no false positives)
            if (false) {  // Disabled: pipelined MIPS handles sequential memory ops
                hazards.add(new Hazard("STRUCTURAL", i, i+1,
                    String.format("Consecutive memory accesses at lines %d and %d",
                    current.lineNum, next.lineNum)));
            }
        }
        
        return hazards;
    }
    
    // private void simulatePipeline(List<Instruction> instructions, List<Hazard> hazards) {
    //     pipelineModel.setRowCount(0);
        
    //     int cycle = 1;
    //     int maxCycles = Math.min(instructions.size() + 10, 50);
        
    //     for (int i = 0; i < maxCycles; i++) {
    //         Object[] row = new Object[6];
    //         row[0] = cycle;
            
    //         // Fill stages (simplified visualization)
    //         if (i < instructions.size()) row[1] = instructions.get(i).name;
    //         if (i+1 < instructions.size()) row[2] = instructions.get(i+1).name;
    //         if (i+2 < instructions.size()) row[3] = instructions.get(i+2).name;
    //         if (i+3 < instructions.size()) row[4] = instructions.get(i+3).name;
    //         if (i+4 < instructions.size()) row[5] = instructions.get(i+4).name;
            
    //         pipelineModel.addRow(row);
    //         cycle++;
    //     }
    // }
    
    private void simulatePipeline(List<Instruction> instructions, List<Hazard> hazards) {
    pipelineModel.setRowCount(0);
    cycleHazards.clear();
    instructionHazardCount.clear();
    
    int totalInstructions = instructions.size();
    int totalStages = 5;  // IF, ID, EX, MEM, WB
    int totalCycles = totalInstructions + totalStages - 1;  // Formula: N + 5 - 1
    
    // Pre-process hazards to map them to specific cycles
    for (Hazard h : hazards) {
        String hazardInfo = h.type;
        int hazardCycle = -1;
        if (h.type.equals("RAW")) hazardCycle = h.instr1Idx + 2;
        else if (h.type.equals("CONTROL")) hazardCycle = h.instr1Idx + 1;
        else if (h.type.equals("STRUCTURAL")) hazardCycle = h.instr1Idx + 3;
        else if (h.type.equals("WAW") || h.type.equals("WAR")) hazardCycle = h.instr1Idx + 2;
        
        if (hazardCycle >= 0 && hazardCycle < totalCycles) {
            String existing = cycleHazards.getOrDefault(hazardCycle, "");
            cycleHazards.put(hazardCycle, existing.isEmpty() ? hazardInfo : existing + ", " + hazardInfo);
        }
        instructionHazardCount.put(h.instr1Idx, instructionHazardCount.getOrDefault(h.instr1Idx, 0) + 1);
        instructionHazardCount.put(h.instr2Idx, instructionHazardCount.getOrDefault(h.instr2Idx, 0) + 1);
    }
    
    for (int cycle = 0; cycle < totalCycles; cycle++) {
        Object[] row = new Object[7];
        row[0] = cycle + 1;  // Cycle number (1-based)
        
        // Stage 1: IF (Instruction Fetch)
        if (cycle < totalInstructions) {
            row[1] = instructions.get(cycle).name;
        } else {
            row[1] = "-";
        }
        
        // Stage 2: ID (Instruction Decode)
        if (cycle - 1 >= 0 && cycle - 1 < totalInstructions) {
            row[2] = instructions.get(cycle - 1).name;
        } else {
            row[2] = "-";
        }
        
        // Stage 3: EX (Execute)
        if (cycle - 2 >= 0 && cycle - 2 < totalInstructions) {
            row[3] = instructions.get(cycle - 2).name;
        } else {
            row[3] = "-";
        }
        
        // Stage 4: MEM (Memory Access)
        if (cycle - 3 >= 0 && cycle - 3 < totalInstructions) {
            row[4] = instructions.get(cycle - 3).name;
        } else {
            row[4] = "-";
        }
        
        // Stage 5: WB (Write Back)
        if (cycle - 4 >= 0 && cycle - 4 < totalInstructions) {
            row[5] = instructions.get(cycle - 4).name;
        } else {
            row[5] = "-";
        }
        
        // Column 6: Hazards for this cycle
        String hazardsForCycle = cycleHazards.getOrDefault(cycle, "-");
        if (!hazardsForCycle.equals("-")) {
            row[6] = "⚠️ " + hazardsForCycle;
        } else {
            row[6] = "-";
        }
        
        pipelineModel.addRow(row);
    }
}


    private void generateReport(List<Instruction> instructions, List<Hazard> hazards) {
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════════════════╗\n");
        report.append("║              ✓ MIPS HAZARD ANALYSIS REPORT - DETAILED               ║\n");
        report.append("╚════════════════════════════════════════════════════════════════════╝\n\n");
        
        report.append("┌─ INSTRUCTIONS ANALYZED ────────────────────────────────────────────┐\n");
        for (int i = 0; i < Math.min(instructions.size(), 50); i++) {
            Instruction instr = instructions.get(i);
            String instrText = instr.original.length() > 35 ? instr.original.substring(0, 32) + "..." : instr.original;
            report.append(String.format("  %2d: %-35s [W: %s | R: %s]\n",
                instr.lineNum, instrText,
                instr.writeReg != null && instr.writeReg != -1 ? "$"+instr.writeReg : "-",
                instr.readRegs.isEmpty() ? "-" : instr.readRegs.toString()));
        }
        report.append("└────────────────────────────────────────────────────────────────────┘\n\n");
        
        report.append("┌─ HAZARDS DETECTED ──────────────────────────────────────────────────┐\n");
        if (hazards.isEmpty()) {
            report.append("│  ✓ No hazards detected! Code is pipeline-safe.                    │\n");
        } else {
            for (int i = 0; i < hazards.size(); i++) {
                Hazard h = hazards.get(i);
                String typeIcon = h.type.equals("RAW") ? "⚠" : h.type.equals("WAW") ? "⚠" : 
                                  h.type.equals("WAR") ? "⚠" : h.type.equals("CONTROL") ? "🔀" : "⚡";
                String line = String.format("  %s %d. [%-10s] %s", typeIcon, i+1, h.type, h.description);
                if (line.length() > 75) {
                    report.append("│ ").append(line.substring(0, 72)).append(" │\n");
                } else {
                    report.append("│ ").append(String.format("%-72s", line)).append(" │\n");
                }
            }
        }
        report.append("└────────────────────────────────────────────────────────────────────┘\n\n");
        
        report.append("┌─ HAZARD SUMMARY ────────────────────────────────────────────────────┐\n");
        report.append(String.format("│  %-30s %3d (RAW)           │\n", "Data Hazards", rawCount));
        report.append(String.format("│  ├─ Read After Write (RAW)", rawCount)).append(String.format("%21d │\n", rawCount));
        report.append(String.format("│  ├─ Write After Read (WAR)", warCount)).append(String.format("%21d │\n", warCount));
        report.append(String.format("│  └─ Write After Write (WAW)", wawCount)).append(String.format("%20d │\n", wawCount));
        report.append(String.format("│  Control Hazards", controlCount)).append(String.format("%39d │\n", controlCount));
        report.append(String.format("│  Structural Hazards", structuralCount)).append(String.format("%35d │\n", structuralCount));
        report.append("├────────────────────────────────────────────────────────────────────┤\n");
        report.append(String.format("│  %-30s %3d                   │\n", "TOTAL HAZARDS", hazards.size()));
        report.append("└────────────────────────────────────────────────────────────────────┘\n\n");
        
        report.append("┌─ OPTIMIZATION RECOMMENDATIONS ──────────────────────────────────────┐\n");
        if (hazards.isEmpty()) {
            report.append("│  ✓ Excellent! Your code has no pipeline hazards.                  │\n");
        } else {
            if (rawCount > 0) {
                report.append("│  • Insert NOP or reorder instructions to avoid read-after-write   │\n");
            }
            if (warCount > 0) {
                report.append("│  • Use different registers or reorder to avoid write-after-read   │\n");
            }
            if (wawCount > 0) {
                report.append("│  • Avoid writing to the same register in consecutive instructions │\n");
            }
            if (controlCount > 0) {
                report.append("│  • Use branch delay slots or prediction to hide branch penalty    │\n");
            }
            if (structuralCount > 0) {
                report.append("│  • Reduce resource conflicts by spacing memory accesses           │\n");
            }
        }
        report.append("└────────────────────────────────────────────────────────────────────┘\n");
        
        reportArea.setText(report.toString());
    }
    
    private void updatePerformanceTab(double cpi, double throughput, double stallPercentage) {
        // FIXED: Now this method properly updates the performanceArea
        StringBuilder perf = new StringBuilder();
        perf.append("╔════════════════════════════════════════════════════════════════════╗\n");
        perf.append("║               📊 PERFORMANCE METRICS SUMMARY & ANALYSIS              ║\n");
        perf.append("╚════════════════════════════════════════════════════════════════════╝\n\n");
        
        perf.append("┌─ EXECUTION STATISTICS ──────────────────────────────────────────────┐\n");
        perf.append(String.format("│  Total Instructions Executed . . . . . . . . . . . . %3d        │\n", totalInstructions));
        perf.append(String.format("│  Total Cycles Taken . . . . . . . . . . . . . . . . %3d        │\n", totalCycles));
        perf.append(String.format("│  Stall Cycles (caused by hazards) . . . . . . . . %3d        │\n", stallCycles));
        perf.append("└────────────────────────────────────────────────────────────────────┘\n\n");
        
        perf.append("┌─ PERFORMANCE INDICATORS ────────────────────────────────────────────┐\n");
        perf.append(String.format("│  CPI (Cycles Per Instruction) . . . . . . . . . . . . %6.2f    │\n", cpi));
        perf.append(String.format("│  Throughput (Instructions/Cycle) . . . . . . . . . . %6.2f    │\n", throughput));
        perf.append(String.format("│  Stall Percentage . . . . . . . . . . . . . . . . . . %6.1f %%  │\n", stallPercentage));
        perf.append("└────────────────────────────────────────────────────────────────────┘\n\n");
        
        perf.append("┌─ PERFORMANCE INTERPRETATION ────────────────────────────────────────┐\n");
        String verdict;
        if (cpi == 1.0) {
            verdict = "✓ IDEAL CPI = 1.0 - Perfect pipeline performance!";
        } else if (cpi < 1.3) {
            verdict = "✓ EXCELLENT CPI - Very few pipeline stalls.";
        } else if (cpi < 1.6) {
            verdict = "△ GOOD CPI - Minor performance impact from hazards.";
        } else if (cpi < 2.0) {
            verdict = "△ MODERATE CPI - Consider optimizing code structure.";
        } else {
            verdict = "✗ HIGH CPI - Significant hazards detected. Optimize needed.";
        }
        String verdictLine = String.format("│  %s", String.format("%-64s", verdict));
        perf.append(verdictLine).append(" │\n");
        perf.append("└────────────────────────────────────────────────────────────────────┘\n\n");
        
        perf.append("┌─ HAZARD BREAKDOWN & DISTRIBUTION ───────────────────────────────────┐\n");
        int totalHazards = rawCount + warCount + wawCount + controlCount + structuralCount;
        if (totalHazards > 0) {
            perf.append(String.format("│  %-30s %3d (%5.1f%%)              │\n", "READ-AFTER-WRITE (RAW)", rawCount, rawCount*100.0/totalHazards));
            perf.append(String.format("│  %-30s %3d (%5.1f%%)              │\n", "WRITE-AFTER-READ (WAR)", warCount, warCount*100.0/totalHazards));
            perf.append(String.format("│  %-30s %3d (%5.1f%%)              │\n", "WRITE-AFTER-WRITE (WAW)", wawCount, wawCount*100.0/totalHazards));
            perf.append(String.format("│  %-30s %3d (%5.1f%%)              │\n", "CONTROL HAZARDS", controlCount, controlCount*100.0/totalHazards));
            perf.append(String.format("│  %-30s %3d (%5.1f%%)              │\n", "STRUCTURAL HAZARDS", structuralCount, structuralCount*100.0/totalHazards));
            perf.append("├────────────────────────────────────────────────────────────────────┤\n");
            perf.append(String.format("│  %-30s %3d (100.0%%)             │\n", "TOTAL", totalHazards));
        } else {
            perf.append("│  ✓ No hazards detected! Code is fully optimized.                  │\n");
        }
        perf.append("└────────────────────────────────────────────────────────────────────┘\n");
        
        // FIXED: Set the text in performanceArea
        performanceArea.setText(perf.toString());
    }
    
    private JPanel createHeatmapLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        legend.setBorder(BorderFactory.createTitledBorder("Hazard Count Legend"));
        legend.setBackground(new Color(245, 245, 245));
        
        int[][] legendData = {
            {0, 0, 255, 255, 200},      // Light yellow = 0
            {1, 2, 255, 165, 0},        // Orange = 1-2
            {3, 5, 200, 50, 50},        // Dark red = 3-5
            {6, 10, 100, 20, 20}        // Brown = 6+
        };
        
        for (int[] data : legendData) {
            int minVal = data[0];
            int maxVal = data[1];
            Color color = new Color(data[2], data[3], data[4]);
            
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            item.setOpaque(false);
            
            JPanel colorBox = new JPanel();
            colorBox.setBackground(color);
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            colorBox.setPreferredSize(new Dimension(25, 25));
            item.add(colorBox);
            
            String label;
            if (minVal == maxVal) {
                label = minVal + " hazard";
            } else if (maxVal == 10) {
                label = minVal + "+ hazards";
            } else {
                label = minVal + "-" + maxVal + " hazards";
            }
            
            JLabel labelText = new JLabel(label);
            labelText.setFont(getScaledFont("Arial", Font.PLAIN, 11));
            item.add(labelText);
            
            legend.add(item);
        }
        
        return legend;
    }
    
    private JPanel createHazardVisualizationPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Tooltip label at top
        tooltipLabel = new JLabel("Hover over cells to see details");
        tooltipLabel.setBorder(BorderFactory.createEtchedBorder());
        tooltipLabel.setFont(getScaledFont("Arial", Font.ITALIC, 11));
        tooltipLabel.setForeground(Color.DARK_GRAY);
        mainPanel.add(tooltipLabel, BorderLayout.NORTH);
        
        // Create custom drawing panel
        JPanel gridPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int cellWidth = 80;
                int cellHeight = 40;
                int padding = 50;
                
                // Draw axes
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(padding, getHeight() - padding, padding, 20);
                g2d.drawLine(padding, getHeight() - padding, getWidth() - 20, getHeight() - padding);
                
                // Draw axis labels
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("Cycles →", getWidth() - 120, getHeight() - 10);
                
                // Draw cycle numbers (X-axis)
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                for (int c = 0; c < totalCycles && c < 100; c++) {
                    int x = padding + c * cellWidth + cellWidth / 2;
                    int y = getHeight() - padding + 20;
                    g2d.drawString(String.valueOf(c), x - 10, y);
                }
                
                // Draw instruction labels (Y-axis)
                for (int i = 0; i < totalInstructions && i < 100; i++) {
                    int x = 20;
                    int y = 40 + i * cellHeight + cellHeight / 2 + 5;
                    String label = "I" + i;
                    g2d.drawString(label, x, y);
                }
                
                // Draw grid cells
                for (int i = 0; i < totalInstructions && i < 100; i++) {
                    for (int c = 0; c < totalCycles && c < 100; c++) {
                        int x = padding + c * cellWidth;
                        int y = 40 + i * cellHeight;
                        
                        // Determine pipeline stage for this instruction at this cycle
                        int stage = c - i;
                        
                        // Determine color based on hazards
                        Color cellColor = new Color(240, 240, 240);  // Light gray (empty by default)
                        
                        if (stage >= 0 && stage < 5) {
                            cellColor = new Color(100, 255, 100);  // GREEN: normal execution
                            
                            // Check for hazards affecting this instruction
                            for (Hazard h : analysisHazards) {
                                if (h.instr1Idx == i || h.instr2Idx == i) {
                                    if (h.type.equals("RAW")) {
                                        cellColor = new Color(255, 200, 100);  // ORANGE
                                    } else if (h.type.equals("WAW")) {
                                        cellColor = new Color(255, 192, 203);  // PINK
                                    } else if (h.type.equals("WAR")) {
                                        cellColor = new Color(255, 192, 203);  // PINK
                                    } else if (h.type.equals("CONTROL")) {
                                        cellColor = new Color(100, 150, 255);  // BLUE
                                    } else if (h.type.equals("STRUCTURAL")) {
                                        cellColor = new Color(150, 150, 150);  // GRAY
                                    }
                                    break;
                                }
                            }
                        }
                        
                        // Draw cell background
                        g2d.setColor(cellColor);
                        g2d.fillRect(x, y, cellWidth, cellHeight);
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawRect(x, y, cellWidth, cellHeight);
                        
                        // Draw stage name in cell
                        if (stage >= 0 && stage < 5) {
                            g2d.setColor(Color.BLACK);
                            g2d.setFont(new Font("Arial", Font.BOLD, 11));
                            String stageName = STAGES[stage];
                            FontMetrics fm = g2d.getFontMetrics();
                            int textX = x + (cellWidth - fm.stringWidth(stageName)) / 2;
                            int textY = y + ((cellHeight - fm.getHeight()) / 2) + fm.getAscent();
                            g2d.drawString(stageName, textX, textY);
                        }
                    }
                }
            }
            
            @Override
            public Dimension getPreferredSize() {
                int cellWidth = 80;
                int cellHeight = 40;
                int width = Math.min(50 + totalCycles * cellWidth + 50, 3000);
                int height = Math.min(40 + totalInstructions * cellHeight + 50, 3000);
                return new Dimension(width, height);
            }
        };
        
        gridPanel.setBackground(Color.WHITE);
        
        // Add mouse motion listener for dynamic tooltips
        gridPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                int cellWidth = 80;
                int cellHeight = 40;
                int padding = 50;
                
                int col = (evt.getX() - padding) / cellWidth;
                int row = (evt.getY() - 40) / cellHeight;
                
                if (col >= 0 && col < totalCycles && row >= 0 && row < totalInstructions) {
                    int stage = col - row;
                    String tooltip;
                    
                    if (stage >= 0 && stage < 5 && row < analysisInstructions.size()) {
                        Instruction instr = analysisInstructions.get(row);
                        tooltip = "Instr " + row + " (" + instr.name + ") in " + STAGES[stage] + " @ Cycle " + col;
                        
                        // Check for hazards
                        for (Hazard h : analysisHazards) {
                            if (h.instr1Idx == row || h.instr2Idx == row) {
                                tooltip += " | " + h.type + ": " + h.description;
                                break;
                            }
                        }
                    } else {
                        tooltip = "Empty cell (stage outside pipeline)";
                    }
                    
                    tooltipLabel.setText(tooltip);
                } else {
                    tooltipLabel.setText("Hover over cells to see details");
                }
            }
        });
        
        gridPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tooltipLabel.setText("Hover over cells to see details");
            }
        });
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Add legend panel at bottom
        JPanel legendPanel = createLegendPanel();
        mainPanel.add(legendPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JPanel createLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        legend.setBorder(BorderFactory.createTitledBorder("Legend - Pipeline Hazards"));
        
        String[] labels = {
            "GREEN\n(Normal)",
            "ORANGE\n(RAW)",
            "PINK\n(WAW/WAR)",
            "BLUE\n(Control)",
            "GRAY\n(Structural)",
            "LIGHT GRAY\n(Empty)"
        };
        
        Color[] colors = {
            new Color(100, 255, 100),
            new Color(255, 200, 100),
            new Color(255, 192, 203),
            new Color(100, 150, 255),
            new Color(150, 150, 150),
            new Color(240, 240, 240)
        };
        
        for (int i = 0; i < labels.length; i++) {
            JPanel item = new JPanel(new BorderLayout(5, 5));
            item.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            
            JPanel colorBox = new JPanel();
            colorBox.setBackground(colors[i]);
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            colorBox.setPreferredSize(new Dimension(30, 30));
            
            JLabel label = new JLabel(labels[i]);
            label.setFont(getScaledFont("Arial", Font.PLAIN, 9));
            
            item.add(colorBox, BorderLayout.WEST);
            item.add(label, BorderLayout.CENTER);
            legend.add(item);
        }
        
        return legend;
    }
    
    private JPanel createGanttChartPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Zoom control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, getScaledSize(10), getScaledSize(5)));
        controlPanel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setFont(getScaledFont("Arial", Font.PLAIN, 11));
        JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 50, 200, 100);
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.setPreferredSize(new Dimension(getScaledSize(300), getScaledSize(50)));
        
        JLabel zoomPercentLabel = new JLabel("100%");
        zoomPercentLabel.setFont(getScaledFont("Arial", Font.PLAIN, 11));
        
        zoomSlider.addChangeListener(e -> {
            ganttZoom = zoomSlider.getValue() / 100f;
            zoomPercentLabel.setText(zoomSlider.getValue() + "%");
        });
        
        controlPanel.add(zoomLabel);
        controlPanel.add(zoomSlider);
        controlPanel.add(zoomPercentLabel);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Create Gantt chart drawing panel
        JPanel ganttPanel = new JPanel() {
            private java.util.Map<String, String> hoveredBox = null;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int cellWidth = (int) (60 * ganttZoom);
                int cellHeight = 40;
                int rowHeight = cellHeight + 10;
                int leftMargin = 120;
                int topMargin = 40;
                
                // Draw title
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Pipeline Gantt Chart - Instruction Flow Through Stages", 20, 25);
                
                // Draw cycle numbers at top
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.setColor(Color.DARK_GRAY);
                for (int c = 0; c < totalCycles && c < 200; c++) {
                    int x = leftMargin + c * cellWidth;
                    g2d.drawString("C" + c, x + 5, topMargin - 5);
                    
                    // Draw vertical grid line
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawLine(x, topMargin, x, getHeight() - 20);
                    g2d.setColor(Color.DARK_GRAY);
                }
                
                // Draw instructions
                for (int i = 0; i < totalInstructions && i < 200; i++) {
                    Instruction instr = analysisInstructions.get(i);
                    
                    // Draw instruction label
                    int y = topMargin + i * rowHeight + cellHeight / 2;
                    g2d.setFont(new Font("Arial", Font.BOLD, 11));
                    g2d.setColor(Color.BLACK);
                    String instrLabel = "I" + i + ": " + instr.name;
                    g2d.drawString(instrLabel, 10, y + 5);
                    
                    // Draw pipeline stages for this instruction
                    for (int stage = 0; stage < 5; stage++) {
                        int cycle = i + stage;
                        if (cycle < totalCycles) {
                            int x = leftMargin + cycle * cellWidth;
                            int boxY = topMargin + i * rowHeight;
                            
                            // Determine color based on stage
                            Color stageColor;
                            switch (stage) {
                                case 0: stageColor = new Color(173, 216, 230); break; // LightBlue (IF)
                                case 1: stageColor = new Color(144, 238, 144); break; // LightGreen (ID)
                                case 2: stageColor = new Color(255, 255, 153); break; // LightYellow (EX)
                                case 3: stageColor = new Color(255, 192, 203); break; // LightPink (MEM)
                                case 4: stageColor = new Color(224, 255, 255); break; // LightCyan (WB)
                                default: stageColor = Color.LIGHT_GRAY;
                            }
                            
                            g2d.setColor(stageColor);
                            g2d.fillRect(x, boxY, cellWidth, cellHeight);
                            
                            // Check if this instruction has a hazard
                            boolean hasHazard = false;
                            Hazard hazardAtPoint = null;
                            for (Hazard h : analysisHazards) {
                                if (h.instr1Idx == i || h.instr2Idx == i) {
                                    hasHazard = true;
                                    hazardAtPoint = h;
                                    break;
                                }
                            }
                            
                            // Draw border
                            if (hasHazard) {
                                g2d.setColor(Color.RED);
                                g2d.setStroke(new BasicStroke(3));
                                g2d.drawRect(x, boxY, cellWidth, cellHeight);
                                
                                // Draw exclamation mark
                                g2d.setColor(Color.RED);
                                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                                g2d.drawString("!", x + cellWidth - 15, boxY + 20);
                            } else {
                                g2d.setColor(Color.BLACK);
                                g2d.setStroke(new BasicStroke(1));
                                g2d.drawRect(x, boxY, cellWidth, cellHeight);
                            }
                            
                            // Draw stage label
                            g2d.setColor(Color.BLACK);
                            g2d.setFont(new Font("Arial", Font.BOLD, 9));
                            String stageName = STAGES[stage];
                            FontMetrics fm = g2d.getFontMetrics();
                            int textX = x + (cellWidth - fm.stringWidth(stageName)) / 2;
                            int textY = boxY + ((cellHeight - fm.getHeight()) / 2) + fm.getAscent();
                            g2d.drawString(stageName, textX, textY);
                        }
                    }
                }
            }
            
            @Override
            public Dimension getPreferredSize() {
                int cellWidth = (int) (60 * ganttZoom);
                int width = Math.min(120 + totalCycles * cellWidth + 50, 5000);
                int height = Math.min(40 + totalInstructions * 50 + 50, 3000);
                return new Dimension(width, height);
            }
        };
        
        ganttPanel.setBackground(Color.WHITE);
        
        // Mouse listener for clicking on boxes
        ganttPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int cellWidth = (int) (60 * ganttZoom);
                int leftMargin = 120;
                int topMargin = 40;
                int rowHeight = 50;
                
                int col = (evt.getX() - leftMargin) / cellWidth;
                int row = (evt.getY() - topMargin) / rowHeight;
                
                if (col >= 0 && col < totalCycles && row >= 0 && row < totalInstructions) {
                    int stage = col - row;
                    if (stage >= 0 && stage < 5 && row < analysisInstructions.size()) {
                        Instruction instr = analysisInstructions.get(row);
                        
                        StringBuilder details = new StringBuilder();
                        details.append("Instruction Details\n");
                        details.append("=".repeat(40)).append("\n\n");
                        details.append("Instruction #: ").append(row).append("\n");
                        details.append("Name: ").append(instr.name).append("\n");
                        details.append("Type: ").append(instr.type).append("\n");
                        details.append("Pipeline Stage: ").append(STAGES[stage]).append(" @ Cycle ").append(col).append("\n");
                        details.append("Original: ").append(instr.original).append("\n\n");
                        
                        details.append("Registers:\n");
                        details.append("  Write: ").append(instr.writeReg != null && instr.writeReg != -1 ? "$" + instr.writeReg : "None").append("\n");
                        details.append("  Read: ").append(instr.readRegs.isEmpty() ? "None" : instr.readRegs.toString()).append("\n\n");
                        
                        // Check for hazards
                        boolean foundHazard = false;
                        for (Hazard h : analysisHazards) {
                            if (h.instr1Idx == row || h.instr2Idx == row) {
                                details.append("Hazard Type: ").append(h.type).append("\n");
                                details.append("Description: ").append(h.description).append("\n");
                                foundHazard = true;
                                break;
                            }
                        }
                        
                        if (!foundHazard) {
                            details.append("Status: No hazards detected\n");
                        }
                        
                        JTextArea textArea = new JTextArea(details.toString());
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
                        textArea.setMargin(new Insets(10, 10, 10, 10));
                        
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(450, 300));
                        
                        JOptionPane.showMessageDialog(ganttPanel, scrollPane, 
                            "Instruction " + row + " Details", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(ganttPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createHazardHeatmapPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Export button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        
        JButton exportButton = new GradientButton("Export as PNG");
        exportButton.setFont(new Font("Arial", Font.PLAIN, 12));
        exportButton.addActionListener(e -> exportHeatmapAsPNG());
        buttonPanel.add(exportButton);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        // Split pane for heatmap and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.7);
        
        // Heatmap container with legend below
        JPanel heatmapContainer = new JPanel(new BorderLayout());
        
        // Heatmap panel
        JPanel heatmapPanel = new JPanel() {
            private String hoveredTooltip = null;
            private int hoveredX = -1, hoveredY = -1;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int cellWidth = 40;
                int cellHeight = 50;
                int leftMargin = 120;
                int topMargin = 50;
                
                // Draw title
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.setColor(Color.BLACK);
                g2d.drawString("Hazard Heatmap - Instruction vs Pipeline Stage", 20, 30);
                
                // Create hazard count matrix
                int[][] hazardCount = new int[5][totalInstructions];
                java.util.Map<String, java.util.List<Integer>> stageHazards = new java.util.HashMap<>();
                for (int i = 0; i < 5; i++) {
                    stageHazards.put(STAGES[i], new java.util.ArrayList<>());
                }
                
                for (Hazard h : analysisHazards) {
                    for (int stage = 0; stage < 5; stage++) {
                        int instrIdx = h.instr1Idx;
                        if (instrIdx < totalInstructions) {
                            hazardCount[stage][instrIdx]++;
                            stageHazards.get(STAGES[stage]).add(instrIdx);
                        }
                        instrIdx = h.instr2Idx;
                        if (instrIdx < totalInstructions) {
                            hazardCount[stage][instrIdx]++;
                        }
                    }
                }
                
                // Draw stage labels (Y-axis)
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                for (int stage = 0; stage < 5; stage++) {
                    int y = topMargin + stage * cellHeight + cellHeight / 2;
                    String label = STAGES[stage];
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(label, 10, y + 5);
                }
                
                // Draw instruction numbers (X-axis)
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                for (int i = 0; i < totalInstructions && i < 100; i++) {
                    int x = leftMargin + i * cellWidth + cellWidth / 2;
                    g2d.drawString(String.valueOf(i), x - 10, topMargin - 10);
                }
                
                // Draw heatmap cells
                for (int stage = 0; stage < 5; stage++) {
                    for (int i = 0; i < totalInstructions && i < 100; i++) {
                        int x = leftMargin + i * cellWidth;
                        int y = topMargin + stage * cellHeight;
                        int count = hazardCount[stage][i];
                        
                        // Determine color based on hazard count
                        Color cellColor;
                        if (count == 0) {
                            cellColor = new Color(255, 255, 200);  // Light yellow
                        } else if (count <= 2) {
                            cellColor = new Color(255, 165, 0);    // Orange
                        } else if (count <= 5) {
                            cellColor = new Color(200, 50, 50);    // Dark red
                        } else {
                            cellColor = new Color(100, 20, 20);    // Brown
                        }
                        
                        g2d.setColor(cellColor);
                        g2d.fillRect(x, y, cellWidth, cellHeight);
                        
                        // Draw border
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawRect(x, y, cellWidth, cellHeight);
                        
                        // Draw count if > 0
                        if (count > 0) {
                            g2d.setColor(Color.WHITE);
                            g2d.setFont(new Font("Arial", Font.BOLD, 11));
                            FontMetrics fm = g2d.getFontMetrics();
                            String countStr = String.valueOf(count);
                            int textX = x + (cellWidth - fm.stringWidth(countStr)) / 2;
                            int textY = y + ((cellHeight - fm.getHeight()) / 2) + fm.getAscent();
                            g2d.drawString(countStr, textX, textY);
                        }
                        
                        // Highlight hovered cell
                        if (stage == hoveredY && i == hoveredX) {
                            g2d.setColor(Color.YELLOW);
                            g2d.setStroke(new BasicStroke(3));
                            g2d.drawRect(x, y, cellWidth, cellHeight);
                        }
                    }
                }
                
            }
            
            private void drawHeatmapLegend(Graphics2D g2d, int x, int y) {
                // Legend now displayed in separate panel below heatmap
            }
            
            @Override
            public Dimension getPreferredSize() {
                int width = Math.min(120 + totalInstructions * 40 + 100, 3000);
                int height = 50 + 5 * 50 + 100;
                return new Dimension(width, height);
            }
        };
        
        heatmapPanel.setBackground(Color.WHITE);
        
        // Mouse motion listener for tooltips
        heatmapPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                int cellWidth = 40;
                int cellHeight = 50;
                int leftMargin = 120;
                int topMargin = 50;
                
                int col = (evt.getX() - leftMargin) / cellWidth;
                int row = (evt.getY() - topMargin) / cellHeight;
                
                if (col >= 0 && col < totalInstructions && row >= 0 && row < 5) {
                    // Count hazards for this cell
                    int hazardCount = 0;
                    java.util.List<String> hazardTypes = new java.util.ArrayList<>();
                    
                    for (Hazard h : analysisHazards) {
                        if ((h.instr1Idx == col || h.instr2Idx == col)) {
                            hazardCount++;
                            if (!hazardTypes.contains(h.type)) {
                                hazardTypes.add(h.type);
                            }
                        }
                    }
                    
                    String tooltip = String.format("Stage %s, Instruction %d: %d hazards (%s)", 
                        STAGES[row], col, hazardCount, 
                        hazardTypes.isEmpty() ? "none" : String.join(", ", hazardTypes));
                    
                    heatmapPanel.setToolTipText(tooltip);
                } else {
                    heatmapPanel.setToolTipText(null);
                }
            }
        });
        
        JScrollPane heatmapScroll = new JScrollPane(heatmapPanel);
        
        // Legend panel for heatmap
        JPanel legendPanel = createHeatmapLegendPanel();
        
        // Add heatmap and legend to container
        heatmapContainer.add(heatmapScroll, BorderLayout.CENTER);
        heatmapContainer.add(legendPanel, BorderLayout.SOUTH);
        
        splitPane.setTopComponent(heatmapContainer);
        
        // Bar chart panel
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 13));
                g2d.drawString("Hazard Type Distribution", 20, 25);
                
                // Data for bar chart
                int[] hazardCounts = {rawCount, warCount, wawCount, controlCount, structuralCount};
                String[] labels = {"RAW", "WAR", "WAW", "Control", "Structural"};
                Color[] colors = {Color.RED, Color.GREEN, new Color(200, 100, 200), Color.BLUE, Color.GRAY};
                
                int maxCount = java.util.Arrays.stream(hazardCounts).max().orElse(1);
                if (maxCount == 0) maxCount = 1;
                
                int barWidth = 40;
                int barSpacing = 15;
                int leftMargin = 50;
                int topMargin = 50;
                int chartHeight = getHeight() - topMargin - 40;
                
                // Draw bars
                for (int i = 0; i < labels.length; i++) {
                    int barHeight = (int) ((double) hazardCounts[i] / maxCount * chartHeight);
                    int x = leftMargin + i * (barWidth + barSpacing);
                    int y = topMargin + chartHeight - barHeight;
                    
                    // Draw bar
                    g2d.setColor(colors[i]);
                    g2d.fillRect(x, y, barWidth, barHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(x, y, barWidth, barHeight);
                    
                    // Draw count
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    String countStr = String.valueOf(hazardCounts[i]);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x + (barWidth - fm.stringWidth(countStr)) / 2;
                    g2d.drawString(countStr, textX, y - 5);
                    
                    // Draw label
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    int labelX = x + (barWidth - fm.stringWidth(labels[i])) / 2;
                    g2d.drawString(labels[i], labelX, topMargin + chartHeight + 20);
                }
                
                // Draw axes
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(40, topMargin, 40, topMargin + chartHeight);
                g2d.drawLine(40, topMargin + chartHeight, getWidth() - 20, topMargin + chartHeight);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 200);
            }
        };
        
        chartPanel.setBackground(Color.WHITE);
        splitPane.setBottomComponent(chartPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private void exportHeatmapAsPNG() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new File("Hazard_Heatmap.png"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Image", "png"));
        
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                // Create a buffered image for the heatmap
                int width = 1200;
                int height = 600;
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                    width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                
                // Fill background
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, width, height);
                
                // Draw heatmap
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.setColor(Color.BLACK);
                g2d.drawString("MIPS Pipeline Hazard Heatmap", 20, 30);
                
                int cellWidth = 30;
                int cellHeight = 40;
                int leftMargin = 100;
                int topMargin = 80;
                
                // Create hazard count matrix
                int[][] hazardCount = new int[5][totalInstructions];
                for (Hazard h : analysisHazards) {
                    for (int stage = 0; stage < 5; stage++) {
                        if (h.instr1Idx < totalInstructions) {
                            hazardCount[stage][h.instr1Idx]++;
                        }
                        if (h.instr2Idx < totalInstructions) {
                            hazardCount[stage][h.instr2Idx]++;
                        }
                    }
                }
                
                // Draw stage labels
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                for (int stage = 0; stage < 5; stage++) {
                    int y = topMargin + stage * cellHeight + cellHeight / 2;
                    g2d.drawString(STAGES[stage], 10, y + 5);
                }
                
                // Draw cells
                for (int stage = 0; stage < 5; stage++) {
                    for (int i = 0; i < totalInstructions && i < 30; i++) {
                        int x = leftMargin + i * cellWidth;
                        int y = topMargin + stage * cellHeight;
                        int count = hazardCount[stage][i];
                        
                        Color cellColor;
                        if (count == 0) {
                            cellColor = new Color(255, 255, 200);
                        } else if (count <= 2) {
                            cellColor = new Color(255, 165, 0);
                        } else if (count <= 5) {
                            cellColor = new Color(200, 50, 50);
                        } else {
                            cellColor = new Color(100, 20, 20);
                        }
                        
                        g2d.setColor(cellColor);
                        g2d.fillRect(x, y, cellWidth, cellHeight);
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.drawRect(x, y, cellWidth, cellHeight);
                    }
                }
                
                // Draw legend
                int legendY = topMargin + 5 * cellHeight + 30;
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                g2d.setColor(Color.BLACK);
                g2d.drawString("Legend:", 100, legendY);
                
                String[] legendLabels = {"0 hazards", "1-2 hazards", "3-5 hazards", "6+ hazards"};
                Color[] legendColors = {
                    new Color(255, 255, 200), new Color(255, 165, 0),
                    new Color(200, 50, 50), new Color(100, 20, 20)
                };
                
                for (int i = 0; i < legendLabels.length; i++) {
                    int x = 100 + i * 220;
                    g2d.setColor(legendColors[i]);
                    g2d.fillRect(x, legendY + 10, 30, 20);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, legendY + 10, 30, 20);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2d.drawString(legendLabels[i], x + 35, legendY + 25);
                }
                
                g2d.dispose();
                
                javax.imageio.ImageIO.write(image, "png", chooser.getSelectedFile());
                JOptionPane.showMessageDialog(frame, "Heatmap exported successfully to:\n" + 
                    chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error exporting heatmap: " + ex.getMessage());
            }
        }
    }
    
    // Custom table cell renderer for hazard highlighting
    class HazardTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Set default styling
            if (isSelected) {
                comp.setBackground(new Color(100, 150, 255));
                comp.setForeground(Color.WHITE);
            } else if (row % 2 == 0) {
                comp.setBackground(new Color(240, 240, 240));
                comp.setForeground(Color.BLACK);
            } else {
                comp.setBackground(Color.WHITE);
                comp.setForeground(Color.BLACK);
            }
            
            // Check if this cell contains hazard information
            if (column >= 1 && column <= 5 && value != null && !value.toString().equals("-")) {
                // Check if this instruction has a hazard at this cycle
                if (cycleHazards.containsKey(row)) {
                    comp.setBackground(new Color(255, 150, 150));  // Light red for hazard
                    ((JLabel) comp).setFont(new Font("Monospaced", Font.BOLD, 12));
                }
            }
            
            // Hazards column (column 6)
            if (column == 6) {
                if (value != null && !value.toString().equals("-")) {
                    comp.setBackground(new Color(255, 200, 100));  // Orange for hazards
                    ((JLabel) comp).setFont(new Font("Monospaced", Font.BOLD, 12));
                    ((JComponent) comp).setToolTipText("Hazard: " + value.toString());
                }
            }
            
            return comp;
        }
    }
    
    private void runBenchmarkMode() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Select MIPS files for benchmark");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MIPS Files (*.asm, *.s, *.txt)", "asm", "s", "txt"));
        
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            
            // Create results table
            String[] columns = {"File", "Instructions", "Cycles", "CPI", "Hazards", "RAW", "WAW", "WAR", "Control", "Structural"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (File file : files) {
                try {
                    String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                    List<Instruction> instrs = parseInstructions(content);
                    List<Hazard> hazards = detectHazards(instrs);
                    
                    // Calculate metrics
                    int totalHazards = hazards.size();
                    int raw = (int) hazards.stream().filter(h -> h.type.equals("RAW")).count();
                    int waw = (int) hazards.stream().filter(h -> h.type.equals("WAW")).count();
                    int war = (int) hazards.stream().filter(h -> h.type.equals("WAR")).count();
                    int control = (int) hazards.stream().filter(h -> h.type.equals("CONTROL")).count();
                    int structural = (int) hazards.stream().filter(h -> h.type.equals("STRUCTURAL")).count();
                    
                    int cycles = instrs.size() + totalHazards;
                    double cpi = instrs.size() > 0 ? (double) cycles / instrs.size() : 0;
                    
                    model.addRow(new Object[]{
                        file.getName(), instrs.size(), cycles, 
                        String.format("%.2f", cpi), totalHazards,
                        raw, waw, war, control, structural
                    });
                } catch (IOException e) {
                    model.addRow(new Object[]{file.getName(), "ERROR", e.getMessage(), "", "", "", "", "", "", ""});
                }
            }
            
            // Show results in new window
            JTable resultTable = new JTable(model);
            resultTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
            resultTable.setRowHeight(25);
            resultTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            JScrollPane scroll = new JScrollPane(resultTable);
            
            JFrame benchFrame = new JFrame("Benchmark Results");
            benchFrame.setSize(1200, 500);
            benchFrame.add(scroll);
            benchFrame.setLocationRelativeTo(frame);
            benchFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            benchFrame.setVisible(true);
        }
    }
    
    private JPanel createRegisterViewerPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        String[] regNames = {
            "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
            "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
            "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
            "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
        };
        
        String[] columns = {"Register", "Name", "Value", "Last Written", "Read Count", "Write Count"};
        registerModel = new DefaultTableModel(columns, 32);
        
        for (int i = 0; i < 32; i++) {
            registerModel.setValueAt("$" + i, i, 0);
            registerModel.setValueAt(regNames[i], i, 1);
            registerModel.setValueAt("0x00000000", i, 2);
            registerModel.setValueAt("-", i, 3);
            registerModel.setValueAt(0, i, 4);
            registerModel.setValueAt(0, i, 5);
        }
        
        registerTable = new JTable(registerModel);
        registerTable.setRowHeight(getScaledSize(25));
        registerTable.setFont(getScaledFont("Monospaced", Font.PLAIN, 11));
        registerTable.getTableHeader().setFont(getScaledFont("Arial", Font.BOLD, 12));
        
        // Color code: $zero is read-only (gray)
        registerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (row == 0) {
                    c.setBackground(new Color(200, 200, 200));
                    c.setForeground(Color.BLACK);
                } else if (isSelected) {
                    c.setBackground(new Color(100, 150, 255));
                    c.setForeground(Color.WHITE);
                } else if (row % 2 == 0) {
                    c.setBackground(new Color(240, 240, 240));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        
        JScrollPane scroll = new JScrollPane(registerTable);
        mainPanel.add(scroll, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        infoPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel infoLabel = new JLabel("Register $zero is read-only (always 0). Other registers show counts during pipeline simulation. Analyze code to see register activity.");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(new Color(80, 80, 80));
        infoPanel.add(infoLabel);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void initializeRegisterTracking() {
        registerValues.clear();
        registerReadCounts.clear();
        registerWriteCounts.clear();
        registerLastWritten.clear();
        
        // Initialize all 32 registers to 0
        for (int i = 0; i < 32; i++) {
            registerValues.put(i, 0);
            registerReadCounts.put(i, 0);
            registerWriteCounts.put(i, i == 0 ? -1 : 0);  // $zero cannot be written
            registerLastWritten.put(i, "-");
        }
    }
    
    private void updateRegisterTracking(List<Instruction> instructions) {
        for (int cycle = 0; cycle < instructions.size(); cycle++) {
            Instruction instr = instructions.get(cycle);
            
            // Track reads
            for (int reg : instr.readRegs) {
                if (reg >= 0 && reg < 32) {
                    registerReadCounts.put(reg, registerReadCounts.getOrDefault(reg, 0) + 1);
                }
            }
            
            // Track writes
            if (instr.writeReg != null && instr.writeReg > 0 && instr.writeReg < 32) {
                registerWriteCounts.put(instr.writeReg, registerWriteCounts.getOrDefault(instr.writeReg, 0) + 1);
                registerLastWritten.put(instr.writeReg, "Cycle " + (cycle + 3));  // WB stage is 4 cycles later (IF=cycle, WB=cycle+4)
                // Simulate a simple value update (in real code, would compute from instruction semantics)
                int newValue = registerValues.getOrDefault(instr.writeReg, 0) + 1;
                registerValues.put(instr.writeReg, newValue);
            }
        }
    }
    
    private void updateRegisterViewerTable() {
        if (registerModel == null || registerTable == null) return;
        
        String[] regNames = {
            "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
            "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
            "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
            "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
        };
        
        for (int i = 0; i < 32; i++) {
            registerModel.setValueAt("$" + i, i, 0);
            registerModel.setValueAt(regNames[i], i, 1);
            
            // Value: Show in hex or as decimal for $zero
            int value = registerValues.getOrDefault(i, 0);
            String valueStr = String.format("0x%08X", value);
            registerModel.setValueAt(valueStr, i, 2);
            
            // Last Written
            String lastWritten = registerLastWritten.getOrDefault(i, "-");
            registerModel.setValueAt(lastWritten, i, 3);
            
            // Read Count
            int readCount = registerReadCounts.getOrDefault(i, 0);
            registerModel.setValueAt(readCount, i, 4);
            
            // Write Count
            int writeCount = registerWriteCounts.getOrDefault(i, 0);
            registerModel.setValueAt(writeCount, i, 5);
        }
        
        // Force refresh
        registerTable.repaint();
    }
    
    private void showHelpDialog() {
        String helpText = "<html>" +
            "<body style='width: 500px; font-family: Segoe UI;'>" +
            "<h2 style='color: #00ACC1;'>MIPS Hazard Analyzer - Help Guide</h2>" +
            "" +
            "<h3>📋 What is a Pipeline Hazard?</h3>" +
            "<p>A pipeline hazard prevents the next instruction from executing during its designated clock cycle.</p>" +
            "" +
            "<h3>🔍 Hazard Types Detected:</h3>" +
            "<ul>" +
            "<li><b>RAW (Read After Write)</b> - Instruction reads register before previous write completes</li>" +
            "<li><b>WAW (Write After Write)</b> - Two instructions write to same register</li>" +
            "<li><b>WAR (Write After Read)</b> - Instruction writes to register being read</li>" +
            "<li><b>Control Hazard</b> - Branch instruction causes pipeline flush</li>" +
            "<li><b>Structural Hazard</b> - Memory access conflicts</li>" +
            "</ul>" +
            "" +
            "<h3>📊 Performance Metrics:</h3>" +
            "<ul>" +
            "<li><b>CPI</b> - Cycles Per Instruction (ideal = 1.0)</li>" +
            "<li><b>Throughput</b> - Instructions per cycle</li>" +
            "<li><b>Stall %</b> - Percentage of cycles wasted on stalls</li>" +
            "</ul>" +
            "" +
            "<h3>📚 Supported Instructions:</h3>" +
            "<p>add, sub, and, or, lw, sw, beq, bne, j, addi, and more...</p>" +
            "</body>" +
            "</html>";
        
        JOptionPane.showMessageDialog(frame, helpText, "MIPS Hazard Analyzer Help", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ============================================================================
    // INNER CLASSES
    // ============================================================================
    
    static class ProfessionalSplashScreen extends JWindow {
        private JProgressBar progressBar;
        private JLabel statusLabel;
        
        public ProfessionalSplashScreen() {
            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(new Color(30, 35, 45));
            content.setBorder(BorderFactory.createLineBorder(new Color(0, 172, 193), 2));
            
            // Logo/Title area
            JLabel titleLabel = new JLabel("MIPS Pipeline Hazard Analyzer", JLabel.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setForeground(new Color(0, 172, 193));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
            
            JLabel subtitleLabel = new JLabel("Research Edition v3.0", JLabel.CENTER);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitleLabel.setForeground(Color.LIGHT_GRAY);
            
            // Progress bar
            progressBar = new JProgressBar();
            progressBar.setPreferredSize(new Dimension(400, 8));
            progressBar.setBackground(new Color(50, 55, 65));
            progressBar.setForeground(new Color(0, 172, 193));
            progressBar.setBorderPainted(false);
            
            statusLabel = new JLabel("Initializing modules...", JLabel.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            statusLabel.setForeground(new Color(150, 160, 170));
            statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));
            
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.add(titleLabel, BorderLayout.NORTH);
            centerPanel.add(subtitleLabel, BorderLayout.CENTER);
            
            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.setOpaque(false);
            southPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
            southPanel.add(progressBar, BorderLayout.NORTH);
            southPanel.add(statusLabel, BorderLayout.SOUTH);
            
            content.add(centerPanel, BorderLayout.CENTER);
            content.add(southPanel, BorderLayout.SOUTH);
            
            setContentPane(content);
            pack();
            setSize(500, 300);
            setLocationRelativeTo(null);
        }
        
        public void updateProgress(int value, String message) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(value);
                statusLabel.setText(message);
            });
        }
        
        public void close() {
            SwingUtilities.invokeLater(() -> setVisible(false));
        }
    }
    
    static class ThemeManager {
        public static final Color DARK_BG = new Color(30, 35, 45);
        public static final Color DARK_PANEL = new Color(40, 45, 55);
        public static final Color LIGHT_BG = new Color(240, 242, 245);
        public static final Color LIGHT_PANEL = new Color(255, 255, 255);
        
        private static boolean isDark = true;
        
        public static void toggleTheme(JFrame frame, JPanel... panels) {
            isDark = !isDark;
            Color bgColor = isDark ? DARK_BG : LIGHT_BG;
            Color panelColor = isDark ? DARK_PANEL : LIGHT_PANEL;
            
            frame.getContentPane().setBackground(bgColor);
            for (JPanel panel : panels) {
                panel.setBackground(panelColor);
            }
            frame.repaint();
        }
        
        public static boolean isDark() { return isDark; }
    }
    
    // ==================== PROFESSIONAL UI COMPONENTS ====================
    
    static class RoundedPanel extends JPanel {
        private int cornerRadius = 15;
        private Color backgroundColor;
        
        public RoundedPanel(int radius, Color bg) {
            this.cornerRadius = radius;
            this.backgroundColor = bg;
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
        }
    }
    
    static class GradientButton extends JButton {
        private Color startColor = new Color(13, 71, 161);
        private Color endColor = new Color(0, 172, 193);
        
        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Enhanced text rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            
            // Set font with proper rendering
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
    
    static class CustomTitleBar extends JPanel {
        private JFrame parent;
        private JLabel titleLabel;
        private JButton closeBtn;
        private JButton minBtn;
        private Point dragStart;
        
        public CustomTitleBar(JFrame frame, String title) {
            this.parent = frame;
            setPreferredSize(new Dimension(frame.getWidth(), 35));
            setBackground(new Color(25, 35, 55));
            setLayout(new BorderLayout());
            
            titleLabel = new JLabel("  " + title);
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.setOpaque(false);
            
            minBtn = createTitleButton("−");
            closeBtn = createTitleButton("✕");
            closeBtn.addActionListener(e -> System.exit(0));
            minBtn.addActionListener(e -> parent.setState(JFrame.ICONIFIED));
            
            buttonPanel.add(minBtn);
            buttonPanel.add(closeBtn);
            
            add(titleLabel, BorderLayout.WEST);
            add(buttonPanel, BorderLayout.EAST);
            
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { dragStart = e.getPoint(); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point p = parent.getLocation();
                    parent.setLocation(p.x + e.getX() - dragStart.x, p.y + e.getY() - dragStart.y);
                }
            });
        }
        
        private JButton createTitleButton(String text) {
            JButton btn = new GradientButton(text);
            btn.setPreferredSize(new Dimension(45, 45));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(65, 75, 95)); }
                public void mouseExited(MouseEvent e) { btn.setBackground(new Color(45, 55, 75)); }
            });
            return btn;
        }
    }
    
    // Helper classes
    static class Instruction {
        String original;
        String name;
        String type;
        int lineNum;
        Integer writeReg = null;
        Set<Integer> readRegs = new HashSet<>();
        boolean isValid = false;
    }
    
    static class Hazard {
        String type;
        int instr1Idx;
        int instr2Idx;
        String description;
        
        Hazard(String type, int idx1, int idx2, String desc) {
            this.type = type;
            this.instr1Idx = idx1;
            this.instr2Idx = idx2;
            this.description = desc;
        }
    }
    
    static class MIPSSyntaxDocument extends javax.swing.text.DefaultStyledDocument {
        private javax.swing.text.SimpleAttributeSet keywordAttrs = new javax.swing.text.SimpleAttributeSet();
        private javax.swing.text.SimpleAttributeSet registerAttrs = new javax.swing.text.SimpleAttributeSet();
        private javax.swing.text.SimpleAttributeSet commentAttrs = new javax.swing.text.SimpleAttributeSet();
        private javax.swing.text.SimpleAttributeSet numberAttrs = new javax.swing.text.SimpleAttributeSet();
        private javax.swing.text.SimpleAttributeSet labelAttrs = new javax.swing.text.SimpleAttributeSet();
        private javax.swing.text.SimpleAttributeSet normalAttrs = new javax.swing.text.SimpleAttributeSet();
        
        private static final String[] MIPS_KEYWORDS = {
            "add", "addu", "addi", "addiu", "sub", "subu", "and", "andi", "or", "ori", "xor", "xori", "nor",
            "sll", "srl", "sra", "slt", "sltu", "slti", "sltiu",
            "lw", "lh", "lhu", "lb", "lbu", "sw", "sh", "sb",
            "beq", "bne", "bgtz", "blez", "bgez", "bltz",
            "j", "jal", "jr",
            "lui", "main", ".text", ".data"
        };
        
        MIPSSyntaxDocument() {
            // Keyword style: Blue
            javax.swing.text.StyleConstants.setForeground(keywordAttrs, new Color(65, 150, 255));
            javax.swing.text.StyleConstants.setBold(keywordAttrs, true);
            
            // Register style: Green
            javax.swing.text.StyleConstants.setForeground(registerAttrs, new Color(76, 200, 100));
            
            // Comment style: Gray
            javax.swing.text.StyleConstants.setForeground(commentAttrs, new Color(120, 120, 120));
            javax.swing.text.StyleConstants.setItalic(commentAttrs, true);
            
            // Number style: Red/Orange
            javax.swing.text.StyleConstants.setForeground(numberAttrs, new Color(255, 150, 100));
            
            // Label style: Purple
            javax.swing.text.StyleConstants.setForeground(labelAttrs, new Color(200, 120, 255));
            javax.swing.text.StyleConstants.setBold(labelAttrs, true);
            
            // Normal style: White
            javax.swing.text.StyleConstants.setForeground(normalAttrs, Color.WHITE);
        }
        
        @Override
        public void insertString(int offset, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            super.insertString(offset, text, attrs);
            highlightSyntax();
        }
        
        @Override
        public void remove(int offset, int length) throws javax.swing.text.BadLocationException {
            super.remove(offset, length);
            highlightSyntax();
        }
        
        private void highlightSyntax() {
            try {
                String text = getText(0, getLength());
                setCharacterAttributes(0, getLength(), normalAttrs, true);
                
                String[] lines = text.split("\n");
                int offset = 0;
                
                for (String line : lines) {
                    highlightLine(line, offset);
                    offset += line.length() + 1;
                }
            } catch (Exception e) {
                // Silently ignore highlighting errors
            }
        }
        
        private void highlightLine(String line, int lineOffset) {
            try {
                // Highlight comments
                int commentIdx = line.indexOf('#');
                if (commentIdx != -1) {
                    setCharacterAttributes(lineOffset + commentIdx, line.length() - commentIdx, commentAttrs, false);
                    line = line.substring(0, commentIdx);
                }
                
                // Highlight labels (ends with :)
                if (line.trim().endsWith(":")) {
                    String label = line.trim();
                    int labelStart = lineOffset + line.indexOf(label);
                    setCharacterAttributes(labelStart, label.length(), labelAttrs, false);
                    return;
                }
                
                // Tokenize the line
                String[] tokens = line.split("[\\s,()]+");
                int searchOffset = lineOffset;
                
                for (String token : tokens) {
                    if (token.isEmpty()) continue;
                    
                    int tokenIdx = line.indexOf(token, searchOffset - lineOffset);
                    if (tokenIdx == -1) continue;
                    
                    int absoluteIdx = lineOffset + tokenIdx;
                    
                    // Check if keyword
                    if (isKeyword(token)) {
                        setCharacterAttributes(absoluteIdx, token.length(), keywordAttrs, false);
                    }
                    // Check if register
                    else if (isRegister(token)) {
                        setCharacterAttributes(absoluteIdx, token.length(), registerAttrs, false);
                    }
                    // Check if number
                    else if (isNumber(token)) {
                        setCharacterAttributes(absoluteIdx, token.length(), numberAttrs, false);
                    }
                    
                    searchOffset = tokenIdx + token.length();
                }
            } catch (Exception e) {
                // Silently ignore
            }
        }
        
        private boolean isKeyword(String token) {
            String lower = token.toLowerCase();
            for (String kw : MIPS_KEYWORDS) {
                if (lower.equals(kw)) return true;
            }
            return false;
        }
        
        private boolean isRegister(String token) {
            String lower = token.toLowerCase();
            return lower.matches("\\$[a-z0-9]+") || lower.matches("\\$(zero|at|v[0-1]|a[0-3]|t[0-9]|s[0-7]|k[01]|gp|sp|fp|ra)");
        }
        
        private boolean isNumber(String token) {
            try {
                if (token.startsWith("0x") || token.startsWith("0X")) {
                    Integer.parseInt(token.substring(2), 16);
                    return true;
                }
                Integer.parseInt(token);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}







