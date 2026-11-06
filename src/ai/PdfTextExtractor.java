package ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;

// reads text from a pdf with pdfbox
public final class PdfTextExtractor {
    private PdfTextExtractor() {}

    public static String extract(Path file) throws IOException {
        try (PDDocument document = PDDocument.load(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return tidy(stripper.getText(document));
        }
    }

    public static boolean looksGarbled(String text) {
        if (text == null || text.isBlank()) {
            return true;
        }
        int letters = 0;
        int spaces = 0;
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (Character.isLetter(current)) {
                letters++;
            } else if (current == ' ') {
                spaces++;
            }
        }
        double letterRatio = letters / (double) text.length();
        double spaceRatio = spaces / (double) text.length();
        return letterRatio < 0.55 || spaceRatio < 0.04;
    }

    private static String tidy(String text) {
        String out = text.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F]", "");
        out = out.replaceAll("[ \\t]+", " ");
        out = out.replaceAll(" ?\\n ?", "\n");
        out = out.replaceAll("\\n{3,}", "\n\n");
        out = out.replaceAll("(\\p{L})-\\n(\\p{Ll})", "$1$2");
        return out.trim();
    }
}
