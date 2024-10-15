package dev.padrewin.colddev.command.framework;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.command.suggest.SuggestionCommand;
import dev.padrewin.colddev.command.framework.handler.TestArgumentHandler;
import dev.padrewin.colddev.command.framework.model.TestEnum;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandSuggestionTest {

    private ColdPlugin ColdPlugin;
    private CommandSender sender;

    @BeforeEach
    public void setup() {
        this.ColdPlugin = mock(ColdPlugin.class);
        this.sender = mock(CommandSender.class);

        when(this.ColdPlugin.getName()).thenReturn("TestPlugin");
    }

    @Test
    public void testSuggestion_arg1_empty() {
        BaseColdCommand command = new SuggestionCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(TestArgumentHandler.SUGGESTIONS, suggestions);
    }

    @Test
    public void testSuggestion_arg2_empty() {
        BaseColdCommand command = new SuggestionCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "apple ";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Stream.of(TestEnum.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList()), suggestions);
    }

    @Test
    public void testSuggestion_arg1_partial() {
        BaseColdCommand command = new SuggestionCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "app";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Arrays.asList("apple", "applause"), suggestions);
    }

    @Test
    public void testSuggestion_arg2_partial() {
        BaseColdCommand command = new SuggestionCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "apple val";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Arrays.asList("value_1", "value_2"), suggestions);
    }

    @Test
    public void testSuggestion_arg1_complete() {
        BaseColdCommand command = new SuggestionCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "apple";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Collections.singletonList("apple"), suggestions);
    }

    @Test
    public void testSuggestion_arg2_complete() {
        BaseColdCommand command = new SuggestionCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "apple value_1";

        List<String> suggestions = commandWrapper.tabComplete(this.sender, command.getName(), this.splitInput(input));

        assertEquals(Collections.singletonList("value_1"), suggestions);
    }

    private String[] splitInput(String input) {
        return input.split(" ", -1);
    }

}
