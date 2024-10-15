package dev.padrewin.colddev.command.framework;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.command.required.NoArgsCommand;
import dev.padrewin.colddev.command.framework.command.required.OneArgsCommand;
import dev.padrewin.colddev.command.framework.command.required.TwoArgsCommand;
import dev.padrewin.colddev.manager.AbstractLocaleManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandExecutionRequiredTest {

    private ColdPlugin ColdPlugin;
    private CommandSender sender;

    @BeforeEach
    public void setup() {
        this.ColdPlugin = mock(ColdPlugin.class);
        this.sender = mock(CommandSender.class);

        AbstractLocaleManager localeManager = mock(AbstractLocaleManager.class);

        when(this.ColdPlugin.getName()).thenReturn("TestPlugin");
        when(this.ColdPlugin.getManager(AbstractLocaleManager.class)).thenReturn(localeManager);
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
    public void testExecution_required_noArgs() {
        BaseColdCommand command = new NoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess(NoArgsCommand.SUCCESS_OUTPUT);
    }

    @Test
    public void testExecution_required_oneArgs_valid() {
        BaseColdCommand command = new OneArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "VALUE_1";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("VALUE_1");
    }

    @Test
    public void testExecution_required_oneArgs_valid_extraArgs() {
        BaseColdCommand command = new OneArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "VALUE_1 extra values";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("VALUE_1");
    }

    @Test
    public void testExecution_required_oneArgs_missing() {
        BaseColdCommand command = new OneArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyMissing();
    }

    @Test
    public void testExecution_required_oneArgs_invalid() {
        BaseColdCommand command = new OneArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "bad";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    @Test
    public void testExecution_required_twoArgs_valid() {
        BaseColdCommand command = new TwoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "VALUE_1 VALUE_2";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("VALUE_1");
        this.verifySuccess("VALUE_2");
    }

    @Test
    public void testExecution_required_twoArgs_valid_extraArgs() {
        BaseColdCommand command = new TwoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "VALUE_1 VALUE_2 extra args";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("VALUE_1");
        this.verifySuccess("VALUE_2");
    }

    @Test
    public void testExecution_required_twoArgs_missingOne() {
        BaseColdCommand command = new TwoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "VALUE_1";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyMissing();
    }

    @Test
    public void testExecution_required_twoArgs_missingTwo() {
        BaseColdCommand command = new TwoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyMissing();
    }

    @Test
    public void testExecution_required_twoArgs_oneInvalid() {
        BaseColdCommand command = new TwoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "bad VALUE_2";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    @Test
    public void testExecution_required_twoArgs_twoInvalid() {
        BaseColdCommand command = new TwoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "VALUE_1 bad";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    @Test
    public void testExecution_required_twoArgs_bothInvalid() {
        BaseColdCommand command = new TwoArgsCommand(this.ColdPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.ColdPlugin, command);

        String input = "bad bad";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyInvalid();
    }

    private void verifySuccess(String expected) {
        verify(this.sender).sendMessage(eq(expected));
    }

    private void verifyMissing() {
        verify(this.sender).sendMessage(eq("command-usage"));
    }

    private void verifyInvalid() {
        verify(this.sender).sendMessage(eq("invalid-argument"));
    }

    private String[] splitInput(String input) {
        return StringUtils.split(input, ' ');
    }

}
