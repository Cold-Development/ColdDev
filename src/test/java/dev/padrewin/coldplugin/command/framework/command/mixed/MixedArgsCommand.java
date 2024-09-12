package dev.padrewin.coldplugin.command.framework.command.mixed;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.argument.ArgumentHandlers;
import dev.padrewin.coldplugin.command.framework.ArgumentsDefinition;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.CommandInfo;
import dev.padrewin.coldplugin.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldplugin.command.framework.ArgumentCondition;
import java.util.Objects;

public class MixedArgsCommand extends BaseColdCommand {

    public static final String TEST_PERMISSION = "colddev.test.mixedargs";

    public MixedArgsCommand(ColdPlugin coldPlugin) {
        super(coldPlugin);
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
