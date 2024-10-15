package dev.padrewin.colddev.command.framework;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.command.mixed.MixedArgsCommand;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandSuggestionMixedTest {

    private ColdPlugin ColdPlugin;
    private CommandSender sender;

    @BeforeEach
    public void setup() {
        this.ColdPlugin = mock(ColdPlugin.class);
        this.sender = mock(CommandSender.class);

        when(this.ColdPlugin.getName()).thenReturn("TestPlugin");
    }

    @Test
    public void testSuggestion_arg1_empty_hasPermission() {
        BaseColdCommand command = new MixedArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        when(this.sender.hasPermission(MixedArgsCommand.TEST_PERMISSION)).thenReturn(true);

        String input = "";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(new HashSet<>(Arrays.asList("on", "off", "alice", "bob")), new HashSet<>(suggestions));
    }

    @Test
    public void testSuggestion_arg1_empty_noPermission() {
        BaseColdCommand command = new MixedArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(new HashSet<>(Arrays.asList("on", "off")), new HashSet<>(suggestions));
    }

    private String[] splitInput(String input) {
        return input.split(" ", -1);
    }

}
