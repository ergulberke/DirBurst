package gui;

import core.Scanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private JTextField urlField;
    private JComboBox<String> wordlistSize;
    private JCheckBox[] statusCodes;
    private JTextArea outputArea;

    private JButton startButton;
    private JButton pauseButton;
    private JButton restartButton;

    private Thread scanThread;
    private Scanner scanner;

    public MainFrame() {
        setTitle("Scanner");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Üst: URL barı
        JPanel topPanel = new JPanel();
        urlField = new JTextField(40);
        urlField.setForeground(Color.GRAY);
        urlField.setText("https://example.com");

        urlField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (urlField.getText().equals("https://example.com")) {
                    urlField.setText("");
                    urlField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (urlField.getText().isEmpty()) {
                    urlField.setForeground(Color.GRAY);
                    urlField.setText("https://example.com");
                }
            }
        });

        topPanel.add(new JLabel("URL: "));
        topPanel.add(urlField);

        // Sol altta: Wordlist seçimi
        String[] sizes = {"Small", "Medium", "Large"};
        wordlistSize = new JComboBox<>(sizes);
        JPanel wordlistPanel = new JPanel();
        wordlistPanel.add(new JLabel("Wordlist:"));
        wordlistPanel.add(wordlistSize);

        // Sağda: HTTP kod filtreleme
        JPanel codePanel = new JPanel();
        codePanel.setLayout(new BoxLayout(codePanel, BoxLayout.Y_AXIS));
        codePanel.setBorder(BorderFactory.createTitledBorder("HTTP Kod Filtrele"));
        codePanel.setPreferredSize(new Dimension(150, 200));

        int[] codes = {100, 101, 200, 301, 302, 403, 404, 500};
        statusCodes = new JCheckBox[codes.length];
        for (int i = 0; i < codes.length; i++) {
            statusCodes[i] = new JCheckBox(String.valueOf(codes[i]));
            statusCodes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            codePanel.add(statusCodes[i]);
            codePanel.add(Box.createVerticalStrut(5));
        }

        // Alt: Çıktı ekranı
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Butonlar
        startButton = new JButton("Başlat");
        pauseButton = new JButton("Durdur");
        restartButton = new JButton("Yeniden Başlat");

        pauseButton.setEnabled(false);
        restartButton.setEnabled(false);

        startButton.addActionListener(this::startScan);

        pauseButton.addActionListener(e -> {
            if (scanner != null && !scanner.isPaused()) {
                scanner.pause();
                pauseButton.setText("Devam Et");
                restartButton.setEnabled(true);
                startButton.setEnabled(false);
            } else if (scanner != null) {
                scanner.resume();
                pauseButton.setText("Durdur");
                restartButton.setEnabled(false);
                startButton.setEnabled(false);
            }
        });

        restartButton.addActionListener(e -> {
            if (scanner != null) {
                scanner.stop();
                startScan(null);
            }
        });

        // Alt panel (butonlar + wordlist)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(wordlistPanel);
        bottomPanel.add(startButton);
        bottomPanel.add(pauseButton);
        bottomPanel.add(restartButton);

        // Bileşenleri yerleştir
        add(topPanel, BorderLayout.NORTH);
        add(codePanel, BorderLayout.EAST);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        // Uygulama açılır açılmaz placeholder ayarla
        SwingUtilities.invokeLater(() -> {
            if (urlField.getText().isEmpty()) {
                urlField.setForeground(Color.GRAY);
                urlField.setText("https://example.com");
            }
        });
    }

    private void startScan(ActionEvent e) {
        String url = urlField.getText().trim();
        String size = (String) wordlistSize.getSelectedItem();
        ArrayList<Integer> selectedCodes = new ArrayList<>();
        for (JCheckBox checkBox : statusCodes) {
            if (checkBox.isSelected()) {
                selectedCodes.add(Integer.parseInt(checkBox.getText()));
            }
        }

        if (url.isEmpty() || url.equals("https://example.com")) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir URL girin!");
            return;
        }

        String wordlistPath = switch (size) {
            case "Small" -> "wordlists/raft-small-words.txt";
            case "Medium" -> "wordlists/raft-medium-words.txt";
            case "Large" -> "wordlists/raft-large-words.txt";
            default -> throw new IllegalStateException("Beklenmeyen boyut: " + size);
        };

        outputArea.setText(""); // ekranı temizle

        scanner = new Scanner(url, wordlistPath, selectedCodes, outputArea);
        scanThread = new Thread(scanner::start);
        scanThread.start();

        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        pauseButton.setText("Durdur");
        restartButton.setEnabled(false);
    }
}
