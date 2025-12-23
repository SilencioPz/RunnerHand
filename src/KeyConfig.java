import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KeyConfig {
    private static final String CONFIG_FILE = "runnerhand_config.properties";
    private static final Map<String, Integer> DEFAULT_KEYS = new HashMap<>();

    static {
        DEFAULT_KEYS.put("start_pause", 107);      // Numpad +
        DEFAULT_KEYS.put("split", 106);           // Numpad *
        DEFAULT_KEYS.put("reset", 96);            // Numpad 0
        DEFAULT_KEYS.put("previous_split", 100);  // Numpad 4
        DEFAULT_KEYS.put("skip_split", 102);      // Numpad 6
        DEFAULT_KEYS.put("compare_prev", 97);     // Numpad 1
        DEFAULT_KEYS.put("compare_next", 103);    // Numpad 7
        DEFAULT_KEYS.put("finish_run", 104);      // Numpad 8
    }

    private Properties properties;
    private Map<String, Integer> keyMap;

    public KeyConfig() {
        properties = new Properties();
        keyMap = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                properties.load(new FileInputStream(configFile));
                for (String action : DEFAULT_KEYS.keySet()) {
                    String value = properties.getProperty(action);
                    if (value != null) {
                        keyMap.put(action, Integer.parseInt(value));
                    } else {
                        keyMap.put(action, DEFAULT_KEYS.get(action));
                    }
                }
            } else {
                keyMap.putAll(DEFAULT_KEYS);
                saveConfig();
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar configurações: " + e.getMessage());
            keyMap.putAll(DEFAULT_KEYS);
        }
    }

    public void saveConfig() {
        try {
            for (Map.Entry<String, Integer> entry : keyMap.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue().toString());
            }
            properties.store(new FileOutputStream(CONFIG_FILE), "RunnerHand Key Configuration");
        } catch (Exception e) {
            System.out.println("Erro ao salvar configurações: " + e.getMessage());
        }
    }

    public int getKeyCode(String action) {
        return keyMap.getOrDefault(action, DEFAULT_KEYS.get(action));
    }

    public void setKeyCode(String action, int keyCode) {
        keyMap.put(action, keyCode);
        saveConfig();
    }

    public Map<String, Integer> getAllKeyCodes() {
        return new HashMap<>(keyMap);
    }

    public static String getKeyName(int keyCode) {
        switch (keyCode) {
            case 96:
                return "Numpad 0";
            case 97:
                return "Numpad 1";
            case 98:
                return "Numpad 2";
            case 99:
                return "Numpad 3";
            case 100:
                return "Numpad 4";
            case 101:
                return "Numpad 5";
            case 102:
                return "Numpad 6";
            case 103:
                return "Numpad 7";
            case 104:
                return "Numpad 8";
            case 105:
                return "Numpad 9";
            case 106:
                return "Numpad *";
            case 107:
                return "Numpad +";
            case 109:
                return "Numpad -";
            case 110:
                return "Numpad .";
            case 111:
                return "Numpad /";
            default:
                return "Key " + keyCode;
        }
    }
}