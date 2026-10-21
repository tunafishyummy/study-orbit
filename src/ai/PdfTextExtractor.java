package ai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

// best effort text reader for text based pdf files
public final class PdfTextExtractor {
    private PdfTextExtractor() {}

    public static String extract(Path file) throws IOException {
        byte[] data = Files.readAllBytes(file);
        StringBuilder out = new StringBuilder();
        int from = 0;
        while (true) {
            int stream = indexOf(data, "stream", from);
            if (stream < 0) {
                break;
            }
            int start = stream + 6;
            if (start < data.length && data[start] == '\r') {
                start++;
            }
            if (start < data.length && data[start] == '\n') {
                start++;
            }
            int end = indexOf(data, "endstream", start);
            if (end < 0) {
                break;
            }
            byte[] raw = new byte[end - start];
            System.arraycopy(data, start, raw, 0, raw.length);
            byte[] content = inflate(raw);
            if (content == null) {
                content = raw;
            }
            String text = new String(content, StandardCharsets.ISO_8859_1);
            if (text.contains("BT") && (text.contains("Tj") || text.contains("TJ"))) {
                appendText(text, out);
            }
            from = end + 9;
        }
        return tidy(out.toString());
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

    private static int indexOf(byte[] data, String needle, int from) {
        byte[] target = needle.getBytes(StandardCharsets.ISO_8859_1);
        outer:
        for (int index = Math.max(0, from); index <= data.length - target.length; index++) {
            for (int offset = 0; offset < target.length; offset++) {
                if (data[index + offset] != target[offset]) {
                    continue outer;
                }
            }
            return index;
        }
        return -1;
    }

    private static byte[] inflate(byte[] raw) {
        Inflater inflater = new Inflater();
        inflater.setInput(raw);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        try {
            while (inflater.finished() == false) {
                int count = inflater.inflate(buffer);
                if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    break;
                }
                out.write(buffer, 0, count);
            }
        } catch (DataFormatException exception) {
            return null;
        } finally {
            inflater.end();
        }
        if (out.size() == 0) {
            return null;
        }
        return out.toByteArray();
    }

    private static void appendText(String content, StringBuilder out) {
        Integer lastNumber = null;
        int index = 0;
        while (index < content.length()) {
            char current = content.charAt(index);
            if (current == '(') {
                int close = stringEnd(content, index);
                if (close < 0) {
                    break;
                }
                if (lastNumber != null && lastNumber < -140) {
                    out.append(' ');
                }
                out.append(unescape(content.substring(index + 1, close)));
                lastNumber = null;
                index = close + 1;
            } else if (current == '<' && index + 1 < content.length() && content.charAt(index + 1) != '<') {
                int close = content.indexOf('>', index);
                if (close < 0) {
                    break;
                }
                appendHex(content.substring(index + 1, close), out);
                lastNumber = null;
                index = close + 1;
            } else if (Character.isDigit(current)
                    || (current == '-' && index + 1 < content.length() && Character.isDigit(content.charAt(index + 1)))) {
                int end = index + 1;
                while (end < content.length() && (Character.isDigit(content.charAt(end)) || content.charAt(end) == '.')) {
                    end++;
                }
                try {
                    lastNumber = (int) Double.parseDouble(content.substring(index, end));
                } catch (NumberFormatException exception) {
                    lastNumber = null;
                }
                index = end;
            } else if (content.startsWith("Td", index) || content.startsWith("TD", index)
                    || content.startsWith("T*", index) || content.startsWith("ET", index) || current == '\'') {
                out.append('\n');
                lastNumber = null;
                if (current == '\'') {
                    index = index + 1;
                } else {
                    index = index + 2;
                }
            } else {
                index++;
            }
        }
    }

    private static void appendHex(String hex, StringBuilder out) {
        String cleaned = hex.replaceAll("[^0-9A-Fa-f]", "");
        if (cleaned.length() % 2 == 1) {
            cleaned = cleaned + "0";
        }
        for (int index = 0; index + 1 < cleaned.length(); index += 2) {
            out.append((char) Integer.parseInt(cleaned.substring(index, index + 2), 16));
        }
    }

    private static String unescape(String text) {
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current != '\\') {
                out.append(current);
                continue;
            }
            index++;
            if (index >= text.length()) {
                break;
            }
            char escaped = text.charAt(index);
            switch (escaped) {
                case 'n':
                    out.append('\n');
                    break;
                case 'r':
                    out.append('\r');
                    break;
                case 't':
                    out.append('\t');
                    break;
                case 'b':
                    out.append('\b');
                    break;
                case 'f':
                    out.append('\f');
                    break;
                case '\\':
                    out.append('\\');
                    break;
                case '(':
                    out.append('(');
                    break;
                case ')':
                    out.append(')');
                    break;
                default:
                    if (escaped >= '0' && escaped <= '7') {
                        int value = escaped - '0';
                        int extra = 0;
                        while (extra < 2 && index + 1 < text.length()
                                && text.charAt(index + 1) >= '0' && text.charAt(index + 1) <= '7') {
                            index++;
                            value = value * 8 + (text.charAt(index) - '0');
                            extra++;
                        }
                        out.append((char) value);
                    } else {
                        out.append(escaped);
                    }
            }
        }
        return out.toString();
    }

    private static int stringEnd(String text, int open) {
        int depth = 1;
        for (int index = open + 1; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '\\') {
                index++;
                continue;
            }
            if (current == '(') {
                depth++;
            }
            if (current == ')') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
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
