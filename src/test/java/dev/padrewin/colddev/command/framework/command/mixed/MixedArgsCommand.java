package dev.padrewin.colddev.command.framework.command.mixed;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.command.framework.ArgumentCondition;

public class MixedArgsCommand extends BaseColdCommand {

    public static final String TEST_PERMISSION = "colddev.test.mixedargs";

    public MixedArgsCommand(ColdPlugin ColdPlugin) {
        super(ColdPlugin);
    }

    @ColdExecutable
    public void execute(CommandContext context, String value, String value2) {
        context.getSender().sendMessage(value == null ? "null" : value);
        context.getSender().sendMessage(value2);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test")
                .arguments(ArgumentsDefinition.builder()
                        .optional("arg1", ArgumentHandlers.forValues(String.class, "alice", "bob"), ArgumentCondition.hasPermission(TEST_PERMISSION))
                        .required("arg2", ArgumentHandlers.forValues(String.class, "on", "off"))
                        .build())
                .build();
    }

}
