import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class ImageLoader {
    public static JLabel loadLogo(String resourcePath, int targetWidth, int targetHeight) {
        try {
            String cleanPath = resourcePath.replace("src/", "").replace("\\", "/");
            if (!cleanPath.startsWith("/")) {
                cleanPath = "/" + cleanPath;
            }

            System.out.println("Tentando carregar: " + cleanPath);

            InputStream stream = ImageLoader.class.getResourceAsStream(cleanPath);

            if (stream != null) {
                BufferedImage originalImage = ImageIO.read(stream);

                double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                int scaledWidth = targetWidth;
                int scaledHeight = (int) (targetWidth / aspectRatio);

                Image scaledImage = originalImage.getScaledInstance(
                        scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                stream.close();
                return new JLabel(new ImageIcon(scaledImage));
            } else {
                System.out.println("❌ Stream null para: " + cleanPath);

                File fileAttempt = new File(resourcePath);
                if (fileAttempt.exists()) {
                    System.out.println("✓ Encontrado como arquivo externo");
                    BufferedImage originalImage = ImageIO.read(fileAttempt);
                    double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                    int scaledWidth = targetWidth;
                    int scaledHeight = (int) (targetWidth / aspectRatio);
                    Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    return new JLabel(new ImageIcon(scaledImage));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar a imagem: " + resourcePath);
            e.printStackTrace();
        }

        System.out.println("⚠ Usando fallback de texto");
        JLabel textLogo = new JLabel("<html><center>RUNNER<br>HAND</center></html>");
        textLogo.setFont(new Font("Arial", Font.BOLD, 16));
        textLogo.setForeground(new Color(0, 150, 255));
        textLogo.setHorizontalAlignment(SwingConstants.CENTER);
        return textLogo;
    }
}