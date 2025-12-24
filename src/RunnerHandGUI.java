//comando para gerar JAR: javac -d bin **/*.java DENTRO da pasta do Projeto (utilize Terminal no IntelliJ)

import java.util.Base64;
import java.io.FileOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class RunnerHandGUI {
    private MyTimer timer;
    private Run run;
    private KeyConfig keyConfig;
    private JPanel titlePanel;
    private JLabel timeLabel;
    private JLabel titleLabel;
    private JButton startPauseButton, splitButton, resetButton, configButton, settingsButton;
    private JTable splitsTable;
    private DefaultTableModel tableModel;
    private boolean isConfigured = false;
    private Image backgroundImage;
    private JPanel backgroundPanel;
    private boolean showComparisons = true;
    private Map<Integer, ImageIcon> splitIcons = new HashMap<>();
    private RunCounter runCounter;

    private final Color DARK_BG = new Color(30, 30, 30, 230);
    private final Color DARK_PANEL = new Color(45, 45, 45, 220);
    private final Color LIGHT_TEXT = new Color(230, 230, 230);
    private final Color ACCENT_COLOR = new Color(0, 150, 255);
    private final Color HIGHLIGHT_COLOR = new Color(0, 100, 200);
    private final Color GREEN_COLOR = new Color(0, 255, 128);
    private final Color RED_COLOR = new Color(255, 100, 100);
    private final Color GOLD_COLOR = new Color(255, 215, 0);
    private final Color TRANSPARENT_DARK = new Color(30, 30, 30, 120);
    private final Color TRANSPARENT_PANEL = new Color(45, 45, 45, 150);
    private final Color TRANSPARENT_BLACK = new Color(0, 0, 0, 100);

    public RunnerHandGUI() {
        timer = new MyTimer();
        run = new Run();
        keyConfig = new KeyConfig();
        runCounter = new RunCounter();

        setupGUI();
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        InputMap inputMap = backgroundPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = backgroundPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyConfig.getKeyCode("start_pause"), 0), "start_pause");
        actionMap.put("start_pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startPauseButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(keyConfig.getKeyCode("split"), 0), "split");
        actionMap.put("split", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (splitButton.isEnabled()) splitButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(keyConfig.getKeyCode("reset"), 0), "reset");
        actionMap.put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(keyConfig.getKeyCode("previous_split"), 0), "previous_split");
        actionMap.put("previous_split", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!timer.isRunning() && run.getCurrentSplitIndex() > 0) {
                    run.previousSplit();
                    updateTable();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(keyConfig.getKeyCode("skip_split"), 0), "skip_split");
        actionMap.put("skip_split", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer.isRunning() && run.getCurrentSplitIndex() < run.getSplits().size()) {
                    run.skipSplit();
                    int splitIndex = run.getCurrentSplitIndex() - 1;
                    if (splitIndex >= 0 && splitIndex < tableModel.getRowCount()) {
                        tableModel.setValueAt("SKIPPED", splitIndex, 3);
                    }
                    updateTable();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(keyConfig.getKeyCode("compare_prev"), 0), "compare_prev");
        actionMap.put("compare_prev", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showComparisons = !showComparisons;
                updateTable();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(keyConfig.getKeyCode("finish_run"), 0), "finish_run");
        actionMap.put("finish_run", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer.isRunning() && !run.isFinished()) {
                    handleFinishRun();
                }
            }
        });
    }

    private void incrementRunCounter() {
        String gameTitle = run.getRunTitle();
        runCounter.incrementCounter(gameTitle);
    }

    private void setupGUI() {
        JFrame frame = new JFrame("RunnerHand - Speedrun Timer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 800);
        frame.setLocationRelativeTo(null);

        backgroundPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(20, 20, 30),
                            0, getHeight(), new Color(40, 40, 60)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        frame.setContentPane(backgroundPanel);
        loadBackgroundImage();

        JPanel titlePanel = createTitlePanel();
        backgroundPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centerTopPanel = createTimerPanel();

        JPanel splitsPanel = createSplitsPanel();

        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setOpaque(false);
        centerContainer.add(centerTopPanel, BorderLayout.NORTH);
        centerContainer.add(splitsPanel, BorderLayout.CENTER);

        backgroundPanel.add(centerContainer, BorderLayout.CENTER);

        JPanel bottomPanel = createButtonPanel(frame);
        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        javax.swing.Timer uiTimer = new javax.swing.Timer(10, e -> updateDisplay());
        uiTimer.start();

        frame.setVisible(true);
    }

    private JPanel createTitlePanel() {
//        JPanel titlePanel = new JPanel(new BorderLayout(10, 10));

        titlePanel = new JPanel(new BorderLayout(10, 10));

        titlePanel.setBackground(new Color(0, 0, 0, 150));

        titlePanel.setOpaque(true);

        titlePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JButton editTitleButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ACCENT_COLOR);
                g2d.setStroke(new BasicStroke(2));

                int x = getWidth() / 2;
                int y = getHeight() / 2;

                g2d.drawLine(x - 8, y - 8, x + 6, y + 6);
                g2d.drawLine(x - 8, y - 8, x - 6, y - 4);
                g2d.drawLine(x - 6, y - 4, x - 2, y - 8);

                g2d.fillPolygon(
                        new int[]{x + 6, x + 10, x + 8},
                        new int[]{y + 6, y + 2, y + 4},
                        3
                );
            }
        };
        editTitleButton.setPreferredSize(new Dimension(30, 30));
        editTitleButton.setToolTipText("Editar título da run");
        styleIconButton(editTitleButton);
        editTitleButton.addActionListener(e -> changeRunTitle());

        titleLabel = new JLabel("Nova Run", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JButton aboutButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ACCENT_COLOR);
                g2d.setStroke(new BasicStroke(2));

                int x = getWidth() / 2;
                int y = getHeight() / 2;

                g2d.drawOval(x - 7, y - 9, 14, 14);

                g2d.drawLine(x, y + 2, x, y + 4);
                g2d.drawArc(x - 3, y - 5, 6, 4, 0, 180);
                g2d.drawLine(x, y - 7, x, y - 8);
            }
        };
        aboutButton.setPreferredSize(new Dimension(30, 30));
        aboutButton.setToolTipText("Sobre o desenvolvedor");
        styleIconButton(aboutButton);
        aboutButton.addActionListener(e -> showAboutDialog());

        titlePanel.add(editTitleButton, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(aboutButton, BorderLayout.EAST);

        JLabel counterLabel = new JLabel("Tentativa: 0");
        counterLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        counterLabel.setForeground(new Color(150, 200, 255));

        int count = runCounter.getCounter(run.getRunTitle());
        counterLabel.setText("Tentativa: " + (count + 1));

        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        counterPanel.setBackground(new Color(0, 0, 0, 0));
        counterPanel.add(counterLabel);

        titlePanel.add(counterPanel, BorderLayout.SOUTH);

        return titlePanel;
    }

    private void showAboutDialog() {
        JDialog aboutDialog = new JDialog();
        aboutDialog.setTitle("Sobre o RunnerHand");
        aboutDialog.setSize(500, 400);
        aboutDialog.setLayout(new BorderLayout(10, 10));
        aboutDialog.getContentPane().setBackground(DARK_BG);
        aboutDialog.setModal(true);
        aboutDialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("RunnerHand - Speedrun Timer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(ACCENT_COLOR);

        JLabel logoLabel = null;
        try {
            File logoFile = new File("src/resources/silenciopz_logo2.png");
            if (!logoFile.exists()) {
                logoFile = new File("silenciopz_logo2.png");
            }
            if (logoFile.exists()) {
                BufferedImage logoImg = ImageIO.read(logoFile);
                Image scaledLogo = logoImg.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(scaledLogo), SwingConstants.CENTER);
            }
        } catch (Exception e) {
            System.out.println("Logo não encontrada: " + e.getMessage());
        }

        if (logoLabel == null) {
            logoLabel = new JLabel("SILENCIOPZ", SwingConstants.CENTER);
            logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
            logoLabel.setForeground(ACCENT_COLOR);
        }

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        infoPanel.setBackground(DARK_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel devLabel = new JLabel("Desenvolvedor: SilencioPz", SwingConstants.CENTER);
        devLabel.setFont(new Font("Arial", Font.BOLD, 16));
        devLabel.setForeground(LIGHT_TEXT);

        JLabel versionLabel = new JLabel("Versão: 1.0", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        versionLabel.setForeground(LIGHT_TEXT);

        JLabel siteLabel = new JLabel("<html><center>Site: <a href='https://silenciopz.neocities.org/'>" +
                "https://silenciopz.neocities.org/</a></center></html>",
                SwingConstants.CENTER);
        siteLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        siteLabel.setForeground(new Color(100, 200, 255));
        siteLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        siteLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI("https://silenciopz.neocities.org/"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(aboutDialog,
                            "Não foi possível abrir o navegador.\nURL: https://silenciopz.neocities.org/",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        infoPanel.add(devLabel);
        infoPanel.add(versionLabel);
        infoPanel.add(siteLabel);

        JButton closeButton = createStyledButton("Fechar");
        closeButton.addActionListener(e -> aboutDialog.dispose());

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(logoLabel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(DARK_BG);
        bottomPanel.add(closeButton);

        aboutDialog.add(mainPanel, BorderLayout.CENTER);
        aboutDialog.add(bottomPanel, BorderLayout.SOUTH);
        aboutDialog.setVisible(true);
    }

    private JPanel createTimerPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setBackground(TRANSPARENT_BLACK);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        topPanel.setOpaque(false);

        JLabel logoLabel = ImageLoader.loadLogo("src/resources/silenciopz_logo2icon.png", 120, 0);
        if (logoLabel.getIcon() == null) {
            logoLabel.setText("<html><center>RUNNER<br>HAND</center></html>");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            logoLabel.setForeground(ACCENT_COLOR);
        }
        logoLabel.setOpaque(false);
        topPanel.add(logoLabel, BorderLayout.WEST);

        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setBackground(TRANSPARENT_PANEL);
        timePanel.setOpaque(true);
        timePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 3),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        timeLabel = new JLabel("00:00:00.00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 52));
        timeLabel.setForeground(LIGHT_TEXT);
        timeLabel.setOpaque(false);
        timePanel.add(timeLabel, BorderLayout.CENTER);

        topPanel.add(timePanel, BorderLayout.CENTER);

        return topPanel;
    }

    private JPanel createSplitsPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(TRANSPARENT_BLACK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        centerPanel.setOpaque(false);

        JLabel splitsLabel = new JLabel("SPLITS");
        splitsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        splitsLabel.setForeground(ACCENT_COLOR);
        splitsLabel.setOpaque(false);
        centerPanel.add(splitsLabel, BorderLayout.NORTH);

        String[] columns = {"", "#", "Nome", "Tempo", "Diferença", "Melhor"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? ImageIcon.class : Object.class;
            }
        };

        splitsTable = new JTable(tableModel);
        splitsTable.setBackground(TRANSPARENT_PANEL);
        splitsTable.setForeground(LIGHT_TEXT);
        splitsTable.setGridColor(new Color(60, 60, 60, 100));
        splitsTable.setSelectionBackground(new Color(0, 100, 200, 150));
        splitsTable.setFont(new Font("Monospaced", Font.BOLD, 13));
        splitsTable.setOpaque(true);

        splitsTable.getTableHeader().setBackground(new Color(50, 50, 50, 180));
        splitsTable.getTableHeader().setForeground(ACCENT_COLOR);
        splitsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        splitsTable.getTableHeader().setOpaque(true);
        splitsTable.setRowHeight(40);

        TableColumn iconColumn = splitsTable.getColumnModel().getColumn(0);
        iconColumn.setPreferredWidth(45);
        iconColumn.setMaxWidth(45);
        iconColumn.setMinWidth(45);

        splitsTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        splitsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        splitsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        splitsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        splitsTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        splitsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 2) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (column > 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                if (row == run.getCurrentSplitIndex() && timer.isRunning()) {
                    c.setBackground(new Color(70, 70, 100, 180));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(TRANSPARENT_PANEL);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                if (column == 4 && value != null && !value.toString().equals("--:--:--")) {
                    String diff = value.toString();
                    if (diff.startsWith("-")) {
                        c.setForeground(GREEN_COLOR);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (diff.startsWith("+")) {
                        c.setForeground(RED_COLOR);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(LIGHT_TEXT);
                    }
                } else if (column == 5) {
                    c.setForeground(GOLD_COLOR);
                } else {
                    c.setForeground(LIGHT_TEXT);
                }

                return c;
            }
        });

        iconColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                setHorizontalAlignment(CENTER);
                if (value instanceof ImageIcon) {
                    setIcon((ImageIcon) value);
                    setText("");
                } else {
                    setIcon(null);
                    setText("");
                }

                if (row == run.getCurrentSplitIndex() && timer.isRunning()) {
                    setBackground(new Color(70, 70, 100, 180));
                } else {
                    setBackground(TRANSPARENT_PANEL);
                }

                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(splitsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 2));
        scrollPane.getViewport().setBackground(TRANSPARENT_PANEL);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createButtonPanel(JFrame frame) {
        JPanel bottomPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        bottomPanel.setBackground(new Color(0, 0, 0, 150));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        bottomPanel.setOpaque(true);

        configButton = createStyledButton("Config");
        configButton.addActionListener(e -> openConfigDialog());

        settingsButton = createStyledButton("Teclas");
        settingsButton.addActionListener(e -> openKeyConfigDialog());

        JButton backgroundButton = createStyledButton("Fundo");
        backgroundButton.setToolTipText("Alterar imagem de fundo");
        backgroundButton.addActionListener(e -> changeBackgroundImage());

        startPauseButton = createStyledButton("Iniciar");
        startPauseButton.addActionListener(e -> handleStartPause(frame));

        splitButton = createStyledButton("Split");
        splitButton.setEnabled(false);
        splitButton.addActionListener(e -> handleSplit(frame));

        resetButton = createStyledButton("Reset");
        resetButton.addActionListener(e -> handleReset());

        JButton saveButton = createStyledButton("Salvar");
        saveButton.addActionListener(e -> saveRunToFile());

        JButton finishButton = createStyledButton("Finalizar");
        finishButton.addActionListener(e -> handleFinishRun());

        JButton htmlButton = createStyledButton("Salvar HTML");
        htmlButton.addActionListener(e -> saveRunToHTML());

        bottomPanel.add(configButton);
        bottomPanel.add(settingsButton);
        bottomPanel.add(backgroundButton);
        bottomPanel.add(startPauseButton);
        bottomPanel.add(splitButton);
        bottomPanel.add(resetButton);
        bottomPanel.add(finishButton);
        bottomPanel.add(htmlButton);
//        bottomPanel.add(saveButton);

        return bottomPanel;
    }

    private void handleStartPause(JFrame frame) {
        if (!isConfigured && run.getSplits().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Configure os splits primeiro!",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!timer.isRunning()) {
            timer.start();
            startPauseButton.setText("Pausar");
            splitButton.setEnabled(true);
            configButton.setEnabled(false);
            settingsButton.setEnabled(false);
        } else {
            timer.pause();
            startPauseButton.setText("Continuar");
            splitButton.setEnabled(false);
        }
    }

    private void handleFinishRun() {
        if (timer.isRunning() && !run.isFinished()) {
            long currentTime = timer.getCurrentTime();
            while (!run.isFinished()) {
                run.nextSplit(currentTime);
            }

            timer.pause();
            startPauseButton.setText("Finalizada");
            startPauseButton.setEnabled(false);
            splitButton.setEnabled(false);
            updateTable();

            saveRunToFile();

            incrementRunCounter();

            JOptionPane.showMessageDialog(backgroundPanel,
                    "RUN FINALIZADA!\n\nTempo Final: " + formatTimeDetailed(currentTime),
                    "Run Finalizada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleSplit(JFrame frame) {
        if (timer.isRunning() && !run.isFinished()) {
            long currentTime = timer.getCurrentTime();
            run.nextSplit(currentTime);
            updateTable();

            if (run.isFinished()) {
                splitButton.setEnabled(false);
                startPauseButton.setEnabled(false);
                timer.pause();
                saveRunToFile();

                JOptionPane.showMessageDialog(frame,
                        "RUN CONCLUÍDA!\n\nTempo Final: " + formatTimeDetailed(currentTime),
                        "Parabéns!",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void handleReset() {
        timer.reset();

        java.util.List<Split> currentSplits = new java.util.ArrayList<>();
        for (Split split : run.getSplits()) {
            Split newSplit = new Split(split.getName());
            newSplit.setImagePath(split.getImagePath());
            currentSplits.add(newSplit);
        }

        String currentTitle = run.getRunTitle();
        run = new Run(currentTitle);

        for (int i = 0; i < currentSplits.size(); i++) {
            Split split = currentSplits.get(i);
            run.addSplit(split.getName(), split.getImagePath());
        }

        startPauseButton.setText("Iniciar");
        startPauseButton.setEnabled(true);
        splitButton.setEnabled(false);
        configButton.setEnabled(true);
        settingsButton.setEnabled(true);

        updateTable();
        updateDisplay();
    }

    private void updateTable() {
        tableModel.setRowCount(0);

        for (int i = 0; i < run.getSplits().size(); i++) {
            Split split = run.getSplits().get(i);
            String splitTime = "--:--:--";
            String difference = "--:--:--";
            String bestTime = "--:--:--";

            if (split.getSplitTime() > 0) {
                long partialTime = run.getPartialTime(i);
                if (partialTime > 0) {
                    splitTime = formatTimeDetailed(partialTime);

                    long bestPartial = run.getBestTime(i);
                    if (bestPartial > 0 && showComparisons) {
                        long diff = partialTime - bestPartial;
                        if (diff > 0) {
                            difference = "+" + formatTimeDetailed(diff);
                        } else if (diff < 0) {
                            difference = "-" + formatTimeDetailed(Math.abs(diff));
                        } else {
                            difference = "±0.00";
                        }
                    }

                    bestTime = bestPartial > 0 ? formatTimeDetailed(bestPartial) : "--:--:--";
                }
            }

            ImageIcon icon = splitIcons.get(i);
            Object[] row = {icon, i + 1, split.getName(), splitTime, difference, bestTime};
            tableModel.addRow(row);
        }

        if (run.getCurrentSplitIndex() < splitsTable.getRowCount()) {
            splitsTable.setRowSelectionInterval(run.getCurrentSplitIndex(), run.getCurrentSplitIndex());
        }
    }

    private void changeRunTitle() {
        String newTitle = JOptionPane.showInputDialog(
                backgroundPanel,
                "Digite o título da run:",
                run.getRunTitle()
        );

        if (newTitle != null && !newTitle.trim().isEmpty()) {
            run.setRunTitle(newTitle.trim());
            titleLabel.setText(newTitle.trim());

            int count = runCounter.getCounter(newTitle.trim());

            updateCounterDisplay();
        }
    }

    private void updateCounterDisplay() {
        if (titlePanel != null) {
            Component[] comps = titlePanel.getComponents();
            for (Component comp : comps) {
                if (comp instanceof JPanel) {
                    Component[] subComps = ((JPanel) comp).getComponents();
                    for (Component subComp : subComps) {
                        if (subComp instanceof JLabel && ((JLabel) subComp).getText().startsWith("Tentativa:")) {
                            int count = runCounter.getCounter(run.getRunTitle());
                            ((JLabel) subComp).setText("Tentativa: " + (count + 1));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void loadBackgroundImage() {
        try {
            File bgFile = new File("src/resources/background.jpg");
            if (!bgFile.exists()) {
                bgFile = new File("background.jpg");
            }
            if (bgFile.exists()) {
                backgroundImage = ImageIO.read(bgFile);
            }
        } catch (Exception e) {
            System.out.println("Não foi possível carregar imagem de fundo: " + e.getMessage());
        }
    }

    private void changeBackgroundImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".jpg") ||
                        f.getName().toLowerCase().endsWith(".jpeg") ||
                        f.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "Imagens (JPG, PNG)";
            }
        });

        if (fileChooser.showOpenDialog(backgroundPanel) == JFileChooser.APPROVE_OPTION) {
            try {
                backgroundImage = ImageIO.read(fileChooser.getSelectedFile());
                backgroundPanel.repaint();

                File dest = new File("src/resources/background.jpg");
                dest.getParentFile().mkdirs();
                ImageIO.write((BufferedImage) backgroundImage, "jpg", dest);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(backgroundPanel,
                        "Erro ao carregar imagem: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ImageIcon loadAndResizeIcon(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            Image scaled = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    private void openKeyConfigDialog() {
        JDialog keyDialog = new JDialog();
        keyDialog.setTitle("Configurar Teclas de Atalho");
        keyDialog.setSize(450, 450);
        keyDialog.setLayout(new BorderLayout(10, 10));
        keyDialog.getContentPane().setBackground(DARK_BG);
        keyDialog.setModal(true);
        keyDialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabelDialog = new JLabel("Clique no campo e pressione a tecla desejada", SwingConstants.CENTER);
        titleLabelDialog.setForeground(ACCENT_COLOR);
        titleLabelDialog.setFont(new Font("Arial", Font.BOLD, 14));

        String[] actions = {
                "Iniciar/Pausar", "Split", "Reset",
                "Split Anterior", "Pular Split", "Comparar Anterior"
        };

        String[] keys = {
                "start_pause", "split", "reset",
                "previous_split", "skip_split", "compare_prev"
        };

        JPanel gridPanel = new JPanel(new GridLayout(actions.length, 2, 15, 15));
        gridPanel.setBackground(DARK_BG);

        JTextField[] keyFields = new JTextField[actions.length];

        for (int i = 0; i < actions.length; i++) {
            JLabel actionLabel = new JLabel(actions[i]);
            actionLabel.setForeground(LIGHT_TEXT);
            actionLabel.setFont(new Font("Arial", Font.BOLD, 13));
            gridPanel.add(actionLabel);

            keyFields[i] = new JTextField(KeyConfig.getKeyName(keyConfig.getKeyCode(keys[i])));
            keyFields[i].setEditable(false);
            keyFields[i].setBackground(new Color(60, 60, 60));
            keyFields[i].setForeground(ACCENT_COLOR);
            keyFields[i].setFont(new Font("Monospaced", Font.BOLD, 12));
            keyFields[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));

            final int index = i;
            final String key = keys[i];
            keyFields[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    keyFields[index].setText("Pressione...");
                    keyFields[index].setForeground(GREEN_COLOR);
                    keyFields[index].addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            keyConfig.setKeyCode(key, e.getKeyCode());
                            keyFields[index].setText(KeyConfig.getKeyName(e.getKeyCode()));
                            keyFields[index].setForeground(ACCENT_COLOR);
                            keyFields[index].removeKeyListener(this);
                            setupKeyBindings();
                        }
                    });
                }
            });

            gridPanel.add(keyFields[i]);
        }

        mainPanel.add(titleLabelDialog, BorderLayout.NORTH);
        mainPanel.add(gridPanel, BorderLayout.CENTER);

        JButton closeButton = createStyledButton("Fechar");
        closeButton.addActionListener(e -> keyDialog.dispose());

        mainPanel.add(closeButton, BorderLayout.SOUTH);
        keyDialog.add(mainPanel);
        keyDialog.setVisible(true);
    }

    private void saveRunToHTML() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(run.getRunTitle() + "_" +
                System.currentTimeMillis() + ".html"));

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".html");
            }

            @Override
            public String getDescription() {
                return "Arquivos HTML (*.html)";
            }
        });

        if (fileChooser.showSaveDialog(backgroundPanel) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".html")) {
                    filePath += ".html";
                }
                run.saveRunHTML(filePath);
                JOptionPane.showMessageDialog(backgroundPanel,
                        "Run salva em HTML com sucesso!\n\n" +
                                "O arquivo contém as imagens EMBEDDADAS e pode ser aberto em:\n" +
                                "• Qualquer navegador (Chrome, Firefox, etc.)\n" +
                                "• LibreOffice Writer\n" +
                                "• Microsoft Word",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(backgroundPanel,
                        "Erro ao salvar run HTML: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Modificar o método saveRunToFile() para incluir HTML:
    private void saveRunToFile() {
        Object[] options = {"Texto (.txt)", "HTML (.html)", "Ambos", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(backgroundPanel,
                "Escolha o formato para salvar:\n\n" +
                        "• HTML: Salva com imagens EMBEDDADAS\n" +
                        "• Texto: Formato simples sem imagens",
                "Salvar Run",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) { // TXT
            saveRunToTXT();
        } else if (choice == 1) { // HTML
            saveRunToHTML();
        } else if (choice == 2) { // Ambos
            saveRunToTXT();
            saveRunToHTML();
        }
        // choice == 3 é Cancelar
    }

    private void saveRunToTXT() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(run.getRunTitle() + "_" +
                System.currentTimeMillis() + ".txt"));

        if (fileChooser.showSaveDialog(backgroundPanel) == JFileChooser.APPROVE_OPTION) {
            try {
                run.saveRun(fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(backgroundPanel,
                        "Run salva em TXT com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(backgroundPanel,
                        "Erro ao salvar run: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void styleIconButton(JButton button) {
        button.setBackground(new Color(0, 0, 0, 0));
        button.setForeground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(GREEN_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GREEN_COLOR, 1),
                        BorderFactory.createEmptyBorder(4, 9, 4, 9)
                ));
                button.repaint();
            }
            public void mouseExited(MouseEvent evt) {
                button.setForeground(ACCENT_COLOR);
                button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                button.repaint();
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DARK_PANEL);
        button.setForeground(LIGHT_TEXT);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(HIGHLIGHT_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GREEN_COLOR, 2),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(DARK_PANEL);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });

        return button;
    }

    private void openConfigDialog() {
        JDialog configDialog = new JDialog();
        configDialog.setTitle("Configurar Splits");
        configDialog.setSize(600, 650);
        configDialog.setLayout(new BorderLayout(10, 10));
        configDialog.getContentPane().setBackground(DARK_BG);
        configDialog.setModal(true);
        configDialog.setLocationRelativeTo(null);

        JPanel configPanel = new JPanel(new BorderLayout(10, 10));
        configPanel.setBackground(DARK_BG);
        configPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(DARK_PANEL);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                "Adicionar Novo Split",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13),
                ACCENT_COLOR
        ));

        JPanel topInputPanel = new JPanel(new BorderLayout(10, 10));
        topInputPanel.setBackground(DARK_PANEL);

        JTextField splitNameField = new JTextField();
        splitNameField.setBackground(new Color(60, 60, 60));
        splitNameField.setForeground(LIGHT_TEXT);
        splitNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        splitNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JButton addButton = createStyledButton("Adicionar");

        JPanel namePanel = new JPanel(new BorderLayout(5, 0));
        namePanel.setBackground(DARK_PANEL);
        JLabel nameLabel = new JLabel("Nome:");
        nameLabel.setForeground(LIGHT_TEXT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(splitNameField, BorderLayout.CENTER);
        namePanel.add(addButton, BorderLayout.EAST);

        topInputPanel.add(namePanel, BorderLayout.NORTH);

        JPanel imagePanel = new JPanel(new BorderLayout(10, 0));
        imagePanel.setBackground(DARK_PANEL);
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel imageLabel = new JLabel("Imagem:");
        imageLabel.setForeground(LIGHT_TEXT);
        imageLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JTextField imagePathField = new JTextField();
        imagePathField.setEditable(false);
        imagePathField.setBackground(new Color(60, 60, 60));
        imagePathField.setForeground(new Color(150, 150, 150));
        imagePathField.setFont(new Font("Arial", Font.PLAIN, 11));
        imagePathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        JButton selectImageButton = createStyledButton("Selecionar");
        selectImageButton.addActionListener(e -> {
            JFileChooser imgChooser = new JFileChooser();
            imgChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() ||
                            f.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$");
                }
                @Override
                public String getDescription() {
                    return "Imagens (JPG, PNG, GIF)";
                }
            });

            if (imgChooser.showOpenDialog(configDialog) == JFileChooser.APPROVE_OPTION) {
                imagePathField.setText(imgChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton loadHTMLButton = createStyledButton("Carregar HTML");
        loadHTMLButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() ||
                            f.getName().toLowerCase().endsWith(".html") ||
                            f.getName().toLowerCase().endsWith(".htm");
                }
                @Override
                public String getDescription() {
                    return "Arquivos HTML (*.html, *.htm)";
                }
            });

            if (fileChooser.showOpenDialog(configDialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    loadRunFromHTML(fileChooser.getSelectedFile().getAbsolutePath());
                    configDialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(configDialog,
                            "Erro ao carregar HTML: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        imagePanel.add(imageLabel, BorderLayout.WEST);
        imagePanel.add(imagePathField, BorderLayout.CENTER);
        imagePanel.add(selectImageButton, BorderLayout.EAST);

        topInputPanel.add(imagePanel, BorderLayout.CENTER);
        inputPanel.add(topInputPanel, BorderLayout.NORTH);

        DefaultListModel<SplitItem> listModel = new DefaultListModel<>();
        JList<SplitItem> splitsList = new JList<>(listModel);
        splitsList.setBackground(DARK_PANEL);
        splitsList.setForeground(LIGHT_TEXT);
        splitsList.setFont(new Font("Arial", Font.PLAIN, 13));
        splitsList.setSelectionBackground(HIGHLIGHT_COLOR);
        splitsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SplitItem) {
                    SplitItem item = (SplitItem) value;
                    label.setText(item.name);
                    if (item.iconPath != null) {
                        ImageIcon icon = loadAndResizeIcon(item.iconPath);
                        if (icon != null) {
                            label.setIcon(icon);
                        }
                    }
                }
                return label;
            }
        });

        JScrollPane listScroll = new JScrollPane(splitsList);
        listScroll.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        inputPanel.add(listScroll, BorderLayout.CENTER);

        JPanel listControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        listControls.setBackground(DARK_PANEL);

        JButton removeButton = createStyledButton("Remover");
        JButton clearButton = createStyledButton("Limpar");
        JButton loadButton = createStyledButton("Carregar");

        listControls.add(removeButton);
        listControls.add(clearButton);
        listControls.add(loadButton);
        listControls.add(loadHTMLButton);

        inputPanel.add(listControls, BorderLayout.SOUTH);
        configPanel.add(inputPanel, BorderLayout.CENTER);

        JButton saveButton = createStyledButton("Salvar Configuração");
        configPanel.add(saveButton, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            String name = splitNameField.getText().trim();
            if (!name.isEmpty()) {
                String iconPath = imagePathField.getText().trim();
                listModel.addElement(new SplitItem(name, iconPath.isEmpty() ? null : iconPath));
                splitNameField.setText("");
                imagePathField.setText("");
                splitNameField.requestFocus();
            }
        });

        splitNameField.addActionListener(e -> addButton.doClick());

        removeButton.addActionListener(e -> {
            int selected = splitsList.getSelectedIndex();
            if (selected != -1) {
                listModel.remove(selected);
            }
        });

        clearButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(configDialog,
                    "Deseja realmente limpar todos os splits?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                listModel.clear();
            }
        });

        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(configDialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                    listModel.clear();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty() && !line.startsWith("Run Title:") &&
                                !line.startsWith("Total Time:") && !line.startsWith("SPLITS:") &&
                                !line.startsWith("BEST SPLITS:")) {
                            String splitName = line.trim().replaceAll("^\\d+\\.\\s*", "").split("-")[0].trim();
                            if (!splitName.isEmpty()) {
                                listModel.addElement(new SplitItem(splitName, null));
                            }
                        }
                    }
                    reader.close();
                    JOptionPane.showMessageDialog(configDialog,
                            "Splits carregados com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(configDialog,
                            "Erro ao carregar arquivo: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        saveButton.addActionListener(e -> {
            if (listModel.size() > 0) {
                String currentTitle = run.getRunTitle();
                run = new Run(currentTitle);
                splitIcons.clear();
                tableModel.setRowCount(0);

                for (int i = 0; i < listModel.size(); i++) {
                    SplitItem item = listModel.getElementAt(i);
                    run.addSplit(item.name, item.iconPath);

                    if (item.iconPath != null && !item.iconPath.isEmpty()) {
                        Split currentSplit = run.getSplits().get(i);
                        currentSplit.setImagePath(item.iconPath);

                        ImageIcon icon = loadAndResizeIcon(item.iconPath);
                        if (icon != null) {
                            splitIcons.put(i, icon);
                        }
                    }
                }

                isConfigured = true;
                configDialog.dispose();
                updateTable();

                JOptionPane.showMessageDialog(configDialog,
                        listModel.size() + " splits configurados com sucesso!",
                        "Configuração Salva",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(configDialog,
                        "Adicione pelo menos um split!",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        configDialog.add(configPanel);
        configDialog.setVisible(true);
    }

    private void loadRunFromHTML(String filePath) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            htmlContent.append(line).append("\n");
        }
        reader.close();

        String html = htmlContent.toString();

        String runTitle = "Run Carregada";
        int titleStart = html.indexOf("<h2>");
        if (titleStart != -1) {
            int titleEnd = html.indexOf("</h2>", titleStart);
            if (titleEnd != -1) {
                runTitle = html.substring(titleStart + 4, titleEnd).trim();
            }
        }

        run = new Run(runTitle);
        splitIcons.clear();
        tableModel.setRowCount(0);

        int tableStart = html.indexOf("<table>");
        if (tableStart != -1) {
            int tableEnd = html.indexOf("</table>", tableStart);
            if (tableEnd != -1) {
                String tableHtml = html.substring(tableStart, tableEnd);

                String[] rows = tableHtml.split("<tr>");

                int splitIndex = 0;

                for (String row : rows) {
                    if (row.contains("<td>") && !row.contains("<th>")) {
                        int nameStart = row.indexOf("<strong>");
                        if (nameStart != -1) {
                            int nameEnd = row.indexOf("</strong>", nameStart);
                            if (nameEnd != -1) {
                                String splitName = row.substring(nameStart + 8, nameEnd).trim();
                                String imagePath = null;

                                int imgStart = row.indexOf("src='data:image");
                                if (imgStart != -1) {
                                    int imgEnd = row.indexOf("'", imgStart + 5);
                                    if (imgEnd != -1) {
                                        String base64Data = row.substring(imgStart + 5, imgEnd);

                                        String[] parts = base64Data.split(",");
                                        if (parts.length == 2) {
                                            String imageType = "jpeg"; // padrão
                                            if (parts[0].contains("jpeg")) imageType = "jpg";
                                            else if (parts[0].contains("png")) imageType = "png";

                                            byte[] imageBytes = java.util.Base64.getDecoder().decode(parts[1]);
                                            String tempImagePath = "temp_split_" + splitIndex + "." + imageType;
                                            File tempFile = new File("src/resources/" + tempImagePath);
                                            tempFile.getParentFile().mkdirs();

                                            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                                                fos.write(imageBytes);
                                                imagePath = tempFile.getAbsolutePath();
                                            }
                                        }
                                    }
                                }

                                run.addSplit(splitName, imagePath);

                                if (imagePath != null) {
                                    ImageIcon icon = loadAndResizeIcon(imagePath);
                                    if (icon != null) {
                                        splitIcons.put(splitIndex, icon);
                                    }
                                }

                                splitIndex++;
                            }
                        }
                    }
                }
            }
        }

        isConfigured = true;
        updateTable();
        titleLabel.setText(runTitle);

        JOptionPane.showMessageDialog(backgroundPanel,
                "Run carregada do HTML com sucesso!\n" +
                        "Foram carregados " + run.getSplits().size() + " splits.",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateDisplay() {
        long currentTime = timer.getCurrentTime();
        String timeString = formatTimeDetailed(currentTime);

        String[] parts = timeString.split("\\.");
        if (parts.length == 2) {
            String htmlTime = String.format(
                    "<html><span style='font-size: 52px;'>%s.</span><span style='color: #00ff88; font-size: 36px;'>%s</span></html>",
                    parts[0], parts[1]
            );
            timeLabel.setText(htmlTime);
        } else {
            timeLabel.setText(timeString);
        }
    }

    private String formatTimeDetailed(long milliseconds) {
        long totalCentiseconds = milliseconds / 10;
        long hours = totalCentiseconds / 360000;
        long minutes = (totalCentiseconds % 360000) / 6000;
        long seconds = (totalCentiseconds % 6000) / 100;
        long centiseconds = totalCentiseconds % 100;

        return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds);
    }

    private static class SplitItem {
        String name;
        String iconPath;

        SplitItem(String name, String iconPath) {
            this.name = name;
            this.iconPath = iconPath;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RunnerHandGUI());
    }
}