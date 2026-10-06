package dev.padrewin.colddev.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adds missing keys to an existing YAML file without re-saving it: every key of the default version
 * that the file doesn't have is inserted together with its comments, after the key it follows in the
 * default version and with the file's own indentation.
 * <p>
 * The file is edited line by line, so everything that was already in it (values, comments, including
 * the ones at the end of a line, spacing, quotes, order) stays exactly as it was. Loading and saving
 * through {@link CommentedFileConfiguration} instead would lose the end-of-line comments and reformat
 * the file. Understands block style YAML (keys, nested sections, lists), which is what config and
 * locale files use.
 */
public final class ConfigUpdater {

    private ConfigUpdater() {
    }

    /**
     * @param file     the existing file to complete
     * @param defaults the default version of the file
     * @return the paths of the keys that were added (empty if the file was already complete)
     */
    public static List<String> update(File file, InputStream defaults) throws IOException {
        List<String> defaultLines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(defaults, StandardCharsets.UTF_8))) {
            defaultLines = reader.lines().collect(Collectors.toCollection(ArrayList::new));
        }
        return update(file, defaultLines);
    }

    /**
     * @param file         the existing file to complete
     * @param defaultLines the lines of the default version of the file
     * @return the paths of the keys that were added (empty if the file was already complete)
     */
    public static List<String> update(File file, List<String> defaultLines) throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        List<String> added = new ArrayList<>();
        merge(defaultLines, parse(defaultLines, 0, defaultLines.size(), 0, ""), lines, null, added);

        if (!added.isEmpty()) {
            write(file, lines);
        }
        return added;
    }

    // ------------------------------------------------------------------------------------------------
    // Merging

    /**
     * A key of the default file, with the lines it covers.
     */
    private static final class Entry {
        String path;
        int commentStart;
        int keyLine;
        int end;
        int indent;
        List<Entry> children = new ArrayList<>();
    }

    private static List<Entry> parse(List<String> lines, int from, int to, int indent, String parentPath) {
        List<Entry> entries = new ArrayList<>();
        int i = from;
        while (i < to) {
            String line = lines.get(i);
            String key = keyOf(line);
            if (key == null || indentOf(line) != indent) {
                i++;
                continue;
            }

            Entry entry = new Entry();
            entry.path = parentPath.isEmpty() ? key : parentPath + "." + key;
            entry.keyLine = i;
            entry.indent = indent;
            entry.end = Math.min(blockEnd(lines, i, indent), to);
            entry.commentStart = Math.max(from, commentStart(lines, i, indent));

            int childIndent = childIndent(lines, i + 1, entry.end);
            if (childIndent > indent) {
                entry.children = parse(lines, i + 1, entry.end, childIndent, entry.path);
            }

            entries.add(entry);
            i = entry.end;
        }
        return entries;
    }

    private static void merge(List<String> defaults, List<Entry> entries, List<String> lines, Entry parent, List<String> added) {
        Entry previous = null;
        for (Entry entry : entries) {
            Block existing = findBlock(lines, entry.path);
            if (existing == null) {
                if (insert(defaults, entry, previous, parent, lines)) {
                    added.add(entry.path);
                }
            } else if (!entry.children.isEmpty() && isSection(lines, existing)) {
                merge(defaults, entry.children, lines, entry, added);
            }
            previous = entry;
        }
    }

    /**
     * Inserts the entry (comments, key and everything under it) after its previous sibling, or as the
     * first key of its section when it has none.
     *
     * @return false if it couldn't be placed (its section is a plain value in the file)
     */
    private static boolean insert(List<String> defaults, Entry entry, Entry previous, Entry parent, List<String> lines) {
        int targetIndent = 0;
        Block parentBlock = null;
        if (parent != null) {
            parentBlock = findBlock(lines, parent.path);
            if (parentBlock == null || !isSection(lines, parentBlock)) {
                return false;
            }
            int childIndent = childIndent(lines, parentBlock.keyLine + 1, parentBlock.end);
            targetIndent = childIndent >= 0 ? childIndent : parentBlock.indent + (entry.indent - parent.indent);
        }

        Block previousBlock = previous != null ? findBlock(lines, previous.path) : null;
        int index;
        if (previousBlock != null) {
            index = previousBlock.end;
        } else if (parentBlock != null) {
            index = parentBlock.keyLine + 1;
        } else {
            index = firstTopLevelKey(lines);
        }

        List<String> block = new ArrayList<>();
        boolean blankBefore = entry.commentStart > 0 && defaults.get(entry.commentStart - 1).isBlank();
        boolean blankAfter = entry.end < defaults.size() && defaults.get(entry.end).isBlank();
        if (previousBlock != null && blankBefore) {
            block.add("");
        }
        for (int i = entry.commentStart; i < entry.end; i++) {
            block.add(reindent(defaults.get(i), targetIndent - entry.indent));
        }
        if (previousBlock == null && blankAfter) {
            block.add("");
        }
        lines.addAll(index, block);
        return true;
    }

    /**
     * A key with nothing after the colon (its value is the indented block below it).
     */
    private static boolean isSection(List<String> lines, Block block) {
        String line = lines.get(block.keyLine).trim();
        String afterColon = line.substring(line.indexOf(':') + 1).trim();
        return afterColon.isEmpty() || afterColon.startsWith("#");
    }

    /**
     * @return where the first top-level key starts (its comments included), or the end of the file
     */
    private static int firstTopLevelKey(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (indentOf(lines.get(i)) == 0 && keyOf(lines.get(i)) != null) {
                return commentStart(lines, i, 0);
            }
        }
        return lines.size();
    }

    private static String reindent(String line, int delta) {
        if (line.isBlank()) {
            return "";
        }
        if (delta > 0) {
            return " ".repeat(delta) + line;
        }
        int remove = Math.min(-delta, indentOf(line));
        return line.substring(remove);
    }

    // ------------------------------------------------------------------------------------------------
    // Reading the structure of the file

    /**
     * Where a key is in a file: its comment block, its own line and everything under it.
     */
    private static final class Block {
        final int keyLine;
        final int end;
        final int indent;

        Block(int keyLine, int end, int indent) {
            this.keyLine = keyLine;
            this.end = end;
            this.indent = indent;
        }
    }

    /**
     * @param path a key path such as {@code mysql-settings.enabled}
     * @return where the key is, or null if the file doesn't have it
     */
    private static Block findBlock(List<String> lines, String path) {
        String[] keys = path.split("\\.");

        int from = 0;
        int to = lines.size();
        int indent = 0;
        for (int k = 0; k < keys.length; k++) {
            int keyLine = -1;
            for (int i = from; i < to; i++) {
                String line = lines.get(i);
                if (indentOf(line) == indent && keys[k].equals(keyOf(line))) {
                    keyLine = i;
                    break;
                }
            }
            if (keyLine < 0) {
                return null;
            }
            int end = blockEnd(lines, keyLine, indent);
            if (k == keys.length - 1) {
                return new Block(keyLine, end, indent);
            }
            from = keyLine + 1;
            to = end;
            indent = childIndent(lines, from, to);
            if (indent < 0) {
                return null;
            }
        }
        return null;
    }

    /**
     * @return the first line of the comments directly above the key, at the same indentation
     */
    private static int commentStart(List<String> lines, int keyLine, int indent) {
        int start = keyLine;
        while (start > 0 && lines.get(start - 1).trim().startsWith("#") && indentOf(lines.get(start - 1)) == indent) {
            start--;
        }
        return start;
    }

    /**
     * @return the index after the last line of the block starting at {@code start}: the block ends at
     * the next line (key or comment) indented as much as the key or less, except list items ({@code - x})
     * which may sit at the key's own indentation. Blank lines right before that line are left outside,
     * so the spacing between sections is kept.
     */
    private static int blockEnd(List<String> lines, int start, int indent) {
        int end = start + 1;
        while (end < lines.size()) {
            String line = lines.get(end);
            boolean listItem = indentOf(line) == indent && line.trim().startsWith("- ");
            if (!line.isBlank() && indentOf(line) <= indent && !listItem) {
                break;
            }
            end++;
        }
        while (end > start + 1 && lines.get(end - 1).isBlank()) {
            end--;
        }
        return end;
    }

    /**
     * @return the indentation of the first key in the range, or -1 if there's none
     */
    private static int childIndent(List<String> lines, int from, int to) {
        for (int i = from; i < to; i++) {
            String line = lines.get(i);
            if (!line.isBlank() && !line.trim().startsWith("#") && !line.trim().startsWith("- ")) {
                return indentOf(line);
            }
        }
        return -1;
    }

    /**
     * @return the key on this line ({@code key: value} or {@code key:}), or null if it isn't a key line
     */
    private static String keyOf(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("- ")) {
            return null;
        }
        int colon = trimmed.indexOf(':');
        if (colon <= 0 || (colon + 1 < trimmed.length() && trimmed.charAt(colon + 1) != ' ')) {
            return null;
        }
        String key = trimmed.substring(0, colon).trim();
        if (key.length() >= 2 && (key.startsWith("\"") && key.endsWith("\"") || key.startsWith("'") && key.endsWith("'"))) {
            key = key.substring(1, key.length() - 1);
        }
        return key;
    }

    private static int indentOf(String line) {
        int indent = 0;
        while (indent < line.length() && line.charAt(indent) == ' ') {
            indent++;
        }
        return indent;
    }

    /**
     * Keeps the file's own line endings (CRLF or LF), so only the added lines differ.
     */
    private static void write(File file, List<String> lines) throws IOException {
        String original = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String separator = original.contains("\r\n") ? "\r\n" : "\n";
        Files.write(file.toPath(), (String.join(separator, lines) + separator).getBytes(StandardCharsets.UTF_8));
    }

}
