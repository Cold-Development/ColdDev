package dev.padrewin.colddev.command.framework.command.reflection;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.model.TestEnum;

public class MissingExecutableReflectionCommand extends BaseColdCommand {

    public MissingExecutableReflectionCommand(ColdPlugin ColdPlugin) {
        super(ColdPlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test")
                .arguments(ArgumentsDefinition.builder()
                        .required("arg1", ArgumentHandlers.forEnum(TestEnum.class))
                        .build())
                .build();
    }

}
