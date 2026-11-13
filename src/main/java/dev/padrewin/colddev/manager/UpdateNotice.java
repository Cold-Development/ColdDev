package dev.padrewin.colddev.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a GitHub release (the JSON of {@code /releases/latest}) and the optional message for server
 * owners hidden in its notes.
 * <p>
 * The message is an HTML comment in the release notes, so it doesn't show on the GitHub page:
 * <pre>
 * &lt;!-- server-message
 * &amp;7Important fix for Vault payments.
 * &amp;cPlease update as soon as possible.
 * --&gt;
 * </pre>
 * Only colours are kept: {@code &a}, {@code &#RRGGBB}, {@code <#RRGGBB>}, {@code <g:#..:#..>} gradients and
 * {@code <r>} rainbows. Any other {@code <tag>} is removed, and the message is limited to
 * {@link #MAX_LINES} lines of {@link #MAX_LINE_LENGTH} characters, so a release can't flood a console or chat.
 */
public final class UpdateNotice {

    public static final int MAX_LINES = 5;
    public static final int MAX_LINE_LENGTH = 200;

    private static final Pattern BLOCK = Pattern.compile("<!--\\s*server-message\\b(.*?)-->",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG = Pattern.compile("<[^<>]*>");
    // The tags HexUtils turns into colours, and nothing else
    private static final Pattern COLOUR_TAG = Pattern.compile("<(?:"
            + "#[A-Fa-f0-9]{6}"
            + "|(?:gradient|g)(?:#\\d+)?(?::#(?:[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,}(?::(?:l|L|loop))?"
            + "|(?:rainbow|r)(?:#\\d+)?(?::\\d*\\.?\\d+)?(?::\\d*\\.?\\d+)?(?::(?:l|L|loop))?"
            + ")>");

    private UpdateNotice() {
    }

    /**
     * A release: its tag (the version) and its notes.
     *
     * @param tag  the tag name, or null if the response doesn't have one
     * @param body the release notes, or null if there are none
     */
    public record Release(String tag, String body) {

        /**
         * @return the message for server owners in the notes; empty if there is none
         */
        public List<String> message() {
            return UpdateNotice.extract(this.body);
        }
    }

    /**
     * @param json the response of GitHub's {@code /repos/{owner}/{repo}/releases/latest}
     */
    public static Release parseRelease(String json) {
        return new Release(readStringField(json, "tag_name"), readStringField(json, "body"));
    }

    /**
     * Reads a string field of a JSON object, with its escapes decoded. Good enough for GitHub's release
     * object, whose {@code tag_name} and {@code body} are top-level fields that no nested object repeats.
     *
     * @return the value, or null if the field is missing, null or not a string
     */
    static String readStringField(String json, String key) {
        if (json == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*").matcher(json);
        if (!matcher.find() || matcher.end() >= json.length() || json.charAt(matcher.end()) != '"') {
            return null;
        }

        StringBuilder value = new StringBuilder();
        for (int i = matcher.end() + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                return value.toString();
            }
            if (c != '\\') {
                value.append(c);
                continue;
            }
            if (++i >= json.length()) {
                return null;
            }
            char escaped = json.charAt(i);
            switch (escaped) {
                case 'n' -> value.append('\n');
                case 'r' -> value.append('\r');
                case 't' -> value.append('\t');
                case 'b' -> value.append('\b');
                case 'f' -> value.append('\f');
                case 'u' -> {
                    if (i + 4 >= json.length()) {
                        return null;
                    }
                    try {
                        value.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    i += 4;
                }
                default -> value.append(escaped); // \" \\ \/
            }
        }
        return null; // unterminated string
    }

    /**
     * @param body release notes
     * @return the lines of the {@code server-message} block, cleaned up; empty if there is no block
     */
    public static List<String> extract(String body) {
        if (body == null) {
            return Collections.emptyList();
        }
        Matcher matcher = BLOCK.matcher(body);
        if (!matcher.find()) {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        for (String line : matcher.group(1).split("\\R", -1)) {
            lines.add(sanitize(line));
        }
        // Blank lines around the message come from the comment's own layout
        while (!lines.isEmpty() && lines.get(0).isEmpty()) {
            lines.remove(0);
        }
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines.size() > MAX_LINES ? new ArrayList<>(lines.subList(0, MAX_LINES)) : lines;
    }

    /**
     * Keeps the colours of a line and nothing that could be abused: control characters and section signs
     * (raw formatting codes) are removed, so are tags other than colours, and the line is shortened to
     * {@link #MAX_LINE_LENGTH}.
     */
    static String sanitize(String line) {
        StringBuilder clean = new StringBuilder(line.length());
        int[] codePoints = line.codePoints().toArray();
        for (int i = 0; i < codePoints.length; i++) {
            int c = codePoints[i] == '\t' ? ' ' : codePoints[i];
            if (c == 0xA7) { // the section sign
                i++; // a raw formatting code: drop its letter too, so it doesn't show up as text
                continue;
            }
            if (c >= 0x20 && c != 0x7F && !(c >= 0x80 && c <= 0x9F)) {
                clean.appendCodePoint(c);
            }
        }

        Matcher tags = TAG.matcher(clean);
        StringBuilder result = new StringBuilder(clean.length());
        while (tags.find()) {
            tags.appendReplacement(result, COLOUR_TAG.matcher(tags.group()).matches()
                    ? Matcher.quoteReplacement(tags.group())
                    : "");
        }
        tags.appendTail(result);

        String text = result.toString().strip();
        if (text.codePointCount(0, text.length()) > MAX_LINE_LENGTH) {
            text = text.substring(0, text.offsetByCodePoints(0, MAX_LINE_LENGTH));
        }
        return text;
    }

}
