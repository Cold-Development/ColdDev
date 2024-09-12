package dev.padrewin.coldplugin.command.framework;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.framework.command.optional.OneArgsCommand;
import dev.padrewin.coldplugin.command.framework.command.optional.TwoArgsCommand;
import dev.padrewin.coldplugin.manager.AbstractLocaleManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandExecutionOptionalTest {

    private ColdPlugin coldPlugin;
    private CommandSender sender;

    @BeforeEach
    public void setup() {
        this.coldPlugin = mock(ColdPlugin.class);
        this.sender = mock(CommandSender.class);

        AbstractLocaleManager localeManager = mock(AbstractLocaleManager.class);

        when(this.coldPlugin.getName()).thenReturn("TestPlugin");
        when(this.coldPlugin.getManager(AbstractLocaleManager.class)).thenReturn(localeManager);
        Answer<Object> answer = invocation -> { // Forwards messages sent through the LocaleManager straight to the player as the message key
            CommandSender sender = invocation.getArgument(0);
            String messageKey = invocation.getArgument(1);
            sender.sendMessage(messageKey);
            return null;
        };
        doAnswer(answer).when(localeManager).sendCommandMessage(any(), any(), any());
        doAnswer(answer).when(localeManager).sendCommandMessage(any(), any());
    }

    @Test
    public void testExecution_optional_oneArgs_zeroValid() {
        BaseColdCommand command = new OneArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyNull();
    }

    @Test
    public void testExecution_optional_oneArgs_oneValid() {
        BaseColdCommand command = new OneArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "VALUE_1";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("VALUE_1");
    }

    @Test
    public void testExecution_optional_oneArgs_invalid() {
        BaseColdCommand command = new OneArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "bad";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    @Test
    public void testExecution_optional_twoArgs_zeroValid() {
        BaseColdCommand command = new TwoArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyNull(2);
    }

    @Test
    public void testExecution_optional_twoArgs_oneValid() {
        BaseColdCommand command = new TwoArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "VALUE_1";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("VALUE_1");
        this.verifyNull();
    }

    @Test
    public void testExecution_optional_twoArgs_twoValid() {
        BaseColdCommand command = new TwoArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "VALUE_1 VALUE_2";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("VALUE_1");
        this.verifySuccess("VALUE_2");
    }

    @Test
    public void testExecution_optional_twoArgs_oneInvalid() {
        BaseColdCommand command = new TwoArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "bad VALUE_2";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    @Test
    public void testExecution_optional_twoArgs_twoInvalid() {
        BaseColdCommand command = new TwoArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "VALUE_1 bad";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    @Test
    public void testExecution_optional_twoArgs_bothInvalid() {
        BaseColdCommand command = new TwoArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "bad bad";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    private void verifySuccess(String expected) {
        verify(this.sender).sendMessage(eq(expected));
    }

    private void verifyNull() {
        this.verifyNull(1);
    }

    private void verifyNull(int amount) {
        verify(this.sender, times(amount)).sendMessage(eq("null"));
    }

    private void verifyInvalid() {
        verify(this.sender).sendMessage(eq("invalid-argument"));
    }

    private String[] splitInput(String input) {
        return StringUtils.split(input, ' ');
    }

}
