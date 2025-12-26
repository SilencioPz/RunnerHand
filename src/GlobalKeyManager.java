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

    private GlobalKeyManager() {
        keyActions = new HashMap<>();
        keyConfig = new KeyConfig();

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            GlobalScreen.addNativeKeyListener(this);
            System.out.println("✓ Hotkeys globais ativadas com sucesso!");
            System.out.println("✓ Teclas padrão: 1-7 (teclado principal)");
            System.out.println("✓ Configure as teclas em 'Teclas' se necessário");
        } catch (NativeHookException e) {
            System.err.println("✗ ERRO: Não foi possível registrar hotkeys globais");
            System.err.println("Motivo: " + e.getMessage());
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

                    if (nativeKeyCode == expectedNativeCode) {
                        executeAction(action);
                        System.out.println("[HOTKEY] Ação executada: " + action);
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
            runnable.run();
        } else {
            System.err.println("Ação não registrada: " + action);
        }
    }

    public void registerAction(String action, Runnable runnable) {
        keyActions.put(action, runnable);
        System.out.println("✓ Ação registrada: " + action);
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
            }
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }
}