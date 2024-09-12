package dev.padrewin.coldplugin.command.framework;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.framework.command.mixed.MixedArgsCommand;
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

public class CommandExecutionMixedTest {

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
    public void testExecution_mixed_oneArgs_hasPermission() {
        BaseColdCommand command = new MixedArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        when(this.sender.hasPermission(MixedArgsCommand.TEST_PERMISSION)).thenReturn(true);

        String input = "on";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyNull();
        this.verifySuccess("on");
    }

    @Test
    public void testExecution_mixed_oneArgs_noPermission() {
        BaseColdCommand command = new MixedArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "on";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifyNull();
        this.verifySuccess("on");
    }

    @Test
    public void testExecution_mixed_twoArgs_hasPermission() {
        BaseColdCommand command = new MixedArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        when(this.sender.hasPermission(MixedArgsCommand.TEST_PERMISSION)).thenReturn(true);

        String input = "bob off";

        commandWrapper.execute(this.sender, command.getName(), this.splitInput(input));

        this.verifySuccess("bob");
        this.verifySuccess("off");
    }

    @Test
    public void testExecution_mixed_twoArgs_noPermission() {
        BaseColdCommand command = new MixedArgsCommand(this.coldPlugin);
        ColdCommandWrapper commandWrapper = new ColdCommandWrapper(this.coldPlugin, command);

        String input = "bob off";

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
