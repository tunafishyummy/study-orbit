package data;

import java.util.regex.Pattern;

// turns markdown-style note text into plain document text
public final class PlainText {
    private static final Pattern FENCE = Pattern.compile("^\\s*```.*$", Pattern.MULTILINE);
    private static final Pattern HEADING = Pattern.compile("^\\s{0,3}#{1,6}\\s+", Pattern.MULTILINE);
    private static final Pattern QUOTE = Pattern.compile("^\\s{0,3}>\\s?", Pattern.MULTILINE);
    private static final Pattern RULE = Pattern.compile("^\\s*([-*_])\\1{2,}\\s*$", Pattern.MULTILINE);
    private static final Pattern BULLET = Pattern.compile("^\\s*[-*+]\\s+", Pattern.MULTILINE);
    private static final Pattern LINK = Pattern.compile("!?\\[([^\\]]*)\\]\\([^)]*\\)");
    private static final Pattern BOLD_STAR = Pattern.compile("\\*\\*([^*\\n]+?)\\*\\*");
    private static final Pattern BOLD_UNDER = Pattern.compile("(?<!\\w)__([^_\\n]+?)__(?!\\w)");
    private static final Pattern ITALIC_STAR = Pattern.compile("(?<!\\*)\\*(?!\\*)([^*\\n]+?)(?<!\\*)\\*(?!\\*)");
    private static final Pattern ITALIC_UNDER = Pattern.compile("(?<!\\w)_(?!_)([^_\\n]+?)(?<!_)_(?!\\w)");
    private static final Pattern MULTI_BLANK = Pattern.compile("\\n{3,}");
    private static final Pattern TRAILING_SPACE = Pattern.compile("[ \\t]+\\n");

    private PlainText() {}

    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        String out = text.replace("\r\n", "\n").replace('\r', '\n');
        out = FENCE.matcher(out).replaceAll("");
        out = HEADING.matcher(out).replaceAll("");
        out = QUOTE.matcher(out).replaceAll("");
        out = RULE.matcher(out).replaceAll("");
        out = BULLET.matcher(out).replaceAll("");
        out = LINK.matcher(out).replaceAll("$1");
        out = BOLD_STAR.matcher(out).replaceAll("$1");
        out = BOLD_UNDER.matcher(out).replaceAll("$1");
        out = ITALIC_STAR.matcher(out).replaceAll("$1");
        out = ITALIC_UNDER.matcher(out).replaceAll("$1");
        out = out.replace("`", "");
        out = MULTI_BLANK.matcher(out).replaceAll("\n\n");
        out = TRAILING_SPACE.matcher(out).replaceAll("\n");
        return out.trim();
    }
}
