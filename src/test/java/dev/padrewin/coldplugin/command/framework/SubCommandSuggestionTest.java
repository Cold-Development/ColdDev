package dev.padrewin.coldplugin.command.framework;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.framework.command.sub.TestSubCommand;
import dev.padrewin.coldplugin.command.framework.model.TestEnum;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubCommandSuggestionTest {

    private ColdPlugin coldPlugin;
    private CommandSender sender;

    @BeforeEach
    public void setup() {
        this.coldPlugin = mock(ColdPlugin.class);
        this.sender = mock(CommandSender.class);

        when(this.coldPlugin.getName()).thenReturn("TestPlugin");
    }

    @Test
    public void testSuggestion_arg1_empty() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Stream.of(TestEnum.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList()), suggestions);
    }

    @Test
    public void testSuggestion_arg1_partial() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "val";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Arrays.asList("value_1", "value_2"), suggestions);
    }

    @Test
    public void testSuggestion_arg1_complete() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "value_1";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Collections.singletonList("value_1"), suggestions);
    }

    @Test
    public void testSuggestion_arg2_empty() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "value_1 ";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Arrays.asList("option1", "option2", "secret-option3"), suggestions);
    }

    @Test
    public void testSuggestion_arg2_partial() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "value_1 opt";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Arrays.asList("option1", "option2"), suggestions);
    }

    @Test
    public void testSuggestion_arg2_complete() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "value_1 option_1";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void testSuggestion_arg3_complete_space() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "value_1 secret-option3 ";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Arrays.asList("true", "false"), suggestions);
    }

    @Test
    public void testSuggestion_arg3_complete_partial() {
        BaseColdCommand command = new TestSubCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "value_1 secret-option3 t";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Collections.singletonList("true"), suggestions);
    }

    private String[] splitInput(String input) {
        return input.split(" ", -1);
    }

}
