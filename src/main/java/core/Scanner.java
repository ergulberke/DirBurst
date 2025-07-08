package core;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Scanner {
    private final String baseUrl;
    private final String wordlistPath;
    private final List<Integer> filterCodes;
    private final JTextArea outputArea;

    private volatile boolean paused = false;
    private volatile boolean stopped = false;

    public Scanner(String baseUrl, String wordlistPath, List<Integer> filterCodes, JTextArea outputArea) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.wordlistPath = wordlistPath;
        this.filterCodes = filterCodes;
        this.outputArea = outputArea;
    }

    public void start() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(wordlistPath)) {
            if (inputStream == null) {
                SwingUtilities.invokeLater(() -> outputArea.append("Wordlist bulunamadı: " + wordlistPath + "\n"));
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null && !stopped) {
                while (paused) {
                    Thread.sleep(200);
                }

                String fullUrl = baseUrl + line;
                int responseCode = getStatusCode(fullUrl);

                if (filterCodes.contains(responseCode)) {
                    final String result = responseCode + " => " + fullUrl + "\n";
                    SwingUtilities.invokeLater(() -> outputArea.append(result));
                }
            }

            if (stopped) {
                SwingUtilities.invokeLater(() -> outputArea.append("Tarama durduruldu.\n"));
            }

        } catch (IOException | InterruptedException e) {
            SwingUtilities.invokeLater(() -> outputArea.append("Hata: " + e.getMessage() + "\n"));
        }
    }
    private int getStatusCode(String targetUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(targetUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            return conn.getResponseCode();
        } catch (IOException e) {
            return -1;
        }
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public void stop() {
        this.stopped = true;
        this.paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isStopped() {
        return stopped;
    }
}
