package dev.padrewin.coldplugin.command.framework.command.reflection;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.argument.ArgumentHandlers;
import dev.padrewin.coldplugin.command.framework.ArgumentsDefinition;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.CommandInfo;
import dev.padrewin.coldplugin.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldplugin.command.framework.handler.TestArgumentHandler;
import dev.padrewin.coldplugin.command.framework.model.TestEnum;

public class MissingExecutableReflectionCommand extends BaseColdCommand {

    public MissingExecutableReflectionCommand(ColdPlugin coldPlugin) {
        super(coldPlugin);
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
