import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RunCounter {
    private static final String COUNTER_FILE = "run_counter.properties";
    private Map<String, Integer> counters;
    private Properties properties;

    public RunCounter() {
        counters = new HashMap<>();
        properties = new Properties();
        loadCounters();
    }

    private void loadCounters() {
        try {
            File file = new File(COUNTER_FILE);
            if (file.exists()) {
                properties.load(new FileInputStream(file));
                for (String key : properties.stringPropertyNames()) {
                    counters.put(key, Integer.parseInt(properties.getProperty(key)));
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar contador: " + e.getMessage());
        }
    }

    public void incrementCounter(String gameTitle) {
        int current = counters.getOrDefault(gameTitle, 0);
        counters.put(gameTitle, current + 1);
        saveCounters();
    }

    public int getCounter(String gameTitle) {
        return counters.getOrDefault(gameTitle, 0);
    }

    public void resetCounter(String gameTitle) {
        counters.put(gameTitle, 0);
        saveCounters();
    }

    private void saveCounters() {
        try {
            for (Map.Entry<String, Integer> entry : counters.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue().toString());
            }
            properties.store(new FileOutputStream(COUNTER_FILE), "RunnerHand Run Counters");
        } catch (Exception e) {
            System.out.println("Erro ao salvar contadores: " + e.getMessage());
        }
    }

    public Map<String, Integer> getAllCounters() {
        return new HashMap<>(counters);
    }
}