import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Run {
    private List<Split> splits;
    private int currentSplitIndex;
    private String runTitle;
    private List<Long> bestSplits;
    private List<Long> previousRunSplits;

    public Run() {
        splits = new ArrayList<>();
        bestSplits = new ArrayList<>();
        previousRunSplits = new ArrayList<>();
        currentSplitIndex = 0;
        runTitle = "Nova Run";
    }

    public Run(String title) {
        this();
        this.runTitle = title;
    }

    public void addSplit(String name) {
        Split split = new Split(name);
        splits.add(split);
        bestSplits.add(0L);
        previousRunSplits.add(0L);
    }

    public void addSplit(String name, String imagePath) {
        Split split = new Split(name);
        split.setImagePath(imagePath);
        splits.add(split);
        bestSplits.add(0L);
        previousRunSplits.add(0L);
    }

    public void nextSplit(long currentTime) {
        if (currentSplitIndex < splits.size()) {
            splits.get(currentSplitIndex).setSplitTime(currentTime);

            long splitTime = getPartialTime(currentSplitIndex);
            if (bestSplits.get(currentSplitIndex) == 0 ||
                    (splitTime > 0 && splitTime < bestSplits.get(currentSplitIndex))) {
                bestSplits.set(currentSplitIndex, splitTime);
            }

            currentSplitIndex++;
        }
    }

    public void skipSplit() {
        if (currentSplitIndex < splits.size()) {
            splits.get(currentSplitIndex).setSplitTime(0);
            currentSplitIndex++;
        }
    }

    public void previousSplit() {
        if (currentSplitIndex > 0) {
            currentSplitIndex--;
            splits.get(currentSplitIndex).setSplitTime(0);
        }
    }

    public long getPartialTime(int splitIndex) {
        if (splitIndex < 0 || splitIndex >= splits.size()) return 0;
        if (splitIndex == 0) return splits.get(0).getSplitTime();

        return splits.get(splitIndex).getSplitTime() - splits.get(splitIndex - 1).getSplitTime();
    }

    public long getBestTime(int splitIndex) {
        if (splitIndex >= 0 && splitIndex < bestSplits.size()) {
            return bestSplits.get(splitIndex);
        }
        return 0;
    }

    public long getPreviousTime(int splitIndex) {
        if (splitIndex >= 0 && splitIndex < previousRunSplits.size()) {
            return previousRunSplits.get(splitIndex);
        }
        return 0;
    }

    public void loadPreviousRun(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null && index < previousRunSplits.size()) {
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length >= 3) {
                        long hours = Long.parseLong(parts[0].trim());
                        long minutes = Long.parseLong(parts[1].trim());
                        long seconds = Long.parseLong(parts[2].trim());
                        long totalMs = (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L);
                        previousRunSplits.set(index, totalMs);
                    }
                }
                index++;
            }
            reader.close();
        }
    }

    public void saveRunHTML(String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        writer.write("<!DOCTYPE html>");
        writer.write("<html lang='pt-BR'>");
        writer.write("<head>");
        writer.write("<meta charset='UTF-8'>");
        writer.write("<title>RunnerHand - " + runTitle + "</title>");
        writer.write("<style>");
        writer.write("body { font-family: Arial, sans-serif; margin: 20px; background: #1e1e1e; color: #e6e6e6; }");
        writer.write("h1 { color: #0096ff; text-align: center; }");
        writer.write("h2 { color: #00ff80; border-bottom: 2px solid #0096ff; padding-bottom: 5px; }");
        writer.write("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        writer.write("th { background: #2d2d2d; color: #0096ff; padding: 12px; text-align: left; }");
        writer.write("td { padding: 10px; border-bottom: 1px solid #3d3d3d; }");
        writer.write("tr:nth-child(even) { background: #252525; }");
        writer.write(".split-icon { width: 40px; height: 40px; border-radius: 5px; }");
        writer.write(".good { color: #00ff80; }");
        writer.write(".bad { color: #ff6464; }");
        writer.write(".gold { color: #ffd700; }");
        writer.write("</style>");
        writer.write("</head>");
        writer.write("<body>");

        writer.write("<h1>RunnerHand Speedrun Timer</h1>");
        writer.write("<h2>" + runTitle + "</h2>");

        String totalTime = splits.isEmpty() ? "00:00:00.000" : formatTime(splits.get(splits.size()-1).getSplitTime());
        writer.write("<p><strong>Tempo Total:</strong> " + totalTime + "</p>");
        writer.write("<p><strong>Data:</strong> " + new java.util.Date() + "</p>");

        writer.write("<h2>SPLITS</h2>");
        writer.write("<table>");
        writer.write("<tr><th>Ícone</th><th>#</th><th>Nome</th><th>Tempo Split</th><th>Tempo Parcial</th><th>Diferença</th><th>Melhor Tempo</th></tr>");

        for (int i = 0; i < splits.size(); i++) {
            Split split = splits.get(i);

            String splitTime = split.getSplitTime() > 0 ? formatTime(split.getSplitTime()) : "Não Concluído";
            String partialTime = getPartialTime(i) > 0 ? formatTime(getPartialTime(i)) : "--:--:--";
            String bestTime = bestSplits.get(i) > 0 ? formatTime(bestSplits.get(i)) : "--:--:--";

            // Calcular diferença para exibição
            String diffClass = "";
            String diffText = "--:--:--";
            if (getPartialTime(i) > 0 && bestSplits.get(i) > 0) {
                long diff = getPartialTime(i) - bestSplits.get(i);
                if (diff > 0) {
                    diffClass = "bad";
                    diffText = "+" + formatTime(diff);
                } else if (diff < 0) {
                    diffClass = "good";
                    diffText = "-" + formatTime(Math.abs(diff));
                } else {
                    diffText = "±0.000";
                }
            }

            writer.write("<tr>");

            // Coluna do ícone (com imagem EMBEDDADA em base64)
            writer.write("<td>");
            if (split.getImagePath() != null && !split.getImagePath().isEmpty()) {
                try {
                    String base64Image = imageToBase64(split.getImagePath());
                    writer.write("<img src='data:image/png;base64," + base64Image + "' class='split-icon' alt='"+ split.getName() +"'>");
                } catch (Exception e) {
                    writer.write("<div style='width:40px;height:40px;background:#0096ff;border-radius:5px;'></div>");
                }
            } else {
                writer.write("<div style='width:40px;height:40px;background:#0096ff;border-radius:5px;'></div>");
            }
            writer.write("</td>");

            writer.write("<td>" + (i + 1) + "</td>");
            writer.write("<td><strong>" + split.getName() + "</strong></td>");
            writer.write("<td>" + splitTime + "</td>");
            writer.write("<td>" + partialTime + "</td>");
            writer.write("<td class='" + diffClass + "'>" + diffText + "</td>");
            writer.write("<td class='gold'>" + bestTime + "</td>");
            writer.write("</tr>");
        }

        writer.write("</table>");

        // Adicionar melhores tempos
        writer.write("<h2>MELHORES TEMPOS POR SPLIT</h2>");
        writer.write("<table>");
        writer.write("<tr><th>#</th><th>Split</th><th>Melhor Tempo</th></tr>");

        for (int i = 0; i < bestSplits.size(); i++) {
            if (bestSplits.get(i) > 0) {
                writer.write("<tr>");
                writer.write("<td>" + (i + 1) + "</td>");
                writer.write("<td>" + splits.get(i).getName() + "</td>");
                writer.write("<td class='gold'>" + formatTime(bestSplits.get(i)) + "</td>");
                writer.write("</tr>");
            }
        }

        writer.write("</table>");

        writer.write("<footer style='margin-top: 40px; text-align: center; color: #888;'>");
        writer.write("<p>Gerado por RunnerHand Speedrun Timer - Desenvolvido por SilencioPz</p>");
        writer.write("</footer>");

        writer.write("</body>");
        writer.write("</html>");

        writer.close();
    }

    // Método para converter imagem para base64
    private String imageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        if (!file.exists()) return "";

        String extension = imagePath.substring(imagePath.lastIndexOf(".") + 1).toLowerCase();
        BufferedImage image = ImageIO.read(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (extension.equals("png")) {
            ImageIO.write(image, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } else if (extension.equals("jpg") || extension.equals("jpeg")) {
            ImageIO.write(image, "JPEG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } else if (extension.equals("gif")) {
            ImageIO.write(image, "GIF", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }

        return "";
    }

    private long parseTime(String timeStr) {
        String[] parts = timeStr.split("[:.]");
        if (parts.length == 4) {
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = Long.parseLong(parts[2]);
            long millis = Long.parseLong(parts[3]);
            return (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L) + millis;
        }
        return 0;
    }

    public void saveRun(String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write("Run Title: " + runTitle);
        writer.newLine();
        writer.write("Total Time: " + formatTime(splits.isEmpty() ? 0 : splits.get(splits.size()-1).getSplitTime()));
        writer.newLine();
        writer.newLine();
        writer.write("SPLITS:");
        writer.newLine();

        for (int i = 0; i < splits.size(); i++) {
            Split split = splits.get(i);
            String splitTime = split.getSplitTime() > 0 ? formatTime(split.getSplitTime()) : "Not Completed";
            String partialTime = getPartialTime(i) > 0 ? formatTime(getPartialTime(i)) : "--:--:--";

            writer.write(String.format("%02d. %s - %s (Partial: %s)",
                    i+1, split.getName(), splitTime, partialTime));
            writer.newLine();
        }

        writer.newLine();
        writer.write("BEST SPLITS:");
        writer.newLine();
        for (int i = 0; i < bestSplits.size(); i++) {
            if (bestSplits.get(i) > 0) {
                writer.write(String.format("%02d. %s", i+1, formatTime(bestSplits.get(i))));
                writer.newLine();
            }
        }

        writer.close();
    }

    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        long millis = milliseconds % 1000;

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }

    public List<Split> getSplits() {
        return splits;
    }

    public int getCurrentSplitIndex() {
        return currentSplitIndex;
    }

    public boolean isFinished() {
        return currentSplitIndex >= splits.size();
    }

    public String getRunTitle() {
        return runTitle;
    }

    public void setRunTitle(String title) {
        this.runTitle = title;
    }
}