import java.io.*;
import java.nio.file.*;

public class NativeLoader {
    static {
        loadNativeLibrary();
    }

    public static void loadNativeLibrary() {
        try {
            System.out.println("=== CARREGANDO JNATIVEHOOK ===");
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();

            System.out.println("Sistema: " + os);
            System.out.println("Arquitetura: " + arch);
            System.out.println("java.library.path: " + System.getProperty("java.library.path"));

            String libPathInJar = getNativeLibraryPath(os, arch);
            System.out.println("Buscando: " + libPathInJar);

            extractAndLoadFromJar(libPathInJar);

        } catch (Exception e) {
            System.err.println("❌ ERRO ao carregar JNativeHook:");
            e.printStackTrace();

            try {
                System.err.println("Tentando carregamento alternativo...");
                com.github.kwhat.jnativehook.GlobalScreen.class.getName();
            } catch (Exception e2) {
                System.err.println("Falha total no carregamento!");
            }
        }
    }

    private static String getNativeLibraryPath(String os, String arch) {
        if (os.contains("win")) {
            return "/com/github/kwhat/jnativehook/lib/windows/x86_64/JNativeHook.dll";
        } else if (os.contains("mac")) {
            return "/com/github/kwhat/jnativehook/lib/darwin/x86_64/libJNativeHook.dylib";
        } else {
            if (arch.contains("64")) {
                return "/com/github/kwhat/jnativehook/lib/linux/x86_64/libJNativeHook.so";
            } else {
                return "/com/github/kwhat/jnativehook/lib/linux/x86/libJNativeHook.so";
            }
        }
    }

    private static void extractAndLoadFromJar(String libPathInJar) throws IOException {
        InputStream in = NativeLoader.class.getResourceAsStream(libPathInJar);

        if (in == null) {
            libPathInJar = libPathInJar.replace("/com/github/kwhat/jnativehook/lib/", "/");
            in = NativeLoader.class.getResourceAsStream(libPathInJar);
        }

        if (in == null) {
            throw new IOException("Biblioteca não encontrada no classpath: " + libPathInJar);
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = libPathInJar.substring(libPathInJar.lastIndexOf('/') + 1);
        File tempFile = new File(tempDir, "jnativehook_" + System.currentTimeMillis() + "_" + fileName);

        System.out.println("Extraindo para: " + tempFile.getAbsolutePath());

        Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        in.close();

        tempFile.deleteOnExit();

        System.load(tempFile.getAbsolutePath());
        System.out.println("✅ Biblioteca carregada com sucesso!");
    }
}