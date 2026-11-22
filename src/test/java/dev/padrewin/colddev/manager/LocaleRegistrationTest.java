package dev.padrewin.colddev.manager;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.locale.Locale;
import dev.padrewin.colddev.locale.YamlFileLocale;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocaleRegistrationTest {
    @TempDir Path folder;

    private void register(String yaml) throws Exception {
        ColdPlugin plugin = mock(ColdPlugin.class);
        when(plugin.getDataFolder()).thenReturn(folder.toFile());
        AbstractLocaleManager manager = new AbstractLocaleManager(plugin) {};
        Files.createDirectories(folder.resolve("locale"));
        Method register = AbstractLocaleManager.class.getDeclaredMethod("registerLocale", Locale.class);
        register.setAccessible(true);
        register.invoke(manager, new YamlFileLocale("en_US", new StringReader(yaml)));
    }

    @Test
    void createsLocaleWithBareHashComment() throws Exception {
        register("#\n# Heading\nmessage: hello\n");
        Path file = folder.resolve("locale/en_US.yml");
        assertEquals("hello", YamlConfiguration.loadConfiguration(file.toFile()).getString("message"));
        assertTrue(Files.readString(file).lines().anyMatch(line -> line.trim().equals("#")));
        assertTrue(Files.readString(file).contains("# Heading"));
    }

    @Test
    void preservesFirstCharacterOfUnspacedComments() throws Exception {
        register("#Header\n# Spaced\n#  Indented\nmessage: hello\n");
        String result = Files.readString(folder.resolve("locale/en_US.yml"));
        assertTrue(result.contains("# Header"), result);
        assertTrue(result.contains("# Spaced"), result);
        assertTrue(result.contains("#  Indented"), result);
    }

    @Test
    void addsMissingMessagesWithoutChangingOwnersText() throws Exception {
        Path file = folder.resolve("locale/en_US.yml");
        Files.createDirectories(file.getParent());
        String original = "# Owner comment\nmessage: 'My message' # keep this\n";
        Files.writeString(file, original);
        String defaults = "#\n#Header\nmessage: default\nnew-message: added\n";
        register(defaults);
        String updated = Files.readString(file);
        assertTrue(updated.contains(original), updated);
        assertEquals("added", YamlConfiguration.loadConfiguration(file.toFile()).getString("new-message"));
        register(defaults);
        assertEquals(updated, Files.readString(file), "Repeated registration must not rewrite the file");
    }
}
