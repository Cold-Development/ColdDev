package dev.padrewin.coldplugin.command.framework.command.optional;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.argument.ArgumentHandlers;
import dev.padrewin.coldplugin.command.framework.ArgumentsDefinition;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.CommandInfo;
import dev.padrewin.coldplugin.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldplugin.command.framework.model.TestEnum;

public class TwoArgsCommand extends BaseColdCommand {

    public TwoArgsCommand(ColdPlugin coldPlugin) {
        super(coldPlugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        TestEnum value = context.get("arg1");
        TestEnum value2 = context.get("arg2");

        context.getSender().sendMessage(value + "");
        context.getSender().sendMessage(value2 + "");
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test")
                .arguments(ArgumentsDefinition.builder()
                        .optional("arg1", ArgumentHandlers.forEnum(TestEnum.class))
                        .optional("arg2", ArgumentHandlers.forEnum(TestEnum.class))
                        .build())
                .build();
    }

}
