import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KeyConfig {
    private static final String CONFIG_FILE = "runnerhand_config.properties";
    private static final Map<String, Integer> DEFAULT_KEYS = new HashMap<>();

    static {
        DEFAULT_KEYS.put("start_pause", 49);    // 1
        DEFAULT_KEYS.put("split", 50);          // 2
        DEFAULT_KEYS.put("reset", 51);          // 3
        DEFAULT_KEYS.put("previous_split", 52); // 4
        DEFAULT_KEYS.put("skip_split", 53);     // 5
        DEFAULT_KEYS.put("compare_prev", 54);   // 6
        DEFAULT_KEYS.put("finish_run", 55);     // 7
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

    public static int awtToNativeKeyCode(int awtKeyCode) {
        if (awtKeyCode >= 49 && awtKeyCode <= 57) { // 1-9
            return awtKeyCode - 47; // 1=2, 2=3, ..., 9=10
        }
        if (awtKeyCode == 48) return 11; // 0

        if (awtKeyCode >= 65 && awtKeyCode <= 90) { // A-Z
            switch (awtKeyCode) {
                case 65: return 30;  // A
                case 66: return 48;  // B
                case 67: return 46;  // C
                case 68: return 32;  // D
                case 69: return 18;  // E
                case 70: return 33;  // F
                case 71: return 34;  // G
                case 72: return 35;  // H
                case 73: return 23;  // I
                case 74: return 36;  // J
                case 75: return 37;  // K
                case 76: return 38;  // L
                case 77: return 50;  // M
                case 78: return 49;  // N
                case 79: return 24;  // O
                case 80: return 25;  // P
                case 81: return 16;  // Q
                case 82: return 19;  // R
                case 83: return 31;  // S
                case 84: return 20;  // T
                case 85: return 22;  // U
                case 86: return 47;  // V
                case 87: return 17;  // W
                case 88: return 45;  // X
                case 89: return 21;  // Y
                case 90: return 44;  // Z
            }
        }

        if (awtKeyCode >= 112 && awtKeyCode <= 123) { // F1-F12
            return awtKeyCode - 53; // F1=59, F2=60, ..., F12=88
        }

        if (awtKeyCode >= 96 && awtKeyCode <= 105) { // Numpad 0-9
            return awtKeyCode - 14; // Numpad0=82, Numpad1=79... Numpad9=81
        }

        switch (awtKeyCode) {
            case 37: return 203; // Esquerda
            case 38: return 200; // Cima
            case 39: return 205; // Direita
            case 40: return 208; // Baixo
        }

        switch (awtKeyCode) {
            case 32: return 57;  // Espaço
            case 10: return 28;  // Enter
            case 8:  return 14;  // Backspace
            case 9:  return 15;  // Tab
            case 27: return 1;   // Esc
            case 16: return 42;  // Shift esquerdo
            case 17: return 29;  // Ctrl esquerdo
            case 18: return 56;  // Alt esquerdo
            case 20: return 58;  // Caps Lock
            case 144: return 69; // Num Lock
            case 145: return 70; // Scroll Lock
            case 45: return 82;  // Insert
            case 46: return 83;  // Delete
            case 36: return 71;  // Home
            case 35: return 79;  // End
            case 33: return 73;  // Page Up
            case 34: return 81;  // Page Down

            case 106: return 55; // Numpad *
            case 107: return 78; // Numpad +
            case 109: return 74; // Numpad -
            case 110: return 83; // Numpad .
            case 111: return 53; // Numpad /
        }

        return awtKeyCode;
    }

    public static String getKeyName(int keyCode) {
        switch (keyCode) {
            case 49: return "1";
            case 50: return "2";
            case 51: return "3";
            case 52: return "4";
            case 53: return "5";
            case 54: return "6";
            case 55: return "7";
            case 56: return "8";
            case 57: return "9";
            case 48: return "0";

            case 112: return "F1";
            case 113: return "F2";
            case 114: return "F3";
            case 115: return "F4";
            case 116: return "F5";
            case 117: return "F6";
            case 118: return "F7";
            case 119: return "F8";
            case 120: return "F9";
            case 121: return "F10";
            case 122: return "F11";
            case 123: return "F12";

            case 96: return "Numpad 0";
            case 97: return "Numpad 1";
            case 98: return "Numpad 2";
            case 99: return "Numpad 3";
            case 100: return "Numpad 4";
            case 101: return "Numpad 5";
            case 102: return "Numpad 6";
            case 103: return "Numpad 7";
            case 104: return "Numpad 8";
            case 105: return "Numpad 9";
            case 106: return "Numpad *";
            case 107: return "Numpad +";
            case 109: return "Numpad -";
            case 110: return "Numpad .";
            case 111: return "Numpad /";

            case 37: return "←";
            case 38: return "↑";
            case 39: return "→";
            case 40: return "↓";

            case 8: return "Backspace";
            case 9: return "Tab";
            case 10: return "Enter";
            case 16: return "Shift";
            case 17: return "Ctrl";
            case 18: return "Alt";
            case 19: return "Pause/Break";
            case 20: return "Caps Lock";
            case 27: return "Esc";
            case 32: return "Espaço";
            case 33: return "Page Up";
            case 34: return "Page Down";
            case 35: return "End";
            case 36: return "Home";
            case 44: return "Print Screen";
            case 45: return "Insert";
            case 46: return "Delete";
            case 91: return "Windows";
            case 92: return "Menu";
            case 144: return "Num Lock";
            case 145: return "Scroll Lock";

            case 173: return "Volume -";
            case 174: return "Volume +";
            case 175: return "Mute";
            case 181: return "Calculadora";
            case 216: return "Play/Pause";
            case 217: return "Stop";
            case 218: return "Próxima";
            case 219: return "Anterior";

            case 65: return "A";
            case 66: return "B";
            case 67: return "C";
            case 68: return "D";
            case 69: return "E";
            case 70: return "F";
            case 71: return "G";
            case 72: return "H";
            case 73: return "I";
            case 74: return "J";
            case 75: return "K";
            case 76: return "L";
            case 77: return "M";
            case 78: return "N";
            case 79: return "O";
            case 80: return "P";
            case 81: return "Q";
            case 82: return "R";
            case 83: return "S";
            case 84: return "T";
            case 85: return "U";
            case 86: return "V";
            case 87: return "W";
            case 88: return "X";
            case 89: return "Y";
            case 90: return "Z";

            default:
                try {
                    return KeyEvent.getKeyText(keyCode);
                } catch (Exception e) {
                    return "Tecla " + keyCode;
                }
        }
    }
}