import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalKeyManager implements NativeKeyListener {
    private static GlobalKeyManager instance;
    private Map<String, Runnable> keyActions;
    private boolean enabled = true;
    private KeyConfig keyConfig;

    static {
        try {
            Class.forName("com.github.kwhat.jnativehook.GlobalScreen");
            System.out.println("✅ JNativeHook pré-carregado com sucesso");
        } catch (Exception e) {
            System.err.println("⚠ AVISO: JNativeHook não pré-carregado");
        }
    }

    private GlobalKeyManager() {
        keyActions = new HashMap<>();
        keyConfig = new KeyConfig();

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            System.out.println("Registrando NativeHook...");
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            System.out.println("✓ Hotkeys globais ativadas!");
        } catch (NativeHookException e) {
            System.err.println("✗ ERRO ao registrar hotkeys:");
            e.printStackTrace();
        }
    }

    public static GlobalKeyManager getInstance() {
        if (instance == null) {
            instance = new GlobalKeyManager();
        }
        return instance;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (!enabled) return;

        int nativeKeyCode = e.getKeyCode();

        System.out.println("[DEBUG] Tecla: " + NativeKeyEvent.getKeyText(nativeKeyCode) +
                " | Código Native: " + nativeKeyCode);

        handleKeyPress(nativeKeyCode);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Não usado
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Não usado
    }

    private void handleKeyPress(int nativeKeyCode) {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Integer> actions = keyConfig.getAllKeyCodes();

                for (Map.Entry<String, Integer> entry : actions.entrySet()) {
                    String action = entry.getKey();
                    int awtKeyCode = entry.getValue();
                    int expectedNativeCode = KeyConfig.awtToNativeKeyCode(awtKeyCode);

                    System.out.println("[DEBUG] Comparando: nativeKeyCode=" + nativeKeyCode +
                            " | expectedNativeCode=" + expectedNativeCode +
                            " | action=" + action);

                    if (nativeKeyCode == expectedNativeCode) {
                        executeAction(action);
                        System.out.println("[HOTKEY] ✓ Ação executada: " + action);
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Erro ao processar tecla: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void executeAction(String action) {
        Runnable runnable = keyActions.get(action);
        if (runnable != null) {
            System.out.println("[EXEC] Executando ação: " + action);
            runnable.run();
        } else {
            System.err.println("✗ Ação não registrada: " + action);
            System.err.println("Ações disponíveis: " + keyActions.keySet());
        }
    }

    public void registerAction(String action, Runnable runnable) {
        keyActions.put(action, runnable);
        System.out.println("✓ Ação registrada: " + action);
    }

    public void startListening() {
        keyConfig = new KeyConfig();
        System.out.println("✓ Listener reiniciado - configurações recarregadas");
        System.out.println("Ações registradas: " + keyActions.keySet());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("Hotkeys globais: " + (enabled ? "ATIVADAS" : "DESATIVADAS"));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void cleanup() {
        try {
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.unregisterNativeHook();
                System.out.println("✓ Hotkeys globais desregistradas");
            }
        } catch (NativeHookException e) {
            System.err.println("Erro ao limpar hotkeys: " + e.getMessage());
            e.printStackTrace();
        }
    }
}