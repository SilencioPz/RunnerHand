import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RunCounter {
    private static final String COUNTER_FILE = "runnerhand_counters.properties";
    private Properties counters;

    public RunCounter() {
        counters = new Properties();
        loadCounters();
    }

    private void loadCounters() {
        try {
            File file = new File(COUNTER_FILE);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    counters.load(fis);
                }
                System.out.println("✓ Contadores carregados: " + counters.size() + " runs");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar contadores: " + e.getMessage());
        }
    }

    private void saveCounters() {
        try {
            try (FileOutputStream fos = new FileOutputStream(COUNTER_FILE)) {
                counters.store(fos, "RunnerHand Run Attempt Counters");
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar contadores: " + e.getMessage());
        }
    }

    public int getCounter(String runTitle) {
        String value = counters.getProperty(runTitle, "0");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void incrementCounter(String runTitle) {
        int current = getCounter(runTitle);
        counters.setProperty(runTitle, String.valueOf(current + 1));
        saveCounters();
        System.out.println("📊 Contador atualizado: " + runTitle + " = " + (current + 1));
    }
}