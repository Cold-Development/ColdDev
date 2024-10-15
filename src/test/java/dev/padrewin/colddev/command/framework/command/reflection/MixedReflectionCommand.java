package dev.padrewin.colddev.command.framework.command.reflection;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.command.framework.handler.TestArgumentHandler;
import dev.padrewin.colddev.command.framework.model.TestEnum;

public class MixedReflectionCommand extends BaseColdCommand {

    public MixedReflectionCommand(ColdPlugin ColdPlugin) {
        super(ColdPlugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        throw new IllegalStateException("Should never be called for this command");
    }

    @ColdExecutable
    public void execute(CommandContext context, String object) {
        context.getSender().sendMessage(object);
    }

    @ColdExecutable
    public void execute(CommandContext context, TestEnum value, String object) {
        context.getSender().sendMessage(value.name());
        context.getSender().sendMessage(object);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test")
                .arguments(ArgumentsDefinition.builder()
                        .optional("arg1", ArgumentHandlers.forEnum(TestEnum.class))
                        .required("arg2", new TestArgumentHandler())
                        .build())
                .build();
    }

}
