//comando para gerar JAR: javac -d bin **/*.java DENTRO da pasta do Projeto (utilize Terminal no IntelliJ)

import java.awt.List;
import java.util.*;
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
    private GlobalKeyManager globalKeyManager;
    private JLabel counterLabel;

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
        setupGlobalHotkeys();

        showDefaultKeysInfo();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (globalKeyManager != null) {
                globalKeyManager.cleanup();
            }
        }));
    }

    private void showDefaultKeysInfo() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    "<html><div style='text-align: center;'><b>RUNNERHAND - TECLAS PADRÃO</b><br><br>" +
                            "1 = Iniciar/Pausar<br>" +
                            "2 = Split<br>" +
                            "3 = Reset<br>" +
                            "4 = Split Anterior<br>" +
                            "5 = Pular Split<br>" +
                            "6 = Comparar (liga/desliga)<br>" +
                            "7 = Finalizar Run<br><br>" +
                            "<i>Configure outras teclas em 'Teclas'</i><br>" +
                            "<i>Hotkeys funcionam em segundo plano!</i></div></html>",
                    "Teclas de Atalho",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private void  setupGlobalHotkeys() {
        globalKeyManager = GlobalKeyManager.getInstance();

        globalKeyManager.registerAction("start_pause", () -> {
            if (startPauseButton != null && startPauseButton.isEnabled()) {
                startPauseButton.doClick();
            }
        });

        globalKeyManager.registerAction("split", () -> {
            if (splitButton != null && splitButton.isEnabled()) {
                splitButton.doClick();
            }
        });

        globalKeyManager.registerAction("reset", () -> {
            if (resetButton != null && resetButton.isEnabled()) {
                resetButton.doClick();
            }
        });

        globalKeyManager.registerAction("previous_split", () -> {
            if (!timer.isRunning() && run.getCurrentSplitIndex() > 0) {
                run.previousSplit();
                updateTable();
            }
        });

        globalKeyManager.registerAction("skip_split", () -> {
            if (timer.isRunning() && run.getCurrentSplitIndex() < run.getSplits().size()) {
                run.skipSplit();
                int splitIndex = run.getCurrentSplitIndex() - 1;
                if (splitIndex >= 0 && splitIndex < tableModel.getRowCount()) {
                    tableModel.setValueAt("SKIPPED", splitIndex, 3);
                }
                updateTable();
            }
        });

        globalKeyManager.registerAction("compare_prev", () -> {
            showComparisons = !showComparisons;
            updateTable();
        });

        globalKeyManager.registerAction("finish_run", () -> {
            if (timer.isRunning() && !run.isFinished()) {
                handleFinishRun();
            }
        });

        globalKeyManager.startListening();

        addHotkeyToggleToUI();
    }

    private void addHotkeyToggleToUI() {
        JCheckBox globalHotkeyCheckbox = new JCheckBox("Hotkeys Globais Ativas", true);
        globalHotkeyCheckbox.setForeground(LIGHT_TEXT);
        globalHotkeyCheckbox.setBackground(DARK_PANEL);
        globalHotkeyCheckbox.setFocusable(false);
        globalHotkeyCheckbox.setFont(new Font("Arial", Font.BOLD, 12));

        globalHotkeyCheckbox.setToolTipText("Ativa/desativa teclas de atalho globais (funcionam mesmo com" +
                " janela inativa)");

        globalHotkeyCheckbox.addActionListener(e -> {
            if (globalKeyManager != null) {
                globalKeyManager.setEnabled(globalHotkeyCheckbox.isSelected());
                String status = globalHotkeyCheckbox.isSelected() ? "ATIVADAS" : "DESATIVADAS";

                JDialog messageDialog = new JDialog();
                messageDialog.setTitle("Hotkeys Globais");
                messageDialog.setSize(400, 150);
                messageDialog.setLocationRelativeTo(backgroundPanel);
                messageDialog.setModal(true);
                messageDialog.setLayout(new BorderLayout());

                JPanel msgPanel = new JPanel(new BorderLayout(10, 10));
                msgPanel.setBackground(DARK_BG);
                msgPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JLabel msgLabel = new JLabel(
                        "<html><center><b>Hotkeys globais " + status + "</b><br><br>" +
                                "As teclas de atalho agora funcionam<br>" +
                                "mesmo com o jogo em primeiro plano.</center></html>",
                        SwingConstants.CENTER
                );
                msgLabel.setForeground(LIGHT_TEXT);
                msgLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                JButton okButton = new JButton("OK");
                okButton.addActionListener(ev -> messageDialog.dispose());
                styleIconButton(okButton);

                msgPanel.add(msgLabel, BorderLayout.CENTER);
                msgPanel.add(okButton, BorderLayout.SOUTH);

                messageDialog.add(msgPanel);
                messageDialog.setVisible(true);
            }
        });

        JPanel hotkeyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hotkeyPanel.setBackground(new Color(0, 0, 0, 100));
        hotkeyPanel.setOpaque(true);
        hotkeyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));
        hotkeyPanel.add(globalHotkeyCheckbox);

        Component[] comps = backgroundPanel.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getLayout() instanceof BorderLayout) {
                    Component centerComp = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    if (centerComp instanceof JPanel) {
                        JPanel centerPanel = (JPanel) centerComp;

                        centerPanel.add(hotkeyPanel, BorderLayout.NORTH);
                        centerPanel.revalidate();
                        break;
                    }
                }
            }
        }
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

    private void loadPreviousRunData() {
        try {
            String fileName = run.getRunTitle().replaceAll("[^a-zA-Z0-9]", "_");
            File dir = new File(".");

            File[] files = dir.listFiles((d, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.startsWith(fileName.toLowerCase()) &&
                        (lowerName.endsWith(".txt") || lowerName.endsWith(".html") || lowerName.endsWith(".htm"));
            });

            if (files != null && files.length > 0) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                String filePath = files[0].getAbsolutePath();

                if (filePath.toLowerCase().endsWith(".html") || filePath.toLowerCase().endsWith(".htm")) {
                    loadPreviousRunFromHTML(filePath);
                } else {
                    BufferedReader reader = new BufferedReader(new FileReader(filePath));
                    String line;
                    int splitIndex = 0;
                    boolean readingSplits = false;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();

                        if (line.equals("SPLITS:")) {
                            readingSplits = true;
                            continue;
                        }

                        if (line.equals("BEST SPLITS:")) {
                            break;
                        }

                        if (readingSplits && line.contains("Partial:") && splitIndex < run.getPreviousRunSplits().size()) {
                            String partialStr = line.substring(line.indexOf("Partial:") + 9).trim();
                            partialStr = partialStr.replace(")", "").trim();

                            if (!partialStr.equals("--:--:--")) {
                                long partialTime = parseTimeFromString(partialStr);
                                run.setPreviousTime(splitIndex, partialTime);
                                System.out.println("✓ Tempo anterior carregado [Split " + (splitIndex+1) + "]: " +
                                        formatTimeDetailed(partialTime));
                            }
                            splitIndex++;
                        }
                    }
                    reader.close();
                }

                SwingUtilities.invokeLater(() -> {
                    updateTable();
                    System.out.println("✓ Tempos anteriores carregados de: " + files[0].getName());
                });
            } else {
                System.out.println("ℹ Nenhum arquivo anterior encontrado para: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("⚠ Não foi possível carregar tempos anteriores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private long parseTimeFromString(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            if (parts.length != 3) return 0;

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            String[] secParts = parts[2].split("\\.");
            int seconds = Integer.parseInt(secParts[0]);
            int millis = secParts.length > 1 ? Integer.parseInt(secParts[1]) : 0;

            return (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L) + millis;
        } catch (Exception e) {
            System.err.println("Erro ao converter tempo: " + timeStr);
            return 0;
        }
    }

    private void loadPreviousRunFromHTML(String filePath) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            htmlContent.append(line).append("\n");
        }
        reader.close();

        String html = htmlContent.toString();

        int tableStart = html.indexOf("<table>");
        if (tableStart != -1) {
            int tableEnd = html.indexOf("</table>", tableStart);
            if (tableEnd != -1) {
                String tableHtml = html.substring(tableStart, tableEnd);
                String[] rows = tableHtml.split("<tr>");

                int splitIndex = 0;

                for (String row : rows) {
                    if (row.contains("<td>") && !row.contains("<th>")) {
                        String[] cells = row.split("</td>");

                        if (cells.length > 4 && splitIndex < run.getPreviousRunSplits().size()) {
                            String partialCell = cells[4];
                            int tdStart = partialCell.lastIndexOf("<td>");
                            if (tdStart != -1) {
                                String timeStr = partialCell.substring(tdStart + 4).trim();
                                if (!timeStr.equals("--:--:--") && !timeStr.isEmpty()) {
                                    long partialTime = parseTimeFromHTML(timeStr);
                                    run.setPreviousTime(splitIndex, partialTime);
                                }
                            }
                        }
                        splitIndex++;
                    }
                }
            }
        }
    }

    private JPanel createTitlePanel() {

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

        counterLabel = new JLabel("Tentativa: 1");
        counterLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        counterLabel.setForeground(new Color(150, 200, 255));
        counterLabel.setOpaque(true);
        counterLabel.setBackground(new Color(0, 0, 0, 0));

        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        counterPanel.setOpaque(false);
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

        JLabel versionLabel = new JLabel("Versão: 1.0.2", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        versionLabel.setForeground(LIGHT_TEXT);

        JLabel siteLabel = new JLabel("<html><center>Site: <a href='' style='color: #FFFFFF;'>" +
                "https://silenciopz.neocities.org/</a></center></html>",
                SwingConstants.CENTER);
        siteLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        siteLabel.setForeground(new Color(240, 240, 240));
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

        JLabel logoLabel = ImageLoader.loadLogo("src/resources/silenciopz_logo2icon.png",
                120, 0);
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

        String[] columns = {"", "#", "Nome", "Tempo", "Diferença", "Melhor", "Anterior"};
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
        splitsTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        splitsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                setOpaque(true);
                setIcon(null);

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
                    setFont(getFont().deriveFont(Font.BOLD));
                }

                else if (column == 6) {
                    setForeground(new Color(150, 200, 255));
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                else {
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

//        JButton saveButton = createStyledButton("Salvar TXT");
//        saveButton.addActionListener(e -> saveRunToFile());

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
//        bottomPanel.add(saveButton);
        bottomPanel.add(htmlButton);

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

//            incrementRunCounter();

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

        if (run.getCurrentSplitIndex() > 0 ||
                run.getSplits().stream().anyMatch(s -> s.getSplitTime() > 0)) {
            runCounter.incrementCounter(run.getRunTitle());
        }

        java.util.List<Split> currentSplits = new java.util.ArrayList<>();
        for (Split split : run.getSplits()) {
            Split newSplit = new Split(split.getName());
            newSplit.setImagePath(split.getImagePath());
            currentSplits.add(newSplit);
        }

        java.util.List<Long> savedPreviousRuns = new java.util.ArrayList<>(run.getPreviousRunSplits());
        java.util.List<Long> savedBestSplits = new java.util.ArrayList<>(run.getBestSplits());

        String currentTitle = run.getRunTitle();
        run = new Run(currentTitle);

        for (int i = 0; i < currentSplits.size(); i++) {
            Split split = currentSplits.get(i);
            run.addSplit(split.getName(), split.getImagePath());
        }

        for (int i = 0; i < Math.min(savedPreviousRuns.size(), run.getPreviousRunSplits().size()); i++) {
            run.setPreviousTime(i, savedPreviousRuns.get(i));
        }

        for (int i = 0; i < Math.min(savedBestSplits.size(), run.getBestSplits().size()); i++) {
            run.getBestSplits().set(i, savedBestSplits.get(i));
        }

        startPauseButton.setText("Iniciar");
        startPauseButton.setEnabled(true);
        splitButton.setEnabled(false);
        configButton.setEnabled(true);
        settingsButton.setEnabled(true);

        updateTable();
        updateDisplay();
        updateCounterDisplay();
    }

    private void updateTable() {
        splitsTable.setEnabled(false);
        tableModel.setRowCount(0);

        long currentRunTotal = 0;

        for (int i = 0; i < run.getSplits().size(); i++) {
            Split split = run.getSplits().get(i);
            String splitTime = "--:--:--";
            String difference = "--:--:--";
            String bestTime = "--:--:--";
            String previousTime = "--:--:--";

            if (split.getSplitTime() > 0) {
                long partialTime = run.getPartialTime(i);
                if (partialTime > 0) {
                    splitTime = formatTimeDetailed(partialTime);
                    currentRunTotal = split.getSplitTime();

                    long previousPartial = run.getPreviousTime(i);
                    if (previousPartial > 0 && showComparisons) {
                        long diff = partialTime - previousPartial;
                        if (diff > 0) {
                            difference = "+" + formatTimeDetailed(diff);
                        } else if (diff < 0) {
                            difference = "-" + formatTimeDetailed(Math.abs(diff));
                        } else {
                            difference = "±0.00";
                        }
                    }

                    long bestPartial = run.getBestTime(i);
                    if (bestPartial > 0 && partialTime <= bestPartial) {
                        bestTime = formatTimeDetailed(bestPartial);
                    } else {
                        bestTime = "--:--:--";
                    }
                }
            }

            long previousPartial = run.getPreviousTime(i);
            if (previousPartial > 0) {
                previousTime = formatTimeDetailed(previousPartial);
            }

            ImageIcon icon = splitIcons.get(i);
            Object[] row = {icon, i + 1, split.getName(), splitTime,
                    difference, bestTime, previousTime};
            tableModel.addRow(row);
        }

        long totalPB = 0;
        boolean hasPB = false;
        for (int i = 0; i < run.getPreviousRunSplits().size(); i++) {
            long prevTime = run.getPreviousTime(i);
            if (prevTime > 0) {
                totalPB += prevTime;
                hasPB = true;
            }
        }

        long savedPB = loadSavedPB();
        if (savedPB > 0 && (totalPB == 0 || savedPB < totalPB)) {
            totalPB = savedPB;
            hasPB = true;
        }

        if (run.isFinished() && currentRunTotal > 0) {
            if (totalPB == 0 || currentRunTotal < totalPB) {
                totalPB = currentRunTotal;
                hasPB = true;
                savePB(totalPB); // Salvar novo PB
                System.out.println("🏆 NOVO PERSONAL BEST! " + formatTimeDetailed(totalPB));
            }
        }

        Object[] separatorRow = {
                null,
                "───",
                "────────────────────────────────",
                "──────────────",
                "──────────────",
                "──────────────",
                "──────────────"
        };
        tableModel.addRow(separatorRow);

        String currentTimeDisplay = currentRunTotal > 0 ?
                formatTimeDetailed(currentRunTotal) : "--:--:--";

        Object[] currentRunRow = {
                null,
                "⏱",
                "Tempo desta Run",
                "",
                "",
                "",
                currentTimeDisplay
        };
        tableModel.addRow(currentRunRow);

        String pbDisplay = hasPB ? formatTimeDetailed(totalPB) : "--:--:--";
        Object[] pbRow = {
                null,
                "🏆",
                "Personal Best (PB)",
                "",
                "",
                "",
                pbDisplay
        };
        tableModel.addRow(pbRow);

        splitsTable.setEnabled(true);
        splitsTable.revalidate();
        splitsTable.repaint();

        if (run.getCurrentSplitIndex() < splitsTable.getRowCount()) {
            splitsTable.setRowSelectionInterval(run.getCurrentSplitIndex(), run.getCurrentSplitIndex());
        }
    }

    private long loadSavedPB() {
        try {
            String fileName = run.getRunTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_pb.txt";
            File pbFile = new File(fileName);

            if (pbFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(pbFile));
                String line = reader.readLine();
                reader.close();

                if (line != null && !line.trim().isEmpty()) {
                    return Long.parseLong(line.trim());
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ Não foi possível carregar PB salvo: " + e.getMessage());
        }
        return 0;
    }

    private void savePB(long pbTime) {
        try {
            String fileName = run.getRunTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_pb.txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(String.valueOf(pbTime));
            writer.close();
            System.out.println("💾 PB salvo: " + formatTimeDetailed(pbTime));
        } catch (Exception e) {
            System.err.println("❌ Erro ao salvar PB: " + e.getMessage());
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
        if (counterLabel != null) {
            int count = runCounter.getCounter(run.getRunTitle());

            counterLabel.setText(""); // Limpa primeiro
            counterLabel.repaint();   // Força redesenho

            SwingUtilities.invokeLater(() -> {
                counterLabel.setText("Tentativa: " + (count + 1));
                counterLabel.revalidate();
                counterLabel.repaint();

                if (counterLabel.getParent() != null) {
                    counterLabel.getParent().repaint();
                }
            });
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
                "Iniciar/Pausar (1)", "Split (2)", "Reset (3)",
                "Split Anterior (4)", "Pular Split (5)", "Comparar (6)",
                "Finalizar Run (7)"
        };

        String[] keys = {
                "start_pause", "split", "reset",
                "previous_split", "skip_split", "compare_prev", "finish_run"
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
                            setupGlobalHotkeys();
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

    private void saveRunToFile() {
        Object[] options = {"Texto (.txt)", "HTML (.html)", "Ambos", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(backgroundPanel,
                "<html><b>Escolha o formato para salvar:</b><br><br>" +
                        "• <font color='#00ff80'>HTML</font>: Salva com imagens EMBEDDADAS (recomendado)<br>" +
                        "• <font color='#00aaff'>TXT</font>: Formato simples para visualização rápida</html>",
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
        JButton loadButton = createStyledButton("Carregar Run");

        listControls.add(removeButton);
        listControls.add(clearButton);
        listControls.add(loadButton);
//        listControls.add(loadHTMLButton);

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
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() ||
                            f.getName().toLowerCase().endsWith(".html") ||
                            f.getName().toLowerCase().endsWith(".htm") ||
                            f.getName().toLowerCase().endsWith(".txt");
                }

                @Override
                public String getDescription() {
                    return "Runs do RunnerHand (*.html, *.txt)";
                }
            });

            if (fileChooser.showOpenDialog(configDialog) == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    if (path.toLowerCase().endsWith(".html") || path.toLowerCase().endsWith(".htm")) {
                        loadRunFromHTML(path);
                        titleLabel.setText(run.getRunTitle());

                        splitIcons.clear();
                        for (int i = 0; i < run.getSplits().size(); i++) {
                            Split split = run.getSplits().get(i);
                            if (split.getImagePath() != null && !split.getImagePath().isEmpty()) {
                                ImageIcon icon = loadAndResizeIcon(split.getImagePath());
                                if (icon != null) {
                                    splitIcons.put(i, icon);
                                }
                            }
                        }

                        isConfigured = true;
                        updateTable();
                        configDialog.dispose();

                        long totalPB = 0;
                        for (int i = 0; i < run.getPreviousRunSplits().size(); i++) {
                            totalPB += run.getPreviousTime(i);
                        }

                        JOptionPane.showMessageDialog(null,
                                "Run carregada do HTML com sucesso!\n" +
                                        "Splits: " + run.getSplits().size() + "\n" +
                                        "Tempos anteriores carregados na coluna ANTERIOR.\n\n" +
                                        "------------------------------------\n" +
                                        "Personal Best (PB): " + formatTimeDetailed(totalPB),
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        run.loadRunFromFile(path);
                        titleLabel.setText(run.getRunTitle());

                        loadPreviousRunData();

                        splitIcons.clear();
                        for (int i = 0; i < run.getSplits().size(); i++) {
                            Split split = run.getSplits().get(i);
                            if (split.getImagePath() != null && !split.getImagePath().isEmpty()) {
                                ImageIcon icon = loadAndResizeIcon(split.getImagePath());
                                if (icon != null) {
                                    splitIcons.put(i, icon);
                                }
                            }
                        }

                        isConfigured = true;
                        updateTable();
                        configDialog.dispose();

                        long totalPB = 0;
                        for (int i = 0; i < run.getPreviousRunSplits().size(); i++) {
                            totalPB += run.getPreviousTime(i);
                        }

                        JOptionPane.showMessageDialog(null,
                                "Run carregada do TXT com sucesso!\n" +
                                        "Splits: " + run.getSplits().size() + "\n" +
                                        "Tempos anteriores carregados na coluna ANTERIOR.\n\n" +
                                        "------------------------------------\n" +
                                        "Personal Best (PB): " + formatTimeDetailed(totalPB),
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(configDialog,
                            "Erro ao carregar run: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        saveButton.addActionListener(e -> {
            if (listModel.size() > 0) {
                String currentTitle = run.getRunTitle();

                java.util.List<Long> savedPreviousRuns = new java.util.ArrayList<>(run.getPreviousRunSplits());
                java.util.List<Long> savedBestSplits = new java.util.ArrayList<>(run.getBestSplits());

                java.util.List<Split> currentSplits = new java.util.ArrayList<>();
                for (Split split : run.getSplits()) {
                    Split newSplit = new Split(split.getName());
                    newSplit.setImagePath(split.getImagePath());
                    currentSplits.add(newSplit);
                }

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

                for (int i = 0; i < Math.min(savedPreviousRuns.size(), run.getPreviousRunSplits().size()); i++) {
                    run.setPreviousTime(i, savedPreviousRuns.get(i));
                }

                for (int i = 0; i < Math.min(savedBestSplits.size(), run.getBestSplits().size()); i++) {
                    run.getBestSplits().set(i, savedBestSplits.get(i));
                }

                isConfigured = true;
                configDialog.dispose();
                updateTable();

                JOptionPane.showMessageDialog(configDialog,
                        listModel.size() + " splits configurados com sucesso!\nTempos anteriores preservados.",
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
                                            String imageType = "png";
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

                                String[] cells = row.split("</td>");
                                long partialTime = 0;

                                if (cells.length > 4) {
                                    String partialCell = cells[4];
                                    int tdStart = partialCell.lastIndexOf("<td>");
                                    if (tdStart != -1) {
                                        String timeStr = partialCell.substring(tdStart + 4).trim();
                                        if (!timeStr.equals("--:--:--") && !timeStr.isEmpty()) {
                                            partialTime = parseTimeFromHTML(timeStr);
                                        }
                                    }
                                }

                                run.addSplit(splitName, imagePath);

                                if (partialTime > 0) {
                                    run.setPreviousTime(splitIndex, partialTime);
                                }

                                splitIndex++;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("✓ HTML carregado: " + run.getSplits().size() + " splits");
        System.out.println("✓ Tempos anteriores carregados:");
        for (int i = 0; i < run.getPreviousRunSplits().size(); i++) {
            System.out.println("  Split " + (i+1) + ": " + formatTimeDetailed(run.getPreviousTime(i)));
        }
    }

    private long parseTimeFromHTML(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            if (parts.length != 3) return 0;

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            String[] secParts = parts[2].split("\\.");
            int seconds = Integer.parseInt(secParts[0]);
            int millis = secParts.length > 1 ? Integer.parseInt(secParts[1]) : 0;

            return (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L) + millis;
        } catch (Exception e) {
            System.err.println("Erro ao converter tempo: " + timeStr);
            return 0;
        }
    }

    private void updateDisplay() {
        long currentTime = timer.getCurrentTime();
        String timeString = formatTimeDetailed(currentTime);

        String[] parts = timeString.split("\\.");
        if (parts.length == 2) {
            String htmlTime = String.format(
                    "<html><span style='font-size: 52px;'>%s.</span><span style='color: #00ff88; " +
                            "font-size: 36px;'>%s</span></html>",
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