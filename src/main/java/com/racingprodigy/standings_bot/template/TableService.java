package com.racingprodigy.standings_bot.template;

import com.racingprodigy.standings_bot.iracing.model.IracingSeasonStandingChunkResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TableService {

    public TableService() {
        try {
            loadAndRegisterFont("font/Poppins-Regular.ttf");
            loadAndRegisterFont("font/Poppins-Bold.ttf");
            loadAndRegisterFont("font/Poppins-Italic.ttf");
        } catch (Exception e) {
            // log
        }
    }

    public FileUpload getImageTable(List<IracingSeasonStandingChunkResponse> seasonStandings, int weekNumber, String trackName) throws Exception {

        Object[][] data = new Object[36][4];
        DecimalFormat format = new DecimalFormat("0.#");

        for (int x = 0; x < 36; x++) {
            var entry = seasonStandings.get(x);

            data[x][0] = String.valueOf(x + 1);
            data[x][1] = formatDriverName(entry.displayName());
            data[x][2] = format.format(entry.rawPoints());
            data[x][3] = entry.country();
        }

        try {
            // Create a BufferedImage
            BufferedImage image = generateTableImage(
                    new boolean[]{true, false, false, false},
                    data,
                    new int[]{216, 155, 200, 160},
                    weekNumber,
                    trackName);

            InputStream inputStream = bufferedImageToInputStream(image, "png");
            return FileUpload.fromData(inputStream, "table.png");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public InputStream bufferedImageToInputStream(BufferedImage image, String formatName) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // Write the BufferedImage to the ByteArrayOutputStream
        ImageIO.write(image, formatName, os);
        // Convert ByteArrayOutputStream to ByteArrayInputStream
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        return inputStream;
    }

    public BufferedImage generateTableImage(boolean[] shouldCenterColumn, Object[][] data, int[] perColumnPadding, int weekNumber, String trackName) throws Exception {
        int cellPadding = 10;
        int cellHeight = 130;

        int topPadding = 1290;

        int numColumns = shouldCenterColumn.length;

        // Load background image from classpath
        BufferedImage bgImage = ImageIO.read(TableService.class.getResourceAsStream("/imageBackground.png"));

        // Create a temporary Graphics2D object for font metrics calculations
        BufferedImage tmpImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tmpG2d = tmpImage.createGraphics();
        tmpG2d.setFont(new Font("Poppins", Font.PLAIN, 80));
        FontMetrics fm = tmpG2d.getFontMetrics();
        // Determine the width of each column dynamically based on content
        int[] columnWidths = new int[numColumns];
        for (int col = 0; col < numColumns; col++) {
            for (int row = 0; row < data.length; row++) {
                int contentWidth = fm.stringWidth(data[row][col].toString()) + 2 * cellPadding;
                if (contentWidth > columnWidths[col]) {
                    columnWidths[col] = contentWidth;
                }
            }
        }
        tmpG2d.dispose();

        // Create the final BufferedImage
        BufferedImage image = new BufferedImage(bgImage.getWidth(), bgImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw background image
        g2d.drawImage(bgImage, 0, 0, bgImage.getWidth(), bgImage.getHeight(), null);
        g2d.setColor(Color.WHITE);

        g2d.setFont(new Font("Poppins", Font.ITALIC, 90));
        var weekText = String.format("Week %d - %s", weekNumber, trackName);
        drawCell(g2d, weekText, 0, 430, bgImage.getWidth(), cellHeight, cellPadding, true);

        g2d.setFont(new Font("Poppins", Font.PLAIN, 80));
        // Draw data rows
        int x = 0;
        int y = 0;
        for (int row = 0; row < data.length; row++) {
            x = 0;
            for (int col = 0; col < numColumns; col++) {
                x += perColumnPadding[col];
                y = topPadding + row * cellHeight;
                drawCell(g2d, data[row][col].toString(), x, y, columnWidths[col], cellHeight, cellPadding, shouldCenterColumn[col]);
                x += columnWidths[col];
            }
        }

        g2d.setFont(new Font("Poppins", Font.ITALIC, 80));
        var updatedText = "Updated At: " + ZonedDateTime.now(java.time.ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("h:mma '@' MM/dd/yy")).toLowerCase();
        drawCell(g2d, updatedText, 0, y + (int) Math.round(cellHeight * 1.6), bgImage.getWidth(), cellHeight, cellPadding, true);

        g2d.dispose();
        return image;
    }

    private void drawCell(Graphics2D g2d, String text, int x, int y, int width, int height, int padding, boolean centerText) {
        // Draw cell text
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = centerText ? x + (width - fm.stringWidth(text)) / 2 : x + padding;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }

    private String formatDriverName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Input name must not be null or empty");
        }

        String[] names = fullName.trim().split("\\s+");
        if (names.length < 2) {
            throw new IllegalArgumentException("Input name must contain at least a first name and a last name");
        }

        String firstName = names[0];
        String lastName = names[names.length - 1];
        String firstInitial = firstName.substring(0, 1).toUpperCase();

        return firstInitial + ". " + lastName.replaceAll("\\d+$", "");
    }

    private void loadAndRegisterFont(String fontPath) throws Exception {
        InputStream fontStream = TableService.class.getClassLoader().getResourceAsStream(fontPath);
        if (fontStream == null) {
            throw new IllegalArgumentException("Font resource not found: " + fontPath);
        }

        Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);

        fontStream.close();
    }

    private Font getFont(String fontName, int style, int size) {
        return new Font(fontName, style, size);
    }
}
